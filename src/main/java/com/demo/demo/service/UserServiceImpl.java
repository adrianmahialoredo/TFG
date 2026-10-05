package com.demo.demo.service;

import com.demo.demo.dto.UserDto;
import com.demo.demo.dto.UserResponseDTO;
import com.demo.demo.utils.mapper.DtoMapper;
import com.demo.demo.utils.mapper.EntityMapper;
import com.demo.demo.model.Genre;
import com.demo.demo.dto.LoginResponse;
import com.demo.demo.model.Role;
import com.demo.demo.model.User;
import com.demo.demo.repository.UserRepository;
import com.demo.demo.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.demo.demo.utils.EntityResolver.resolveGenres;
import static com.demo.demo.utils.EntityResolver.resolveInstruments;
import static com.demo.demo.utils.EntityResolver.findUser;

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

    private final String defaultProfileImage = "https://res.cloudinary.com/dvm8qkmuo/image/upload/v1750783736/nopic_qvgygr.jpg";

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
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
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
            user.setProfileImageUrl(defaultProfileImage);
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
    public LoginResponse login(String email, String password) {
        log.debug("Login attempt for email: {}", email);
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Login attempt with non-existent email: {}", email);
                        return new BadCredentialsException("User not found");
                    });
            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("Failed login attempt for user: {} - password mismatch", email);
                throw new BadCredentialsException("Password does not match");
            }

            String token = jwtUtil.generateToken(user.getUsername());
            log.info("Successful login for user: {}", email);

            UserResponseDTO userResponse = dtoMapper.userToUserResponseDto(user);

            return new LoginResponse(token, userResponse);
        } catch (BadCredentialsException e) {
            log.warn("Bad credentials for user: {}", email);
            throw e;
        } catch (Exception e) {
            log.error("Error during login for user {}: {}", email, e.getMessage(), e);
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
            if (user.getProfileImageUrl() == null || user.getProfileImageUrl().isEmpty()) {
                user.setProfileImageUrl(defaultProfileImage);
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
                        User updatedUser = entityMapper.userDtoToUser(
                                updatedUserDto,
                                resolveGenres(updatedUserDto.getFavouriteGenresIds()),
                                resolveInstruments(updatedUserDto.getFavouriteInstrumentsIds())
                        );

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


    // -------- DTO Mapping --------

}

