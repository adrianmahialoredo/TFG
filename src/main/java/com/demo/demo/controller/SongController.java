package com.demo.demo.controller;

import com.demo.demo.dto.SongDTO;
import com.demo.demo.service.SongService;
import com.demo.demo.dto.validation.OnCreate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * REST controller for songs.
 *
 * Base path: {@code /api/songs}
 *
 * Endpoints:
 * - GET    /api/songs                            Retrieve all songs
 * - POST   /api/songs/by-ids                     Retrieve the songs with the given IDs (IDs in the body)
 * - GET    /api/songs/{id}                       Retrieve a song by ID
 * - GET    /api/songs/by-decade/{decade}         Retrieve the songs of a decade (e.g. "1990s")
 * - GET    /api/songs/by-genre/{genreId}/popular Retrieve the popular songs of a genre
 * - GET    /api/songs/by-genre/{genreId}/classic Retrieve the classic songs of a genre
 * - GET    /api/songs/by-genre/{genreId}/new     Retrieve the new songs of a genre
 * - GET    /api/songs/popular?limit={n}          Retrieve the popular songs
 * - GET    /api/songs/new?limit={n}              Retrieve the new songs
 * - GET    /api/songs/recommended/ids?limit={n}  Retrieve the IDs of the recommended songs for the user (not implemented yet)
 * - GET    /api/songs/spotify?limit={n}          Retrieve the songs recommended by Spotify
 * - POST   /api/songs                            Create a song
 * - PUT    /api/songs/{id}                       Update a song
 * - DELETE /api/songs/{id}                       Delete a song
 *
 * The {@code limit} parameter is optional; without it every matching song is returned.
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the song does not exist. Other errors are translated by
 * {@link com.demo.demo.exception.GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    /**
     * Retrieves all songs.
     *
     * @return 200 OK with the list of songs
     */
    @GetMapping
    public ResponseEntity<List<SongDTO>> getAll() {
        return ResponseEntity.ok(songService.findAll());
    }

    /**
     * Retrieves the songs with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the songs
     * @return 200 OK with the songs found
     */
    @PostMapping("/by-ids")
    public ResponseEntity<List<SongDTO>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(songService.findByIds(ids));
    }

    /**
     * Retrieves a song by its ID.
     *
     * @param id the ID of the song
     * @return 200 OK with the song, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(songService.findById(id));
    }

    /**
     * Retrieves the songs of a decade.
     *
     * @param decade the decade, in the format "1990s"
     * @return 200 OK with the songs of the decade
     */
    @GetMapping("/by-decade/{decade}")
    public ResponseEntity<List<SongDTO>> getByDecade(@PathVariable String decade) {
        return ResponseEntity.ok(songService.findByDecade(decade));
    }

    /**
     * Retrieves the popular songs of a genre.
     *
     * @param genreId the ID of the genre
     * @return 200 OK with the popular songs of the genre
     */
    @GetMapping("/by-genre/{genreId}/popular")
    public ResponseEntity<List<SongDTO>> getPopularForGenre(@PathVariable Long genreId) {
        return ResponseEntity.ok(songService.findPopularForGenre(genreId));
    }

    /**
     * Retrieves the classic songs of a genre.
     *
     * @param genreId the ID of the genre
     * @return 200 OK with the classic songs of the genre
     */
    @GetMapping("/by-genre/{genreId}/classic")
    public ResponseEntity<List<SongDTO>> getClassicForGenre(@PathVariable Long genreId) {
        return ResponseEntity.ok(songService.findClassicForGenre(genreId));
    }

    /**
     * Retrieves the new songs of a genre.
     *
     * @param genreId the ID of the genre
     * @return 200 OK with the new songs of the genre
     */
    @GetMapping("/by-genre/{genreId}/new")
    public ResponseEntity<List<SongDTO>> getNewForGenre(@PathVariable Long genreId) {
        return ResponseEntity.ok(songService.findNewForGenre(genreId));
    }

    /**
     * Retrieves the popular songs.
     *
     * @param limit maximum number of songs (optional)
     * @return 200 OK with the popular songs
     */
    @GetMapping("/popular")
    public ResponseEntity<List<SongDTO>> getPopular(@RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(songService.findPopular(limit));
    }

    /**
     * Retrieves the new songs.
     *
     * @param limit maximum number of songs (optional)
     * @return 200 OK with the new songs
     */
    @GetMapping("/new")
    public ResponseEntity<List<SongDTO>> getNew(@RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(songService.findNew(limit));
    }

    /**
     * Creates a song, with its lessons and tab sections.
     *
     * @param dto the song to create
     * @return 201 Created with the created song
     */
    @PostMapping
    public ResponseEntity<SongDTO> create(@Validated(OnCreate.class) @RequestBody SongDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(songService.create(dto));
    }

    /**
     * Updates a song.
     *
     * @param id  the ID of the song
     * @param dto the new data of the song
     * @return 200 OK with the updated song, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<SongDTO> update(@PathVariable Long id, @Valid @RequestBody SongDTO dto) {
        return ResponseEntity.of(songService.update(id, dto));
    }

    /**
     * Deletes a song.
     *
     * @param id the ID of the song
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return songService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
