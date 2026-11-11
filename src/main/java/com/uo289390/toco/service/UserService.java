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

    /**
     * Logs a user in with their email or their username.
     * The identifier is looked up as an email first and then as a username.
     *
     * @param identifier the email or the username of the user
     * @param password   the raw password
     * @return the JWT and the user
     * @throws org.springframework.security.authentication.BadCredentialsException if no user
     *         matches or the password is wrong (the same error in both cases)
     */
    LoginResponse login(String identifier, String password);

    /**
     * Replaces the password of the account with that email ("Olvidé mi contraseña").
     * The caller must have checked the password reset code first.
     *
     * @param email       the email of the account (case does not matter)
     * @param newPassword the new raw password, already validated
     * @throws IllegalArgumentException if no account has that email
     */
    void resetPassword(String email, String newPassword);

    UserDto create(UserDto userDto);

    Optional<UserDto> findByEmail(String email);

    List<UserDto> findAll();

    Optional<List<Long>> findFavoriteGenreIdsForUser(Long userId);

    Optional<List<Long>> findFavoriteInstrumentIdsForUser(Long userId);

    /**
     * Retrieves the favourite artists of a user: the artists that appear most in the songs of
     * their lists (see {@link com.uo289390.toco.model.User#getTopArtistsFromItemLists(int)}).
     *
     * @param userId       the ID of the user
     * @param totalArtists how many artists to return (1 to {@code MAX_N_TOP_ARTISTS})
     * @return the artist names from most to least repeated (empty if the lists have no songs),
     *         or empty if the user does not exist
     * @throws IllegalArgumentException if {@code totalArtists} is out of range
     */
    Optional<List<String>> findTopArtistsForUser(Long userId, int totalArtists);

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
