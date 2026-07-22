package com.demo.demo.service;

import com.demo.demo.dto.LessonDTO;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final GenreRepository genreRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getAllLessons() {
        return lessonRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LessonDTO> getLessonById(Long id) {
        return lessonRepository.findById(id).map(this::toDto);
    }

    @Override
    @Transactional
    public LessonDTO createLesson(LessonDTO dto) {
        // 1. Creamos la instancia
        Lesson lesson = new Lesson();

        // 2. Seteamos el ID manual (crucial para tu arquitectura)
        lesson.setId(dto.getId());
        lesson.setType(Item.Type.Lesson);

        // 3. Seteamos campos básicos
        lesson.setTitle(dto.getTitle());
        lesson.setLevel(dto.getLevel());
        lesson.setImageUrl(dto.getImageUrl());
        lesson.setBelongsToGenre(dto.isBelongsToGenre());
        lesson.setPopular(dto.isPopular());

        if (dto.getGenreId() != null) {
            lesson.setGenre(resolveGenre(dto.getGenreId()));
        }

        // 4. IMPORTANTE: Poblamos las secciones ANTES del save
        // Esto es lo que satisface la validación @NotEmpty
        if (dto.getSections() != null && !dto.getSections().isEmpty()) {
            dto.getSections().forEach(sDto -> {
                LessonSection section = new LessonSection();
                section.setTitle(sDto.getTitle());
                section.setHtmlContent(sDto.getHtmlContent());
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
        return toDto(saved);
    }

    @Override
    @Transactional
    public Optional<LessonDTO> updateLesson(Long id, LessonDTO dto) {
        return lessonRepository.findById(id).map(existing -> {
            updateLessonFromDto(existing, dto);
            return toDto(lessonRepository.save(existing));
        });
    }

    @Override
    @Transactional
    public boolean deleteLesson(Long id) {
        if (!lessonRepository.existsById(id)) return false;
        lessonRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsByGenreId(Long genreId) {
        return lessonRepository.findAll().stream()
                .filter(l -> l.isBelongsToGenre() && l.getGenre() != null && l.getGenre().getId().equals(genreId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        return lessonRepository.findAllById(ids).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getHomeLessonsPopular() {
        return lessonRepository.findPopular().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getHomeLessonsNew() {
        return lessonRepository.findHomeLessonsNew().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getHomeLessonsRecommended() {
        return lessonRepository.findHomeLessonsRecommended().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // -------- MAPPING (Paz con el Frontend) --------

    private LessonDTO toDto(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setLevel(lesson.getLevel());

        // Lógica de imagen: prioridad al género, luego a la lección
        dto.setImageUrl(lesson.getGenre() != null && lesson.getGenre().getImageUrl() != null
                ? lesson.getGenre().getImageUrl()
                : lesson.getImageUrl());

        dto.setBelongsToGenre(lesson.isBelongsToGenre());
        dto.setPopular(lesson.isPopular());
        if (lesson.getGenre() != null) {
            dto.setGenreId(lesson.getGenre().getId());
        }

        // Devolvemos solo la lista de IDs para el Front
        if (lesson.getSections() != null) {
            dto.setSectionIds(lesson.getSections().stream()
                    .map(LessonSection::getId)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private void updateLessonFromDto(Lesson lesson, LessonDTO dto) {
        lesson.setTitle(dto.getTitle());
        lesson.setLevel(dto.getLevel());
        lesson.setImageUrl(dto.getImageUrl());
        lesson.setBelongsToGenre(dto.isBelongsToGenre());
        lesson.setPopular(dto.isPopular());

        if (dto.getGenreId() != null) {
            lesson.setGenre(resolveGenre(dto.getGenreId()));
        }

        // Si la actualización incluye secciones, refrescamos el agregado
        if (dto.getSections() != null) {
            lesson.getSections().clear(); // orphanRemoval=true las borrará de la DB
            dto.getSections().forEach(sDto -> {
                LessonSection s = new LessonSection();
                s.setTitle(sDto.getTitle());
                s.setHtmlContent(sDto.getHtmlContent());
                s.setOrderIndex(sDto.getOrderIndex());
                lesson.addSection(s);
            });
        }
    }

    private Genre resolveGenre(Long genreId) {
        return genreRepository.findById(genreId)
                .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + genreId));
    }
}

