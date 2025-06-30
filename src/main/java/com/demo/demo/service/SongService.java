package com.demo.demo.service;

import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.RequiredArgsConstructor;
import com.demo.demo.dto.SongDTO;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing songs.
 * 
 * Provides methods to create, read, update, and delete songs,
 * as well as methods to fetch songs by ID and convert them to DTOs.
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
public class SongService {

    private final SongRepository songRepository;
    private final GenreRepository genreRepository;
    private final InstrumentRepository instrumentRepository;
    private final SongLessonRepository songLessonRepository;

    /**
     * Fetches all songs from the repository and converts them to DTOs.
     *
     * @return List of SongDTOs representing all songs.
     */
    public List<SongDTO> getAllSongs() {
        return songRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a song by its ID and converts it to a DTO.
     *
     * @param id The ID of the song to fetch.
     * @return An Optional containing the SongDTO if found, otherwise empty.
     */
    public Optional<SongDTO> getSongById(Long id) {
        return songRepository.findById(id)
                .map(this::toDto);
    }

    /**
     * Creates a new song from the provided DTO and saves it to the repository.
     *
     * @param dto The SongDTO containing the details of the song to create.
     * @return The created SongDTO.
     */
    public SongDTO createSong(SongDTO dto) {
        Song song = toEntity(dto);
        return toDto(songRepository.save(song));
    }

    /**
     * Updates an existing song with the provided DTO and saves it to the repository.
     *
     * @param id  The ID of the song to update.
     * @param dto The SongDTO containing the updated details of the song.
     * @return An Optional containing the updated SongDTO if found, otherwise empty.
     */
    public Optional<SongDTO> updateSong(Long id, SongDTO dto) {
        return songRepository.findById(id).map(existing -> {
            updateSongFromDto(existing, dto);
            return toDto(songRepository.save(existing));
        });
    }

    /**
     * Deletes a song by its ID.
     *
     * @param id The ID of the song to delete.
     * @return true if the song was deleted, false if it did not exist.
     */
    public boolean deleteSong(Long id) {
        if (!songRepository.existsById(id)) {
            return false;
        }
        songRepository.deleteById(id);
        return true;
    }

    /**
     * Fetches all songs by a specific decade and converts them to DTOs.
     * 
     * @param decade The decade to filter songs by (e.g., "1980s", "1990s").
     * @return List of SongDTOs representing songs from the specified decade.
     */
    public List<SongDTO> getSongsByDecade(String decade) {
        return songRepository.findByDecade(decade).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Fetches all popular songs and converts them to DTOs.
     * 
     * @return List of SongDTOs representing popular songs.
     */
    public List<SongDTO> getPopularSongs() {
        return songRepository.findAll().stream()
                .filter(Song::isPopular)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Fetches all popular songs by genre and converts them to DTOs.
     * 
     * @param genreId The ID of the genre to filter popular songs by.
     * @return List of SongDTOs representing popular songs in the specified genre.
     */
    public List<SongDTO> getPopularSongsByGenre(Long genreId) {
        return songRepository.findAll().stream()
                .filter(song -> song.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId)) && song.isGenrePopular())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Fetches all classic songs by gebnre and converts them to DTOs.
     * 
     * @param genreId The ID of the genre to filter classic songs by.
     * @return List of SongDTOs representing classic songs in the specified genre.
     */
    public List<SongDTO> getClassicSongsByGenre(Long genreId) {
        return songRepository.findAll().stream()
                .filter(song -> song.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId)) && song.isGenreClassic())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Fetches all new songs by genre and converts them to DTOs.
     * 
     * @param genreId The ID of the genre to filter new songs by.
     * @return List of SongDTOs representing new songs in the specified genre.
     */
    public List<SongDTO> getNewSongsByGenre(Long genreId) {
        return songRepository.findAll().stream()
                .filter(song -> song.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId)) && song.isGenreNew())
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    // -------- DTO Mapping --------

    /**
     * Converts a Song entity to a SongDTO.
     *
     * @param song The Song entity to convert.
     * @return The corresponding SongDTO.
     */
    private SongDTO toDto(Song song) {
        SongDTO dto = new SongDTO();
        dto.setId(song.getId());
        dto.setTitle(song.getTitle());
        dto.setArtist(song.getArtist());
        dto.setGenreIds(song.getGenres().stream().map(Genre::getId).collect(Collectors.toList()));
        dto.setInstrumentIds(song.getInstruments().stream().map(Instrument::getId).collect(Collectors.toList()));
        dto.setLessons(song.getLessons().stream().map(SongLesson::getId).collect(Collectors.toList()));
        return dto;
    }

    /**
     * Converts a SongDTO to a Song entity.
     *
     * @param dto The SongDTO to convert.
     * @return The corresponding Song entity.
     */
    private Song toEntity(SongDTO dto) {
        Song song = new Song();
        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setGenres(resolveGenres(dto.getGenreIds()));
        song.setInstruments(resolveInstruments(dto.getInstrumentIds()));
        song.setLessons(resolveLessons(dto.getLessons()));
        return song;
    }

    /**
     * Updates an existing Song entity with the details from the provided SongDTO.
     *
     * @param song The existing Song entity to update.
     * @param dto  The SongDTO containing the updated details.
     */
    private void updateSongFromDto(Song song, SongDTO dto) {
        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setGenres(resolveGenres(dto.getGenreIds()));
        song.setInstruments(resolveInstruments(dto.getInstrumentIds()));
        song.setLessons(resolveLessons(dto.getLessons()));
    }

    /**
     * Resolves a set of Genre entities from a list of genre IDs.
     * 
     * @param ids The list of genre IDs to resolve.
     * @return A set of Genre entities corresponding to the provided IDs.
     * @throws IllegalArgumentException if any genre ID does not exist.
     */
    private Set<Genre> resolveGenres(List<Long> ids) {
        return ids.stream()
                .map(id -> genreRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + id)))
                .collect(Collectors.toSet());
    }

    /**
     * Resolves a set of Instrument entities from a list of instrument IDs.
     * 
     * @param ids The list of instrument IDs to resolve.
     * @return A set of Instrument entities corresponding to the provided IDs.
     * @throws IllegalArgumentException if any instrument ID does not exist.
     */
    private Set<Instrument> resolveInstruments(List<Long> ids) {
        return ids.stream()
                .map(id -> instrumentRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + id)))
                .collect(Collectors.toSet());
    }

    /**
     * Resolves a list of SongLesson entities from a list of lesson IDs.
     * 
     * @param ids The list of lesson IDs to resolve.
     * @return A list of SongLesson entities corresponding to the provided IDs.
     * @throws IllegalArgumentException if any lesson ID does not exist.
     */
    private List<SongLesson> resolveLessons(List<Long> ids) {
        return ids.stream()
                .map(id -> songLessonRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + id)))
                .collect(Collectors.toList());
    }
}
