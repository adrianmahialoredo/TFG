package com.demo.demo.service;

import com.demo.demo.dto.GenreDTO;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

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
@Slf4j
@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final SongRepository songRepository;
    private final LessonRepository lessonRepository;

    @Override
    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<GenreDTO> getGenreById(Long id) {
        return genreRepository.findById(id)
                .map(this::toDTO);
    }

    @Override
    public GenreDTO createGenre(GenreDTO dto) {
        Genre genre = toEntity(dto);
        return toDTO(genreRepository.save(genre));
    }

    @Override
    public Optional<GenreDTO> updateGenre(Long id, GenreDTO dto) {
        return genreRepository.findById(id)
                .map(existing -> {
                    return toDTO(genreRepository.save(updateGenreFromDTO(existing, dto)));
                });
    }

    @Override
    public boolean deleteGenre(Long id) {
        if (!genreRepository.existsById(id)) {
            return false;
        }
        genreRepository.deleteById(id);
        return true;
    }

    // ----------------------
    // DTO Mapping Utilities
    // ----------------------

    private GenreDTO toDTO(Genre genre) {
        GenreDTO dto = new GenreDTO();
        dto.setId(genre.getId());
        dto.setTitle(genre.getTitle());
        dto.setImageUrl(genre.getImageUrl());
        dto.setLevel(genre.getLevel());
        dto.setDescription(genre.getDescription());
        dto.setBannerUrl(genre.getBannerUrl());
        dto.setWelcomeIntroVideoId(genre.getWelcomeIntroVideoId());
        dto.setWelcomeSongVideoId(genre.getWelcomeSongVideoId());

        if (genre.getLessons() == null) return dto;
        dto.setLessonIds(genre.getLessons()
                .stream()
                .map(Lesson::getId)
                .collect(Collectors.toList()));

        return dto;
    }

    private Genre toEntity(GenreDTO dto) {
        Genre genre = new Genre(); 
        genre.setId(dto.getId());
        genre.setTitle(dto.getTitle());
        genre.setDescription(dto.getDescription());
        genre.setImageUrl(dto.getImageUrl());
        genre.setLevel(dto.getLevel());
        genre.setBannerUrl(dto.getBannerUrl());
        genre.setWelcomeIntroVideoId(dto.getWelcomeIntroVideoId());
        genre.setWelcomeSongVideoId(dto.getWelcomeSongVideoId());
        genre.setType(Item.Type.Genre);

        if (dto.getLessonIds() == null) return genre;
        genre.setLessons(resolveLessons(dto.getLessonIds()));
        
        return genre;
    }

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

    private Set<Lesson> resolveLessons(List<Long> lessonIds) {
        if (lessonIds == null || lessonIds.isEmpty()) {
            return new HashSet<>();
        }
        return lessonIds.stream()
                .map(id -> lessonRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + id)))
                .collect(Collectors.toSet());
    }

}

