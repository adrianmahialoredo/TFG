package com.demo.demo.service;

import com.demo.demo.dto.UserDto;
import com.demo.demo.dto.UserResponseDTO;
import com.demo.demo.model.Genre;
import com.demo.demo.model.Instrument;
import com.demo.demo.model.Preferences;
import com.demo.demo.dto.LoginResponse;
import com.demo.demo.model.Role;
import com.demo.demo.model.User;
import com.demo.demo.repository.GenreRepository;
import com.demo.demo.repository.InstrumentRepository;
import com.demo.demo.repository.UserRepository;
import com.demo.demo.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
public class UserServiceImpl implements UserService {

    private final String defaultProfileImage = "https://res.cloudinary.com/dvm8qkmuo/image/upload/v1750783736/nopic_qvgygr.jpg";

    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;
    private final GenreRepository genreRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserDto registerUser(UserDto userDto) {
        log.info("Registering new user with email: {}", userDto.getEmail());
        try {
            User user = toEntity(userDto);
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setProfileImageUrl(defaultProfileImage);
            user.setRole(Role.USER);

            User savedUser = userRepository.save(user);
            log.info("User registered successfully with email: {}", userDto.getEmail());

            return toDto(savedUser);
        } catch (Exception e) {
            log.error("Error registering user with email {}: {}", userDto.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public LoginResponse loginUser(UserDto userDto) {
        log.debug("Login attempt for email: {}", userDto.getEmail());
        try {
            User user = userRepository.findByEmail(userDto.getEmail());

            if (user == null) {
                log.warn("Login attempt with non-existent email: {}", userDto.getEmail());
                throw new BadCredentialsException("User not found");
            }
            if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
                log.warn("Failed login attempt for user: {} - password mismatch", userDto.getEmail());
                throw new BadCredentialsException("Password does not match");
            }

            String token = jwtUtil.generateToken(user.getUsername());
            log.info("Successful login for user: {}", userDto.getEmail());

            UserResponseDTO userResponse = toResponseDTO(user);

            return new LoginResponse(token, userResponse);
        } catch (BadCredentialsException e) {
            log.warn("Bad credentials for user: {}", userDto.getEmail());
            throw e;
        } catch (Exception e) {
            log.error("Error during login for user {}: {}", userDto.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Creating new user with email: {}", userDto.getEmail());
        try {
            User user = toEntity(userDto);
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
            if (user.getProfileImageUrl() == null || user.getProfileImageUrl().isEmpty()) {
                user.setProfileImageUrl(defaultProfileImage);
            }
            if (user.getRole() == null) {
                user.setRole(Role.USER);
            }

            User savedUser = userRepository.save(user);
            log.info("User created successfully with email: {}", userDto.getEmail());

            return toDto(savedUser);
        } catch (Exception e) {
            log.error("Error creating user with email {}: {}", userDto.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        log.debug("Searching user by email: {}", email);
        User user = userRepository.findByEmail(email);
        if (user != null) {
            log.debug("User found with email: {}", email);
            return Optional.of(toDto(user));
        }
        log.debug("User not found with email: {}", email);
        return Optional.empty();
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<List<Long>> getFavoriteGenreIdsByUserId(Long userId) {
        log.debug("Fetching favorite genre IDs for user id: {}", userId);
        return userRepository.findById(userId)
                .map(user -> user.getPreferences().getFavoriteGenres().stream()
                        .map(Genre::getId)
                        .collect(Collectors.toList()));
    }

    @Override
    public List<UserDto> getAllUsersByIds(List<Long> ids) {
        log.debug("Fetching users by ids: {}", ids);
        return userRepository.findAllById(ids).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        return userRepository.findById(id)
                .map(this::toDto);
    }

    @Override
    public Optional<UserDto> updateUser(Long id, UserDto updatedUserDto) {
        log.info("Updating user with id: {}", id);
        try {
            Optional<User> existingUserOpt = userRepository.findById(id);
            if (existingUserOpt.isEmpty()) {
                log.warn("User not found for update with id: {}", id);
                return Optional.empty();
            }

            User existingUser = existingUserOpt.get();
            User updatedUser = toEntity(updatedUserDto);

            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setName(updatedUser.getName());
            existingUser.getPreferences().setFavoriteGenres(updatedUser.getPreferences().getFavoriteGenres());
            existingUser.getPreferences().setFavoriteInstruments(updatedUser.getPreferences().getFavoriteInstruments());
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setProfileImageUrl(updatedUser.getProfileImageUrl());

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            if (existingUser.getRole() == null) {
                existingUser.setRole(Role.USER);
            }

            User savedUser = userRepository.save(existingUser);
            log.info("User with id {} updated successfully", id);

            return Optional.of(toDto(savedUser));
        } catch (Exception e) {
            log.error("Error updating user with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean deleteUser(Long id) {
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


    // -------- DTO Mapping --------

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setFavouriteGenresIds(user.getPreferences().getFavoriteGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList()));
        dto.setFavouriteInstrumentsIds(user.getPreferences().getFavoriteInstruments().stream()
                .map(Instrument::getId)
                .collect(Collectors.toList()));
        return dto;
    }

    private UserResponseDTO toResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setFavouriteGenresIds(user.getPreferences().getFavoriteGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList()));
        dto.setFavouriteInstrumentsIds(user.getPreferences().getFavoriteInstruments().stream()
                .map(Instrument::getId)
                .collect(Collectors.toList()));
        return dto;
    }

    private User toEntity(UserDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setName(dto.getName());
        user.setProfileImageUrl(dto.getProfileImageUrl());
        user.setPassword(dto.getPassword());

        Preferences preferences = user.getPreferences();
        preferences.setFavoriteGenres(resolveGenres(dto.getFavouriteGenresIds()));
        preferences.setFavoriteInstruments(resolveInstruments(dto.getFavouriteInstrumentsIds()));
        return user;
    }

    private Set<Genre> resolveGenres(List<Long> ids) {
        return ids == null ? new HashSet<>() : ids.stream()
                .map(id -> genreRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + id)))
                .collect(Collectors.toSet());
    }

    private Set<Instrument> resolveInstruments(List<Long> ids) {
        return ids == null ? new HashSet<>() : ids.stream()
                .map(id -> instrumentRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + id)))
                .collect(Collectors.toSet());
    }
}

