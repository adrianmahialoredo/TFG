package com.demo.demo.service;

import com.demo.demo.dto.InstrumentGenreDTO;
import com.demo.demo.utils.mapper.DtoMapper;
import com.demo.demo.utils.mapper.EntityMapper;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.demo.demo.utils.EntityResolver.resolveGenre;
import static com.demo.demo.utils.EntityResolver.resolveInstrument;
import static com.demo.demo.utils.EntityResolver.resolveLessons;
import static com.demo.demo.utils.EntityResolver.findInstrumentGenre;

/**
 * Default implementation of {@link InstrumentGenreService}.
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
public class InstrumentGenreServiceImpl implements InstrumentGenreService {

    private final InstrumentGenreRepository instrumentGenreRepository;
    private final DtoMapper dtoMapper;
    private final EntityMapper entityMapper;

    @Override
    public List<InstrumentGenreDTO> findAll() {
        return instrumentGenreRepository.findAll().stream()
                .map(dtoMapper::instrumentGenreToInstrumentGenreDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstrumentGenreDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return instrumentGenreRepository.findAllById(ids).stream()
                .map(dtoMapper::instrumentGenreToInstrumentGenreDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<InstrumentGenreDTO> findById(Long id) {
        return findInstrumentGenre(id)
                .map(dtoMapper::instrumentGenreToInstrumentGenreDto);
    }

    /**
     * Lessons own the relationship (lessons.instrument_genre_id), so they are linked to the saved
     * (managed) InstrumentGenre inside the transaction and their changes are flushed on commit.
     */
    @Override
    @Transactional
    public InstrumentGenreDTO create(InstrumentGenreDTO dto) {
        InstrumentGenre ig = entityMapper.instrumentGenreDtoToInstrumentGenre(
                dto,
                resolveInstrument(dto.getInstrumentId()),
                resolveGenre(dto.getGenreId())
        );
        InstrumentGenre saved = instrumentGenreRepository.save(ig);
        saved.setLessons(resolveLessons(dto.getLessonIds()));
        return dtoMapper.instrumentGenreToInstrumentGenreDto(saved);
    }

    @Override
    @Transactional
    public Optional<InstrumentGenreDTO> update(Long id, InstrumentGenreDTO dto) {
        return findInstrumentGenre(id).map(existing -> {
            return dtoMapper.instrumentGenreToInstrumentGenreDto(instrumentGenreRepository.save(updateFromDto(existing, dto)));
        });
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return findInstrumentGenre(id).map(ig -> {
            // Las lecciones apuntan al InstrumentGenre con una FK: se desvinculan antes de borrarlo
            ig.setLessons(Set.of());
            instrumentGenreRepository.delete(ig);
            return true;
        }).orElse(false);
    }

    @Override
    public List<InstrumentGenreDTO> findForGenre(Long genreId) {
        return instrumentGenreRepository.findByGenreId(genreId).stream()
                .map(dtoMapper::instrumentGenreToInstrumentGenreDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstrumentGenreDTO> findForInstrument(Long instrumentId) {
        return instrumentGenreRepository.findByInstrumentId(instrumentId).stream()
                .map(dtoMapper::instrumentGenreToInstrumentGenreDto)
                .collect(Collectors.toList());
    }

    // -----------------------
    // Mapping Methods
    // -----------------------


    private InstrumentGenre updateFromDto(InstrumentGenre existing, InstrumentGenreDTO dto) {
        existing.setTitle(dto.getTitle());
        existing.setImageUrl(dto.getImageUrl());
        existing.setLevel(dto.getLevel());
        existing.setType(Item.Type.InstrumentGenre);
        existing.setInstrument(resolveInstrument(dto.getInstrumentId()));
        existing.setGenre(resolveGenre(dto.getGenreId()));
        existing.setLessons(resolveLessons(dto.getLessonIds()));
        return existing;
    }

}

