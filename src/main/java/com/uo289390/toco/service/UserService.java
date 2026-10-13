package com.uo289390.toco.service;

import com.uo289390.toco.dto.UserDto;
import com.uo289390.toco.dto.LoginResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing users.
 *
 * Provides methods for user registration, login, retrieval, update, and deletion.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-24
 */
public interface UserService {

    UserDto register(UserDto userDto);

    LoginResponse login(String email, String password);

    UserDto create(UserDto userDto);

    Optional<UserDto> findByEmail(String email);

    List<UserDto> findAll();

    Optional<List<Long>> findFavoriteGenreIdsForUser(Long userId);

    Optional<List<Long>> findFavoriteInstrumentIdsForUser(Long userId);

    Optional<UserDto> addFavoriteGenre(Long userId, Long genreId);

    Optional<UserDto> removeFavoriteGenre(Long userId, Long genreId);

    Optional<UserDto> addFavoriteInstrument(Long userId, Long instrumentId);

    Optional<UserDto> removeFavoriteInstrument(Long userId, Long instrumentId);

    List<UserDto> findByIds(List<Long> ids);

    Optional<UserDto> findById(Long id);

    /**
     * Replaces the profile and favourites of a user. The email is never changed (the one in the
     * DTO is ignored): it was verified with a code at registration.
     *
     * @param id             The ID of the user to update.
     * @param updatedUserDto The new data of the user.
     * @return The updated user, or empty if it does not exist.
     * @throws IllegalArgumentException if the username belongs to another user or the favourites are invalid.
     */
    Optional<UserDto> update(Long id, UserDto updatedUserDto);

    boolean delete(Long id);
}
