package com.demo.demo.service;

import com.demo.demo.dto.GenreDTO;
import com.demo.demo.utils.mapper.DtoMapper;
import com.demo.demo.utils.mapper.EntityMapper;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.demo.demo.utils.EntityResolver.resolveLessons;
import static com.demo.demo.utils.EntityResolver.findGenre;

/**
 * Default implementation of {@link GenreService}.
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
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final SongRepository songRepository;
    private final DtoMapper dtoMapper;
    private final EntityMapper entityMapper;

    @Override
    public List<GenreDTO> findAll() {
        return genreRepository.findAll().stream()
                .map(dtoMapper::genreToGenreDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GenreDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return genreRepository.findAllById(ids).stream()
                .map(dtoMapper::genreToGenreDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<GenreDTO> findById(Long id) {
        return findGenre(id)
                .map(dtoMapper::genreToGenreDto);
    }

    @Override
    @Transactional
    public GenreDTO create(GenreDTO dto) {
        Genre genre = entityMapper.genreDtoToGenre(dto, resolveLessons(dto.getLessonIds()));
        return dtoMapper.genreToGenreDto(genreRepository.save(genre));
    }

    @Override
    @Transactional
    public Optional<GenreDTO> update(Long id, GenreDTO dto) {
        return findGenre(id)
                .map(existing -> {
                    return dtoMapper.genreToGenreDto(genreRepository.save(updateGenreFromDTO(existing, dto)));
                });
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!genreRepository.existsById(id)) {
            return false;
        }
        genreRepository.deleteById(id);
        return true;
    }

    // ----------------------
    // DTO Mapping Utilities
    // ----------------------


    private Genre updateGenreFromDTO(Genre existing, GenreDTO dto) {
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setImageUrl(dto.getImageUrl());
        if (dto.getLevel() > 0) {
            existing.setLevel(dto.getLevel());
        }
        existing.setBannerUrl(dto.getBannerUrl());
        existing.setWelcomeIntroVideoId(dto.getWelcomeIntroVideoId());
        existing.setWelcomeSongVideoId(dto.getWelcomeSongVideoId());
        if (dto.getLessonIds() != null) {
            existing.setLessons(resolveLessons(dto.getLessonIds()));
        }
        return existing;
    }

}

