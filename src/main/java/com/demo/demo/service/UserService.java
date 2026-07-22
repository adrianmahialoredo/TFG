package com.demo.demo.service;

import com.demo.demo.dto.UserDto;
import com.demo.demo.dto.LoginResponse;

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

    UserDto registerUser(UserDto userDto);

    LoginResponse loginUser(UserDto userDto);

    UserDto createUser(UserDto userDto);

    Optional<UserDto> findByEmail(String email);

    List<UserDto> getAllUsers();

    Optional<List<Long>> getFavoriteGenreIdsByUserId(Long userId);

    List<UserDto> getAllUsersByIds(List<Long> ids);

    Optional<UserDto> getUserById(Long id);

    Optional<UserDto> updateUser(Long id, UserDto updatedUserDto);

    boolean deleteUser(Long id);
}
