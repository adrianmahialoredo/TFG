package com.demo.demo.controller;

import com.demo.demo.dto.UserDto;
import com.demo.demo.service.UserService;
import com.demo.demo.dto.validation.OnCreate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * REST controller for users.
 *
 * Base path: {@code /api/users}
 *
 * Endpoints:
 * - GET    /api/users                      Retrieve all users
 * - POST   /api/users/by-ids               Retrieve the users with the given IDs (IDs in the body)
 * - GET    /api/users/{id}                 Retrieve a user by ID
 * - GET    /api/users/{id}/favorite-genres Retrieve the IDs of the favourite genres of a user
 * - POST   /api/users                      Create a user
 * - PUT    /api/users/{id}                 Update a user
 * - DELETE /api/users/{id}                 Delete a user
 *
 * Registration and login live in {@link AuthController}.
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the user does not exist. Other errors are translated by
 * {@link com.demo.demo.exception.GlobalExceptionHandler}.
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
    public ResponseEntity<List<Long>> getFavoriteGenreIdsForUser(@PathVariable Long id) {
        return ResponseEntity.of(userService.findFavoriteGenreIdsForUser(id));
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return userService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
