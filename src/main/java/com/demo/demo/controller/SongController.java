package com.demo.demo.controller;

import com.demo.demo.service.SongService;
import com.demo.demo.dto.SongDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Controller for managing songs.
 * 
 * Provides endpoints to create, read, update, and delete songs.
 * As well as to fetch all songs or a specific song by ID.
 * 
 * This controller uses the SongService to handle business logic
 * and convert entities to DTOs for API responses.
 * 
 * Endpoints:
 * - GET /songs: Fetch all songs
 * - GET /songs/{id}: Fetch a specific song by ID
 * - GET /songs/decades/{decade}: Fetch all songs by a specific decade
 * - GET /songs/popular: Fetch all popular songs
 * - GET /songs/popular/{id}: Fetch all popular songs by genre ID
 * - GET /songs/new/{id}: Fetch all new songs by genre ID
 * - GET /songs/classic/{id}: Fetch all classic songs by genre ID
 * - POST /songs: Create a new song
 * - PUT /songs/{id}: Update an existing song by ID
 * - DELETE /songs/{id}: Delete a song by ID
 * 
 * This controller is annotated with @RestController to handle RESTful requests
 * and @RequestMapping to define the base path for all endpoints.
 * 
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/songs")
public class SongController {

    private final SongService songService;

    /**
     * Fetches all songs from the service and returns them as a list of SongDTOs.
     *
     * @return List of SongDTOs representing all songs.
     */
    @GetMapping
    public List<SongDTO> getAllSongs() {
        return songService.getAllSongs();
    }

