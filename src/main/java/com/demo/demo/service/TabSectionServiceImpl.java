package com.demo.demo.service;

import com.demo.demo.dto.TabSectionDTO;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link TabSectionService}.
 *
 * This service uses repositories to interact with the database
 * and convert entities to DTOs for API responses.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TabSectionServiceImpl implements TabSectionService {

    private final TabSectionRepository tabSectionRepository;
    private final SongLessonRepository songLessonRepository;

    @Override
    public TabSectionDTO createTabSection(TabSectionDTO dto) {
        TabSection section = toEntity(dto);
        return toDTO(tabSectionRepository.save(section));
    }

    @Override
    public Optional<TabSectionDTO> getTabSectionById(Long id) {
        return tabSectionRepository.findById(id).map(this::toDTO);
    }

    @Override
    public List<TabSectionDTO> getTabSectionsBySongLessonId(Long lessonId) {
        return tabSectionRepository.findBySongLessonId(lessonId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TabSectionDTO> updateTabSection(Long id, TabSectionDTO dto) {
        return tabSectionRepository.findById(id).map(existing -> {
            updateEntity(existing, dto);
            return toDTO(tabSectionRepository.save(existing));
        });
    }

    @Override
    public boolean deleteTabSection(Long id) {
        if (!tabSectionRepository.existsById(id)) return false;
        tabSectionRepository.deleteById(id);
        return true;
    }

    // ------------- Mapping -------------

    private TabSectionDTO toDTO(TabSection section) {
        TabSectionDTO dto = new TabSectionDTO();
        dto.setId(section.getId());
        dto.setIdentifier(section.getIdentifier());
        dto.setContent(section.getContent());
        dto.setSongLessonId(section.getSongLesson().getId());
        return dto;
    }

    private TabSection toEntity(TabSectionDTO dto) {
        TabSection section = new TabSection();
        section.setId(dto.getId());
        section.setIdentifier(dto.getIdentifier());
        section.setContent(dto.getContent());
        section.setSongLesson(resolveSongLesson(dto.getSongLessonId()));
        return section;
    }

    private void updateEntity(TabSection section, TabSectionDTO dto) {
        section.setIdentifier(dto.getIdentifier());
        section.setContent(dto.getContent());

        if (!section.getSongLesson().getId().equals(dto.getSongLessonId())) {
            section.setSongLesson(resolveSongLesson(dto.getSongLessonId()));
        }
    }

    private SongLesson resolveSongLesson(Long id) {
        return songLessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SongLesson not found: " + id));
    }

}

