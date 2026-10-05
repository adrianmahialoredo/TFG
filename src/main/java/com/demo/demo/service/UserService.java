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

    UserDto register(UserDto userDto);

    LoginResponse login(String email, String password);

    UserDto create(UserDto userDto);

    Optional<UserDto> findByEmail(String email);

    List<UserDto> findAll();

    Optional<List<Long>> findFavoriteGenreIdsForUser(Long userId);

    List<UserDto> findByIds(List<Long> ids);

    Optional<UserDto> findById(Long id);

    Optional<UserDto> update(Long id, UserDto updatedUserDto);

    boolean delete(Long id);
}
