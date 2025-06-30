package com.demo.demo.service;

import com.demo.demo.dto.UserDto;
import com.demo.demo.dto.UserResponseDTO;
import com.demo.demo.model.Genre;
import com.demo.demo.model.Instrument;
import com.demo.demo.model.LoginResponse;
import com.demo.demo.model.Role;
import com.demo.demo.model.User;
import com.demo.demo.repository.GenreRepository;
import com.demo.demo.repository.InstrumentRepository;
import com.demo.demo.repository.UserRepository;
import com.demo.demo.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 * 
 * Provides methods for user registration, login, retrieval, update, and deletion.
 * Passwords are encrypted before storage, and JWT tokens are generated for authentication.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-24
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final String defaultProfileImage = "https://res.cloudinary.com/dvm8qkmuo/image/upload/v1750783736/nopic_qvgygr.jpg";

    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;
    private final GenreRepository genreRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserDto registerUser(UserDto userDto) {
        User user = toEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setProfileImageUrl(defaultProfileImage);
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return toDto(savedUser);
    }

    public LoginResponse loginUser(UserDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail());

        if (user == null) {
            throw new BadCredentialsException("User not found");
        }
        if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Password does not match");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        UserResponseDTO userResponse = toResponseDTO(user);

        return new LoginResponse(token, userResponse);
    }

    public UserDto findByEmail(String email) {
        User user = userRepository.findByEmail(email);

        return user != null ? toDto(user) : null;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return toDto(user);
    }

    public UserDto updateUser(Long id, UserDto updatedUserDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User updatedUser = toEntity(updatedUserDto);

        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setName(updatedUser.getName());
        existingUser.setFavoriteGenres(updatedUser.getFavoriteGenres());
        existingUser.setFavoriteInstruments(updatedUser.getFavoriteInstruments());
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setProfileImageUrl(updatedUser.getProfileImageUrl());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        if (existingUser.getRole() == null) {
            existingUser.setRole(Role.USER);
        }

        User savedUser = userRepository.save(existingUser);

        return toDto(savedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userRepository.delete(user);
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    // -------- DTO Mapping --------

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setFavouriteGenresIds(user.getFavoriteGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList()));
        dto.setFavouriteInstrumentsIds(user.getFavoriteInstruments().stream()
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
        dto.setFavouriteGenresIds(user.getFavoriteGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList()));
        dto.setFavouriteInstrumentsIds(user.getFavoriteInstruments().stream()
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
        user.setFavoriteGenres(resolveGenres(dto.getFavouriteGenresIds()));
        user.setFavoriteInstruments(resolveInstruments(dto.getFavouriteInstrumentsIds()));
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
