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
 * Service class for managing genres.
 * 
 * Provides methods to create, read, update, and delete genres,
 * as well as methods to fetch genres by ID and convert them to DTOs.
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
public class GenreService {

    private final GenreRepository genreRepository;
    private final SongRepository songRepository;
    private final LessonRepository lessonRepository;

    /**
     * Fetches all genres from the repository and converts them to DTOs.
     * 
     * @return List of GenreDTOs representing all genres.
     */
    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a genre by its ID and converts it to a DTO.
     * 
     * @param id The ID of the genre to fetch.
     * @return An Optional containing the GenreDTO if found, otherwise empty.
     */
    public Optional<GenreDTO> getGenreById(Long id) {
        return genreRepository.findById(id)
                .map(this::toDTO);
    }

    /**
     * Creates a new genre from the provided DTO and saves it to the repository.
     * 
     * @param dto The GenreDTO containing the details of the genre to create.
     * @return The created GenreDTO.
     */
    public GenreDTO createGenre(GenreDTO dto) {
        Genre genre = toEntity(dto);
        return toDTO(genreRepository.save(genre));
    }

    /**
     * Updates an existing genre identified by its ID with the details from the provided DTO.
     * 
     * @param id  The ID of the genre to update.
     * @param dto The GenreDTO containing the updated details of the genre.
     * @return An Optional containing the updated GenreDTO if found, otherwise empty.
     */
    public Optional<GenreDTO> updateGenre(Long id, GenreDTO dto) {
        return genreRepository.findById(id)
                .map(existing -> {
                    return toDTO(genreRepository.save(updateGenreFromDTO(existing, dto)));
                });
    }

    /**
     * Deletes a genre by its ID.
     * 
     * @param id The ID of the genre to delete.
     * @return true if the genre was deleted, false if it did not exist.
     */
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

    /**
     * Converts a Genre entity to a GenreDTO.
     * 
     * @param genre The Genre entity to convert.
     * @return The converted GenreDTO.
     */
    private GenreDTO toDTO(Genre genre) {
        GenreDTO dto = new GenreDTO();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        dto.setImageUrl(genre.getImageUrl());
        dto.setDescription(genre.getDescription());
        dto.setBannerUrl(genre.getBannerUrl());
        dto.setWelcomeIntroVideoId(genre.getWelcomeIntroVideo());
        dto.setWelcomeSongVideoId(genre.getWelcomeSongVideo());

        if (genre.getSongs() == null) return dto;
        dto.setSongIds(genre.getSongs()
                .stream()
                .map(Song::getId)
                .collect(Collectors.toList()));

        if (genre.getLessons() == null) return dto;
        dto.setLessonIds(genre.getLessons()
                .stream()
                .map(Lesson::getId)
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Converts a GenreDTO to a Genre entity.
     * 
     * @param dto The GenreDTO to convert.
     * @return The converted Genre entity.
     */
    private Genre toEntity(GenreDTO dto) {
        Genre genre = new Genre(); 
        genre.setName(dto.getName());
        genre.setDescription(dto.getDescription());
        genre.setImageUrl(dto.getImageUrl());
        genre.setBannerUrl(dto.getBannerUrl());
        genre.setWelcomeIntroVideo(dto.getWelcomeIntroVideoId());
        genre.setWelcomeSongVideo(dto.getWelcomeSongVideoId());

        if (dto.getSongIds() == null) return genre;
        genre.setSongs(resolveSongs(dto.getSongIds()));

        if (dto.getLessonIds() == null) return genre;
        genre.setLessons(resolveLessons(dto.getLessonIds()));
        
        return genre;
    }

    /**
     * Updates an existing Genre entity with the details from a GenreDTO.
     * 
     * @param existing The existing Genre entity to update.
     * @param dto      The GenreDTO containing the updated details.
     * @return The updated Genre entity.
     */
    private Genre updateGenreFromDTO(Genre existing, GenreDTO dto) {
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setImageUrl(dto.getImageUrl());
        existing.setBannerUrl(dto.getBannerUrl());
        existing.setWelcomeIntroVideo(dto.getWelcomeIntroVideoId());
        existing.setWelcomeSongVideo(dto.getWelcomeSongVideoId());
        if (dto.getSongIds() != null) {
            existing.setSongs(resolveSongs(dto.getSongIds()));
        }
        if (dto.getLessonIds() != null) {
            existing.setLessons(resolveLessons(dto.getLessonIds()));
        }
        return existing;
    }

    /**
     * Resolves a list of song IDs to a set of Song entities.
     * 
     * @param songIds The list of song IDs to resolve.
     * @return A set of Song entities corresponding to the provided IDs.
     */
    private Set<Song> resolveSongs(List<Long> songIds) {
        if (songIds == null || songIds.isEmpty()) {
            return new HashSet<>();
        }
        return songIds.stream()
                .map(id -> songRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Song not found: " + id)))
                .collect(Collectors.toSet());
    }

    /**
     * Resolves a list of lesson IDs to a set of Lesson entities.
     * 
     * @param lessonIds The list of lesson IDs to resolve.
     * @return A set of Lesson entities corresponding to the provided IDs.
     */
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
