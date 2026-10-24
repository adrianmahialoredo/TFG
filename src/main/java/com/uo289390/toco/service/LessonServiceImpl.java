package com.uo289390.toco.service;

import com.uo289390.toco.dto.LessonDTO;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.LessonSection;
import com.uo289390.toco.repository.LessonRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.*;
import com.uo289390.toco.utils.EntityResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.uo289390.toco.utils.EntityResolver.findLesson;

/**
 * Default implementation of {@link LessonService}.
 * Optimized for cascading creation via scripts while maintaining
 * flat DTO responses for the frontend.
 *
 * @author Adrián Mahía
 * @version 1.2
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final DtoMapper dtoMapper;

    @Override
    public List<LessonDTO> findAll() {
        return lessonRepository.findAll().stream()
                .map(dtoMapper::lessonToLessonDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LessonDTO> findById(Long id) {
        return findLesson(id).map(dtoMapper::lessonToLessonDto);
    }

    @Override
    @Transactional
    public LessonDTO create(LessonDTO dto) {
        // 1. Creamos la instancia
        Lesson lesson = new Lesson();

        // 2. Seteamos el ID manual (crucial para la arquitectura)
        lesson.setId(dto.getId());
        lesson.setType(Item.Type.Lesson);

        // 3. Seteamos campos básicos
        lesson.setTitle(dto.getTitle());
        lesson.setLevel(dto.getLevel());
        lesson.setImageUrl(dto.getImageUrl());
        lesson.setBelongsToGenre(dto.isBelongsToGenre());
        lesson.setPopular(dto.isPopular());

        Optional.ofNullable(dto.getGenreId())
                .map(EntityResolver::resolveGenre)
                .ifPresent(genre -> {
                    lesson.setGenre(genre);

                    //Si pertenece a genero, sobrescribir la portada
                    if (dto.isBelongsToGenre() && genre.getImageUrl() != null) {
                        lesson.setImageUrl(genre.getImageUrl());
                    }
                });

        assignInstrumentGenre(lesson, dto);
        assignInstrument(lesson, dto);

        // 4. IMPORTANTE: Poblamos las secciones ANTES del save
        // Esto es lo que satisface la validación @NotEmpty
        if (dto.getSections() != null && !dto.getSections().isEmpty()) {
            dto.getSections().forEach(sDto -> {
                LessonSection section = new LessonSection();
                section.setTitle(sDto.getTitle());
                section.setHtmlContent(sDto.getHtmlContent());
                section.setVideoUrl(sDto.getVideoUrl());
                section.setOrderIndex(sDto.getOrderIndex());
                lesson.addSection(section);
            });
        }

        // 5. Validamos nosotros manualmente en el log antes de petar
        if (lesson.getSections().isEmpty()) {
            log.error("¡CUIDADO! Intentando guardar lección ID {} sin secciones. Esto va a fallar.", dto.getId());
        }

        // 6. Guardamos
        Lesson saved = lessonRepository.save(lesson);
        return dtoMapper.lessonToLessonDto(saved);
    }

    @Override
    @Transactional
    public Optional<LessonDTO> update(Long id, LessonDTO dto) {
        return findLesson(id).map(existing -> {
            updateLessonFromDto(existing, dto);
            return dtoMapper.lessonToLessonDto(lessonRepository.save(existing));
        });
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!lessonRepository.existsById(id)) return false;
        lessonRepository.deleteById(id);
        return true;
    }

    @Override
    public List<LessonDTO> findForGenre(Long genreId) {
        return lessonRepository.findByGenreIdAndBelongsToGenreTrue(genreId).stream()
                .map(dtoMapper::lessonToLessonDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LessonDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        return lessonRepository.findAllById(ids).stream()
                .map(dtoMapper::lessonToLessonDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findIdsPopular() {
        return lessonRepository.findByPopularTrue().stream()
                .map(Lesson::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findIdsNew() {
        return lessonRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(Lesson::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findIdsRecommended() {
        return lessonRepository.findAllByOrderByLevelAscIdAsc().stream()
                .map(Lesson::getId)
                .collect(Collectors.toList());
    }
    @Override
    public List<Long> findIdsInstrumentPopular(Long id) {
        return lessonRepository.findByInstrumentIdAndInstrumentPopularTrue(id).stream()
                .map(Lesson::getId)
                .collect(Collectors.toList());
    }

    // -------- MAPPING (Paz con el Frontend) --------


    private void updateLessonFromDto(Lesson lesson, LessonDTO dto) {
        lesson.setTitle(dto.getTitle());
        lesson.setLevel(dto.getLevel());
        lesson.setImageUrl(dto.getImageUrl());
        lesson.setBelongsToGenre(dto.isBelongsToGenre());
        lesson.setPopular(dto.isPopular());

        Optional.ofNullable(dto.getGenreId())
                .map(EntityResolver::resolveGenre)
                .ifPresent(lesson::setGenre);

        assignInstrumentGenre(lesson, dto);
        assignInstrument(lesson, dto);

        // Si la actualización incluye secciones, refrescamos el agregado
        if (dto.getSections() != null) {
            lesson.getSections().clear(); // orphanRemoval=true las borrará de la DB
            dto.getSections().forEach(sDto -> {
                LessonSection s = new LessonSection();
                s.setTitle(sDto.getTitle());
                s.setHtmlContent(sDto.getHtmlContent());
                s.setVideoUrl(sDto.getVideoUrl());
                s.setOrderIndex(sDto.getOrderIndex());
                lesson.addSection(s);
            });
        }
    }

    /**
     * Makes the lesson part of the instrument-genre indicated in the DTO, if any.
     * As with the genre, a DTO without instrumentGenreId leaves the current instrument-genre untouched.
     */
    private void assignInstrumentGenre(Lesson lesson, LessonDTO dto) {
        Optional.ofNullable(dto.getInstrumentGenreId())
                .map(EntityResolver::resolveInstrumentGenre)
                .ifPresent(instrumentGenre -> lesson.joinInstrumentGenre(instrumentGenre, dto.getIndexInInstrumentGenre()));
    }

    /**
     * Associates the lesson with the instrument indicated in the DTO, if any.
     * As with the genre, a DTO without instrumentId leaves the current instrument untouched.
     */
    private void assignInstrument(Lesson lesson, LessonDTO dto) {
        Optional.ofNullable(dto.getInstrumentId())
                .map(EntityResolver::resolveInstrument)
                .ifPresent(lesson::setInstrument);
    }
}