    /**
     * Fetches a specific song by its ID and returns it as a SongDTO.
     *
     * @param id The ID of the song to fetch.
     * @return ResponseEntity containing the SongDTO if found, or 404 Not Found if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getSongById(@PathVariable Long id) {
        return songService.getSongById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new song from the provided SongDTO and returns the created song.
     *
     * @param songDto The SongDTO containing the details of the song to create.
     * @return ResponseEntity containing the created SongDTO with status 201 Created.
     */
    @PostMapping
    public ResponseEntity<SongDTO> createSong(@RequestBody SongDTO songDto) {
        SongDTO created = songService.createSong(songDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing song identified by its ID with the provided SongDTO.
     *
     * @param id The ID of the song to update.
     * @param songDto The SongDTO containing the updated details of the song.
     * @return ResponseEntity containing the updated SongDTO if successful, or 404 Not Found if not found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SongDTO> updateSong(@PathVariable Long id, @RequestBody SongDTO songDto) {
        return songService.updateSong(id, songDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a song identified by its ID.
     *
     * @param id The ID of the song to delete.
     * @return ResponseEntity with status 204 No Content if successful, or 404 Not Found if not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        if (songService.deleteSong(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Fetches all songs by a specific decade and returns them as a list of SongDTOs.
     *
     * @param decade The decade to filter songs by.
     * @return List of SongDTOs representing all songs from the specified decade.
     */
    @GetMapping("/decades/{decade}")
    public List<SongDTO> getSongsByDecade(@PathVariable String decade) {
        return songService.getSongsByDecade(decade);
    }

    /**
     * Fetches popular song IDs with an optional limit and returns them as a list.
     * 
     * @param limit The maximum number of popular songs to retrieve. Optional.
     * @return List of song IDs representing popular songs up to the specified limit.
     */
    @GetMapping("/popular")
    public List<Long> getPopularSongsWithLimit(@RequestParam(required = false) Integer limit) {
        if (limit == null) {
            return songService.getPopularSongs().stream()
                    .map(SongDTO::getId)
                    .collect(java.util.stream.Collectors.toList());
        }
        return songService.getPopularSongsWithLimit(limit);
    }

    /**
     * Fetches all popular songs by a specific genre and returns them as a list of SongDTOs.
     * 
     * @param id The genre to filter popular songs by.
     * @return List of SongDTOs representing all popular songs from the specified genre.
     */
    @GetMapping("/popular/{id}")
    public List<SongDTO> getPopularSongsByGenre(@PathVariable Long id) {
        return songService.getPopularSongsByGenre(id);
    }

    /**
     * Fetches all songs by a specific genre that have the popular flag set to true.
     * Uses optimized database query instead of filtering in memory.
     * 
     * @param genreId The ID of the genre to filter popular songs by.
     * @return List of SongDTOs representing all popular songs from the specified genre.
     */
    @GetMapping("/popular-by-genre/{genreId}")
    public List<SongDTO> getPopularByGenreId(@PathVariable Long genreId) {
        return songService.getPopularByGenreId(genreId);
    }

    /**
     * Fetches all classic songs by a specific genre and returns them as a list of SongDTOs.
     * @TODO: Verify genre_classic flag exists in database.
     * 
     * @param genreId The ID of the genre to filter classic songs by.
     * @return List of SongDTOs representing all classic songs from the specified genre.
     */
    @GetMapping("/classic-by-genre/{genreId}")
    public List<SongDTO> getClassicByGenreId(@PathVariable Long genreId) {
        return songService.getClassicByGenreId(genreId);
    }

    /**
     * Fetches all new songs by a specific genre and returns them as a list of SongDTOs.
     * @TODO: Verify genre_new flag exists in database.
     * 
     * @param genreId The ID of the genre to filter new songs by.
     * @return List of SongDTOs representing all new songs from the specified genre.
     */
    @GetMapping("/new-by-genre/{genreId}")
    public List<SongDTO> getNewByGenreId(@PathVariable Long genreId) {
        return songService.getNewByGenreId(genreId);
    }

    /**
     * Fetches all new songs by a specific genre and returns them as a list of SongDTOs.
     * 
     * @param id The genre to filter new songs by.
     * @return List of SongDTOs representing all new songs from the specified genre.
     */
    @GetMapping("/new/{id}")
    public List<SongDTO> getNewSongsByGenre(@PathVariable Long id) {
        return songService.getNewSongsByGenre(id);
    }

    /**
     * Fetches all classic songs by a specific genre and returns them as a list of SongDTOs.
     * 
     * @param id The genre to filter classic songs by.
     * @return List of SongDTOs representing all classic songs from the specified genre.
     */
    @GetMapping("/classic/{id}")
    public List<SongDTO> getClassicSongsByGenre(@PathVariable Long id) {
        return songService.getClassicSongsByGenre(id);
    }

    /**
     * Fetches all songs by a list of IDs and returns them as a list of SongDTOs.
     * 
     * @param ids The list of song IDs to fetch.
     * @return List of SongDTOs representing all songs with the specified IDs.
     */
    @GetMapping("/ids")
    public List<SongDTO> getSongsByIds(@RequestBody List<Long> ids) {
        return songService.getSongsByIds(ids);
    }

    /**
     * Fetches all popular songs globally and returns them as a list of SongDTOs.
     * @todo Implementar en el backend
     * 
     * @return List of SongDTOs representing all popular songs globally.
     */
    @GetMapping("/home/popular")
    public List<SongDTO> getHomeSongsPopular() {
        return songService.getHomeSongsPopular();
    }

    /**
     * Fetches all new songs globally and returns them as a list of SongDTOs.
     * @todo Implementar en el backend
     * 
     * @return List of SongDTOs representing all new songs globally.
     */
    @GetMapping("/home/new")
    public List<SongDTO> getHomeSongsNew() {
        return songService.getHomeSongsNew();
    }

    /**
     * Fetches all recommended songs for the user and returns them as a list of SongDTOs.
     * @todo Implementar en el backend
     * 
     * @return List of SongDTOs representing recommended songs.
     */
    @GetMapping("/home/recommended")
    public List<SongDTO> getHomeSongsRecommended() {
        return songService.getHomeSongsRecommended();
    }

    /**
     * Fetches all songs from Spotify for the user and returns them as a list of SongDTOs.
     * @todo Implementar en el backend
     * 
     * @return List of SongDTOs representing songs from Spotify.
     */
    @GetMapping("/home/spotify")
    public List<SongDTO> getHomeSongsFromSpotify() {
        return songService.getHomeSongsFromSpotify();
    }
}
