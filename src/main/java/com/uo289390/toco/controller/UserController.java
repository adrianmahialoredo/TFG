package com.uo289390.toco.controller;

import com.uo289390.toco.dto.UserDto;
import com.uo289390.toco.model.Preferences;
import com.uo289390.toco.service.UserService;
import com.uo289390.toco.dto.validation.OnCreate;
import com.uo289390.toco.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.uo289390.toco.utils.RecommendationConstants.DEFAULT_TOP_ARTISTS_COUNT;

/**
 * REST controller for users.
 *
 * Base path: {@code /api/users}
 *
 * Endpoints:
 * - GET    /api/users                                              Retrieve all users
 * - POST   /api/users/by-ids                                       Retrieve the users with the given IDs (IDs in the body)
 * - GET    /api/users/{id}                                         Retrieve a user by ID
 * - GET    /api/users/{id}/favorite-genres                         Retrieve the IDs of the favourite genres of a user
 * - POST   /api/users/{id}/favorite-genres/{genreId}               Add a genre to the favourites of a user
 * - DELETE /api/users/{id}/favorite-genres/{genreId}               Remove a genre from the favourites of a user
 * - GET    /api/users/{id}/favorite-instruments                    Retrieve the IDs of the favourite instruments of a user
 * - POST   /api/users/{id}/favorite-instruments/{instrumentId}     Add an instrument to the favourites of a user
 * - DELETE /api/users/{id}/favorite-instruments/{instrumentId}     Remove an instrument from the favourites of a user
 * - GET    /api/users/{id}/top-artists?total_artists={n}           Retrieve the favourite artists of a user (from the songs of their lists)
 * - POST   /api/users                                              Create a user
 * - PUT    /api/users/{id}                                         Update a user
 * - DELETE /api/users/{id}                                         Delete a user
 *
 * The favourite genres and instruments also travel inside the user
 * ({@code favouriteGenresIds} / {@code favouriteInstrumentsIds}).
 *
 * Registration and login live in {@link AuthController}.
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the user does not exist. Other errors are translated by
 * {@link GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves all users.
     *
     * @return 200 OK with the list of users
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * Retrieves the users with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the users
     * @return 200 OK with the users found
     */
    @PostMapping("/by-ids")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isOnlySelf(#ids, authentication)")
    public ResponseEntity<List<UserDto>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(userService.findByIds(ids));
    }

    /**
     * Retrieves a user by its ID.
     *
     * @param id the ID of the user
     * @return 200 OK with the user, or 404 Not Found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.of(userService.findById(id));
    }

    /**
     * Retrieves the IDs of the favourite genres of a user.
     *
     * @param id the ID of the user
     * @return 200 OK with the genre IDs, or 404 Not Found if the user does not exist
     */
    @GetMapping("/{id}/favorite-genres")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<List<Long>> getFavoriteGenreIdsForUser(@PathVariable Long id) {
        return ResponseEntity.of(userService.findFavoriteGenreIdsForUser(id));
    }

    /**
     * Adds a genre to the favourite genres of a user. Adding a genre that is already a favourite has no effect.
     *
     * @param id      the ID of the user
     * @param genreId the ID of the genre to add
     * @return 200 OK with the updated user, or 404 Not Found if the user or the genre does not exist
     */
    @PostMapping("/{id}/favorite-genres/{genreId}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<UserDto> addFavoriteGenre(@PathVariable Long id, @PathVariable Long genreId) {
        return ResponseEntity.of(userService.addFavoriteGenre(id, genreId));
    }

    /**
     * Removes a genre from the favourite genres of a user.
     *
     * @param id      the ID of the user
     * @param genreId the ID of the genre to remove
     * @return 200 OK with the updated user, 400 Bad Request if the user would be left with fewer than
     *         {@link Preferences#MIN_NUMBER_OF_FAVOURITE_GENRES} favourite genres,
     *         or 404 Not Found if the user does not exist
     */
    @DeleteMapping("/{id}/favorite-genres/{genreId}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<UserDto> removeFavoriteGenre(@PathVariable Long id, @PathVariable Long genreId) {
        return ResponseEntity.of(userService.removeFavoriteGenre(id, genreId));
    }

    /**
     * Retrieves the IDs of the favourite instruments of a user.
     *
     * @param id the ID of the user
     * @return 200 OK with the instrument IDs, or 404 Not Found if the user does not exist
     */
    @GetMapping("/{id}/favorite-instruments")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<List<Long>> getFavoriteInstrumentIdsForUser(@PathVariable Long id) {
        return ResponseEntity.of(userService.findFavoriteInstrumentIdsForUser(id));
    }

    /**
     * Retrieves the favourite artists of a user: the artists that appear most in the songs of their lists.
     *
     * @param id           the ID of the user
     * @param totalArtists how many artists to return (1 to 10, 5 by default)
     * @return 200 OK with the artist names from most to least repeated (empty if the lists have no songs),
     *         400 Bad Request if {@code total_artists} is out of range, or 404 Not Found if the user does not exist
     */
    @GetMapping("/{id}/top-artists")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<List<String>> getTopArtistsForUser(
            @PathVariable Long id,
            @RequestParam(name = "total_artists", defaultValue = "" + DEFAULT_TOP_ARTISTS_COUNT) int totalArtists) {
        return ResponseEntity.of(userService.findTopArtistsForUser(id, totalArtists));
    }

    /**
     * Adds an instrument to the favourite instruments of a user. Adding an instrument that is already a favourite has no effect.
     *
     * @param id           the ID of the user
     * @param instrumentId the ID of the instrument to add
     * @return 200 OK with the updated user, or 404 Not Found if the user or the instrument does not exist
     */
    @PostMapping("/{id}/favorite-instruments/{instrumentId}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<UserDto> addFavoriteInstrument(@PathVariable Long id, @PathVariable Long instrumentId) {
        return ResponseEntity.of(userService.addFavoriteInstrument(id, instrumentId));
    }

    /**
     * Removes an instrument from the favourite instruments of a user.
     *
     * @param id           the ID of the user
     * @param instrumentId the ID of the instrument to remove
     * @return 200 OK with the updated user, 400 Bad Request if the user would be left with fewer than
     *         {@link Preferences#MIN_NUMBER_OF_FAVOURITE_INSTRUMENTS} favourite instruments,
     *         or 404 Not Found if the user does not exist
     */
    @DeleteMapping("/{id}/favorite-instruments/{instrumentId}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<UserDto> removeFavoriteInstrument(@PathVariable Long id, @PathVariable Long instrumentId) {
        return ResponseEntity.of(userService.removeFavoriteInstrument(id, instrumentId));
    }

    /**
     * Creates a user.
     *
     * @param dto the user to create
     * @return 201 Created with the created user
     */
    @PostMapping
    public ResponseEntity<UserDto> create(@Validated(OnCreate.class) @RequestBody UserDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    /**
     * Updates a user.
     *
     * @param id  the ID of the user
     * @param dto the new data of the user
     * @return 200 OK with the updated user, or 404 Not Found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        return ResponseEntity.of(userService.update(id, dto));
    }

    /**
     * Deletes a user.
     *
     * @param id the ID of the user
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return userService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
