package com.uo289390.toco.service;

import com.uo289390.toco.dto.UserDto;
import com.uo289390.toco.dto.UserResponseDTO;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;
import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.Preferences;
import com.uo289390.toco.dto.LoginResponse;
import com.uo289390.toco.model.Role;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.UserRepository;
import com.uo289390.toco.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.uo289390.toco.utils.EntityResolver.resolveGenre;
import static com.uo289390.toco.utils.EntityResolver.resolveGenres;
import static com.uo289390.toco.utils.EntityResolver.resolveInstrument;
import static com.uo289390.toco.utils.EntityResolver.resolveInstruments;
import static com.uo289390.toco.utils.EntityResolver.findUser;
import static com.uo289390.toco.utils.RecommendationConstants.MAX_TOP_ARTISTS_COUNT;

/**
 * Default implementation of {@link UserService}.
 *
 * Passwords are encrypted before storage, and JWT tokens are generated for authentication.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    /** Range of the self-assessed musical level (same scale as the item stars). */
    static final int MIN_DIFFICULTY_LEVEL = 0;
    static final int MAX_DIFFICULTY_LEVEL = 5;

    /** Login error for both an unknown user and a wrong password. */
    static final String INVALID_CREDENTIALS = "Invalid credentials";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final DtoMapper dtoMapper;
    private final EntityMapper entityMapper;

    @Override
    @Transactional
    public UserDto register(UserDto userDto) {
        log.info("Registering new user with email: {}", userDto.getEmail());

        // Checked before saving: the unique constraints would end in a generic 500
        if (userDto.getEmail() != null && userRepository.existsByEmailIgnoreCase(userDto.getEmail().trim())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }

        try {
            User user = entityMapper.userDtoToUser(
                    userDto,
                    resolveGenres(userDto.getFavouriteGenresIds()),
                    resolveInstruments(userDto.getFavouriteInstrumentsIds())
            );
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setRole(Role.USER);

            User savedUser = userRepository.save(user);
            log.info("User registered successfully with email: {}", userDto.getEmail());

            return dtoMapper.userToUserDto(savedUser);
        } catch (Exception e) {
            log.error("Error registering user with email {}: {}", userDto.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public LoginResponse login(String identifier, String password) {
        String login = identifier == null ? "" : identifier.trim();
        log.debug("Login attempt for: {}", login);
        try {
            // Email first: a username is free text and could look like an email.
            // Same message for an unknown user and a wrong password, so the response
            // does not reveal which accounts exist.
            User user = userRepository.findByEmail(login)
                    .or(() -> userRepository.findByUsername(login))
                    .orElseThrow(() -> {
                        log.warn("Login attempt with non-existent email or username: {}", login);
                        return new BadCredentialsException(INVALID_CREDENTIALS);
                    });
            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("Failed login attempt for user: {} - password mismatch", login);
                throw new BadCredentialsException(INVALID_CREDENTIALS);
            }

            String token = jwtUtil.generateToken(user.getId(), user.getRole());
            log.info("Successful login for user: {}", login);

            UserResponseDTO userResponse = dtoMapper.userToUserResponseDto(user);

            return new LoginResponse(token, userResponse);
        } catch (BadCredentialsException e) {
            log.warn("Bad credentials for user: {}", login);
            throw e;
        } catch (Exception e) {
            log.error("Error during login for user {}: {}", login, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("Creating new user with email: {}", userDto.getEmail());
        try {
            User user = entityMapper.userDtoToUser(
                    userDto,
                    resolveGenres(userDto.getFavouriteGenresIds()),
                    resolveInstruments(userDto.getFavouriteInstrumentsIds())
            );
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
            if (user.getRole() == null) {
                user.setRole(Role.USER);
            }

            User savedUser = userRepository.save(user);
            log.info("User created successfully with email: {}", userDto.getEmail());

            return dtoMapper.userToUserDto(savedUser);
        } catch (Exception e) {
            log.error("Error creating user with email {}: {}", userDto.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        log.debug("Searching user by email: {}", email);
        Optional<UserDto> user = userRepository.findByEmail(email)
                .map(dtoMapper::userToUserDto);
        user.ifPresentOrElse(
                found -> log.debug("User found with email: {}", email),
                () -> log.debug("User not found with email: {}", email));
        return user;
    }

    @Override
    public List<UserDto> findAll() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(dtoMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<List<Long>> findFavoriteGenreIdsForUser(Long userId) {
        log.debug("Fetching favorite genre IDs for user id: {}", userId);
        return findUser(userId)
                .map(user -> user.getPreferences().getFavoriteGenres().stream()
                        .map(Genre::getId)
                        .collect(Collectors.toList()));
    }

    @Override
    public Optional<List<String>> findTopArtistsForUser(Long userId, int totalArtists) {
        if (totalArtists < 1 || totalArtists > MAX_TOP_ARTISTS_COUNT) {
            throw new IllegalArgumentException("total_artists must be between 1 and " + MAX_TOP_ARTISTS_COUNT);
        }
        log.debug("Fetching the top {} artists for user id: {}", totalArtists, userId);
        return findUser(userId).map(user -> user.getTopArtistsFromItemLists(totalArtists));
    }

    @Override
    public Optional<List<Long>> findFavoriteInstrumentIdsForUser(Long userId) {
        log.debug("Fetching favorite instrument IDs for user id: {}", userId);
        return findUser(userId)
                .map(user -> user.getPreferences().getFavoriteInstruments().stream()
                        .map(Instrument::getId)
                        .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public Optional<UserDto> addFavoriteGenre(Long userId, Long genreId) {
        log.info("Adding genre {} to the favourites of user {}", genreId, userId);
        return findUser(userId).map(user -> {
            // Set: adding a genre that is already a favourite has no effect
            user.getPreferences().getFavoriteGenres().add(resolveGenre(genreId));
            return dtoMapper.userToUserDto(userRepository.save(user));
        });
    }

    @Override
    @Transactional
    public Optional<UserDto> removeFavoriteGenre(Long userId, Long genreId) {
        log.info("Removing genre {} from the favourites of user {}", genreId, userId);
        return findUser(userId).map(user -> {
            Set<Genre> favourites = user.getPreferences().getFavoriteGenres();
            boolean isFavourite = favourites.stream().anyMatch(genre -> genre.getId().equals(genreId));
            if (isFavourite && favourites.size() <= Preferences.MIN_NUMBER_OF_FAVOURITE_GENRES) {
                throw new IllegalArgumentException(
                        "A user must have at least " + Preferences.MIN_NUMBER_OF_FAVOURITE_GENRES + " favourite genres");
            }
            favourites.removeIf(genre -> genre.getId().equals(genreId));
            return dtoMapper.userToUserDto(userRepository.save(user));
        });
    }

    @Override
    @Transactional
    public Optional<UserDto> addFavoriteInstrument(Long userId, Long instrumentId) {
        log.info("Adding instrument {} to the favourites of user {}", instrumentId, userId);
        return findUser(userId).map(user -> {
            user.getPreferences().getFavoriteInstruments().add(resolveInstrument(instrumentId));
            return dtoMapper.userToUserDto(userRepository.save(user));
        });
    }

    @Override
    @Transactional
    public Optional<UserDto> removeFavoriteInstrument(Long userId, Long instrumentId) {
        log.info("Removing instrument {} from the favourites of user {}", instrumentId, userId);
        return findUser(userId).map(user -> {
            Set<Instrument> favourites = user.getPreferences().getFavoriteInstruments();
            boolean isFavourite = favourites.stream().anyMatch(instrument -> instrument.getId().equals(instrumentId));
            if (isFavourite && favourites.size() <= Preferences.MIN_NUMBER_OF_FAVOURITE_INSTRUMENTS) {
                throw new IllegalArgumentException(
                        "A user must have at least " + Preferences.MIN_NUMBER_OF_FAVOURITE_INSTRUMENTS + " favourite instruments");
            }
            favourites.removeIf(instrument -> instrument.getId().equals(instrumentId));
            return dtoMapper.userToUserDto(userRepository.save(user));
        });
    }

    @Override
    public List<UserDto> findByIds(List<Long> ids) {
        log.debug("Fetching users by ids: {}", ids);
        return userRepository.findAllById(ids).stream()
                .map(dtoMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        log.debug("Fetching user by id: {}", id);
        return findUser(id)
                .map(dtoMapper::userToUserDto);
    }

    @Override
    @Transactional
    public Optional<UserDto> update(Long id, UserDto updatedUserDto) {
        log.info("Updating user with id: {}", id);
        try {
            return findUser(id)
                    .map(existingUser -> {
                        ensureUsernameAvailable(id, updatedUserDto);

                        User updatedUser = entityMapper.userDtoToUser(
                                updatedUserDto,
                                resolveGenres(updatedUserDto.getFavouriteGenresIds()),
                                resolveInstruments(updatedUserDto.getFavouriteInstrumentsIds())
                        );
                        ensureMinimumFavourites(updatedUser.getPreferences());

                        // The email is not updated: it was verified with a code at registration
                        existingUser.setName(updatedUser.getName());
                        existingUser.getPreferences().setFavoriteGenres(updatedUser.getPreferences().getFavoriteGenres());
                        existingUser.getPreferences().setFavoriteInstruments(updatedUser.getPreferences().getFavoriteInstruments());
                        existingUser.setUsername(updatedUser.getUsername());
                        existingUser.setProfileImageUrl(updatedUser.getProfileImageUrl());
                        Optional.ofNullable(updatedUserDto.getDifficultyLevel())
                                .map(UserServiceImpl::validDifficultyLevel)
                                .ifPresent(existingUser::setDifficultyLevel);

                        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                        }

                        if (existingUser.getRole() == null) {
                            existingUser.setRole(Role.USER);
                        }

                        User savedUser = userRepository.save(existingUser);
                        log.info("User with id {} updated successfully", id);

                        return dtoMapper.userToUserDto(savedUser);
                    })
                    .or(() -> {
                        log.warn("User not found for update with id: {}", id);
                        return Optional.empty();
                    });
        } catch (Exception e) {
            log.error("Error updating user with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        log.info("Deleting user with id: {}", id);
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                log.info("User with id {} deleted successfully", id);
                return true;
            }
            log.warn("User not found for deletion with id: {}", id);
            return false;
        } catch (Exception e) {
            log.error("Error deleting user with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }


    // -------- Validation helpers --------

    /**
     * Checks that the email and username of the update are not used by another user,
     * like {@link #register(UserDto)} does: the unique constraints would end in a generic 500.
     */
    private void ensureUsernameAvailable(Long userId, UserDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).filter(other -> !other.getId().equals(userId)).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
    }

    /**
     * Enforces the minimum number of favourites on the resolved (deduplicated) sets,
     * the same rule applied when removing a single favourite.
     */
    private void ensureMinimumFavourites(Preferences preferences) {
        if (preferences.getFavoriteGenres().size() < Preferences.MIN_NUMBER_OF_FAVOURITE_GENRES) {
            throw new IllegalArgumentException(
                    "A user must have at least " + Preferences.MIN_NUMBER_OF_FAVOURITE_GENRES + " favourite genres");
        }
        if (preferences.getFavoriteInstruments().size() < Preferences.MIN_NUMBER_OF_FAVOURITE_INSTRUMENTS) {
            throw new IllegalArgumentException(
                    "A user must have at least " + Preferences.MIN_NUMBER_OF_FAVOURITE_INSTRUMENTS + " favourite instruments");
        }
    }

    /**
     * Checks the self-assessed level sent from the profile (0 to 5, like the item stars).
     * {@code @Valid} is not active on this endpoint, so the range is enforced here (400 if not).
     */
    private static int validDifficultyLevel(int level) {
        if (level < MIN_DIFFICULTY_LEVEL || level > MAX_DIFFICULTY_LEVEL) {
            throw new IllegalArgumentException("Difficulty level must be between "
                    + MIN_DIFFICULTY_LEVEL + " and " + MAX_DIFFICULTY_LEVEL);
        }
        return level;
    }

    // -------- DTO Mapping --------

}

