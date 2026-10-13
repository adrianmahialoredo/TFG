package com.uo289390.toco.service;

import com.uo289390.toco.dto.TabSectionDTO;
import com.uo289390.toco.model.TabSection;
import com.uo289390.toco.repository.TabSectionRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;
import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.uo289390.toco.utils.EntityResolver.resolveSongLesson;
import static com.uo289390.toco.utils.EntityResolver.findTabSection;

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
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TabSectionServiceImpl implements TabSectionService {

    private final TabSectionRepository tabSectionRepository;
    private final DtoMapper dtoMapper;
    private final EntityMapper entityMapper;

    @Override
    @Transactional
    public TabSectionDTO create(TabSectionDTO dto) {
        TabSection section = entityMapper.tabSectionDtoToTabSection(dto, resolveSongLesson(dto.getSongLessonId()));
        return dtoMapper.tabSectionToTabSectionDto(tabSectionRepository.save(section));
    }

    @Override
    public List<TabSectionDTO> findAll() {
        return tabSectionRepository.findAll().stream()
                .map(dtoMapper::tabSectionToTabSectionDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TabSectionDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return tabSectionRepository.findAllById(ids).stream()
                .map(dtoMapper::tabSectionToTabSectionDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TabSectionDTO> findById(Long id) {
        return findTabSection(id).map(dtoMapper::tabSectionToTabSectionDto);
    }

    @Override
    public List<TabSectionDTO> findForSongLesson(Long lessonId) {
        return tabSectionRepository.findBySongLessonId(lessonId)
                .stream()
                .map(dtoMapper::tabSectionToTabSectionDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<TabSectionDTO> update(Long id, TabSectionDTO dto) {
        return findTabSection(id).map(existing -> {
            updateEntity(existing, dto);
            return dtoMapper.tabSectionToTabSectionDto(tabSectionRepository.save(existing));
        });
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!tabSectionRepository.existsById(id)) return false;
        tabSectionRepository.deleteById(id);
        return true;
    }

    // ------------- Mapping -------------


    private void updateEntity(TabSection section, TabSectionDTO dto) {
        section.setIdentifier(dto.getIdentifier());
        section.setContent(dto.getContent());

        if (!section.getSongLesson().getId().equals(dto.getSongLessonId())) {
            section.setSongLesson(resolveSongLesson(dto.getSongLessonId()));
        }
    }

}

