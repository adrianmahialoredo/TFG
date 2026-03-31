package com.demo.demo.controller;

import com.demo.demo.dto.UserDto;
import com.demo.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller for managing users.
 * 
 * Provides endpoints to create, read, update, and delete users.
 * Also allows retrieval of all users or a specific user by ID.
 * 
 * This controller uses the UserService to handle business logic
 * and convert entities to DTOs for API responses.
 * 
 * Endpoints:
 * - GET /users: Fetch all users
 * - GET /users/{id}: Fetch a specific user by ID
 * - POST /users: Create a new user
 * - PUT /users/{id}: Update an existing user by ID
 * - DELETE /users/{id}: Delete a user by ID
 * 
 * This controller is annotated with @RestController to handle RESTful requests
 * and @RequestMapping to define the base path for all endpoints.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Endpoint to retrieve all users.
     *
     * @return List of UserDto
     */
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Retrieves all favorite genre IDs associated with a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of favorite genre IDs for the user, or 404 if user not found.
     */
    @GetMapping("/{userId}/favorite-genres")
    public ResponseEntity<List<Long>> getFavoriteGenreIdsByUserId(@PathVariable Long userId) {
        return userService.getFavoriteGenreIdsByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all users by a list of IDs.
     *
     * @param ids The list of user IDs to retrieve.
     * @return A list of UserDTOs representing all users with the provided IDs.
     */
    @GetMapping("/by-ids")
    public List<UserDto> getAllUsersByIds(@RequestParam List<Long> ids) {
        return userService.getAllUsersByIds(ids);
    }

    /**
     * Endpoint to retrieve a user by its ID.
     *
     * @param id the ID of the user
     * @return ResponseEntity containing UserDto or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to create a new user.
     *
     * @param userDto the DTO containing user details
     * @return ResponseEntity containing the created UserDto
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto created = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Endpoint to update an existing user.
     *
     * @param id the ID of the user to update
     * @param updatedUser the DTO containing updated user details
     * @return ResponseEntity containing the updated UserDto or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto updatedUser) {
        return userService.updateUser(id, updatedUser)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to delete a user by its ID.
     *
     * @param id the ID of the user to delete
     * @return ResponseEntity with no content if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
