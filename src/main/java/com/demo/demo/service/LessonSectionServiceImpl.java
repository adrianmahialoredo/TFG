package com.demo.demo.service;

import com.demo.demo.dto.LessonSectionDTO;
import com.demo.demo.model.Lesson;
import com.demo.demo.model.LessonSection;
import com.demo.demo.repository.LessonRepository;
import com.demo.demo.repository.LessonSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
@Slf4j
@Service
@RequiredArgsConstructor
public class LessonSectionServiceImpl implements LessonSectionService {

    private final LessonSectionRepository lessonSectionRepository;
    private final LessonRepository lessonRepository;

    @Override
    public LessonSectionDTO createLessonSection(LessonSectionDTO dto) {
        LessonSection section = toEntity(dto);
        return toDTO(lessonSectionRepository.save(section));
    }

    @Override
    public Optional<LessonSectionDTO> getLessonSectionById(Long id) {
        return lessonSectionRepository.findById(id).map(this::toDTO);
    }

    @Override
    public List<LessonSectionDTO> getLessonSectionsByLessonId(Long lessonId) {
        return lessonSectionRepository.findByLessonId(lessonId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LessonSectionDTO> updateLessonSection(Long id, LessonSectionDTO dto) {
        return lessonSectionRepository.findById(id).map(existing -> {
            updateEntity(existing, dto);
            return toDTO(lessonSectionRepository.save(existing));
        });
    }

    @Override
    public boolean deleteLessonSection(Long id) {
        if (!lessonSectionRepository.existsById(id)) {
            return false;
        }
        lessonSectionRepository.deleteById(id);
        return true;
    }

    // ------------- Mapping -------------

    private LessonSectionDTO toDTO(LessonSection section) {
        LessonSectionDTO dto = new LessonSectionDTO();
        dto.setId(section.getId());
        dto.setTitle(section.getTitle());
        dto.setHtmlContent(section.getHtmlContent());
        dto.setOrderIndex(section.getOrderIndex());
        dto.setLessonId(section.getLesson().getId());
        return dto;
    }

    private LessonSection toEntity(LessonSectionDTO dto) {
        LessonSection section = new LessonSection();
        section.setId(dto.getId());
        section.setTitle(dto.getTitle());
        section.setHtmlContent(dto.getHtmlContent());
        section.setOrderIndex(dto.getOrderIndex());
        section.setLesson(resolveLesson(dto.getLessonId()));
        return section;
    }

    private void updateEntity(LessonSection section, LessonSectionDTO dto) {
        section.setTitle(dto.getTitle());
        section.setHtmlContent(dto.getHtmlContent());
        section.setOrderIndex(dto.getOrderIndex());

        if (!section.getLesson().getId().equals(dto.getLessonId())) {
            section.setLesson(resolveLesson(dto.getLessonId()));
        }
    }

    private Lesson resolveLesson(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + id));
    }
}

