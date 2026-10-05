package com.demo.demo.service;

import com.demo.demo.dto.LessonSectionDTO;
import com.demo.demo.utils.mapper.DtoMapper;
import com.demo.demo.utils.mapper.EntityMapper;
import com.demo.demo.model.LessonSection;
import com.demo.demo.repository.LessonSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.demo.demo.utils.EntityResolver.resolveLesson;
import static com.demo.demo.utils.EntityResolver.findLessonSection;

/**
 * Default implementation of {@link LessonSectionService}.
 *
 * This service uses repositories to interact with the database
 * and convert entities to DTOs for API responses.
 *
 * @author Adrian Mahia
 * @version 1.0
 * @since 2025-06-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonSectionServiceImpl implements LessonSectionService {

    private final LessonSectionRepository lessonSectionRepository;
    private final DtoMapper dtoMapper;
    private final EntityMapper entityMapper;

    @Override
    @Transactional
    public LessonSectionDTO create(LessonSectionDTO dto) {
        LessonSection section = entityMapper.lessonSectionDtoToLessonSection(dto, resolveLesson(dto.getLessonId()));
        return dtoMapper.lessonSectionToLessonSectionDto(lessonSectionRepository.save(section));
    }

    @Override
    public List<LessonSectionDTO> findAll() {
        return lessonSectionRepository.findAll().stream()
                .map(dtoMapper::lessonSectionToLessonSectionDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LessonSectionDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return lessonSectionRepository.findAllById(ids).stream()
                .map(dtoMapper::lessonSectionToLessonSectionDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LessonSectionDTO> findById(Long id) {
        return findLessonSection(id).map(dtoMapper::lessonSectionToLessonSectionDto);
    }

    @Override
    public List<LessonSectionDTO> findForLesson(Long lessonId) {
        return lessonSectionRepository.findByLessonId(lessonId)
                .stream()
                .map(dtoMapper::lessonSectionToLessonSectionDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<LessonSectionDTO> update(Long id, LessonSectionDTO dto) {
        return findLessonSection(id).map(existing -> {
            updateEntity(existing, dto);
            return dtoMapper.lessonSectionToLessonSectionDto(lessonSectionRepository.save(existing));
        });
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!lessonSectionRepository.existsById(id)) {
            return false;
        }
        lessonSectionRepository.deleteById(id);
        return true;
    }

    // ------------- Mapping -------------


    private void updateEntity(LessonSection section, LessonSectionDTO dto) {
        section.setTitle(dto.getTitle());
        section.setHtmlContent(dto.getHtmlContent());
        section.setOrderIndex(dto.getOrderIndex());

        if (!section.getLesson().getId().equals(dto.getLessonId())) {
            section.setLesson(resolveLesson(dto.getLessonId()));
        }
    }
}

