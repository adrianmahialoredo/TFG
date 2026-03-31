package com.demo.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.demo.demo.dto.UserDto;
import com.demo.demo.model.LoginResponse;
import com.demo.demo.model.Role;
import com.demo.demo.model.User;
import com.demo.demo.repository.GenreRepository;
import com.demo.demo.repository.InstrumentRepository;
import com.demo.demo.repository.UserRepository;
import com.demo.demo.security.JwtUtil;

/**
 * Unit tests for UserService class.
 * Tests CRUD operations, authentication, and business logic.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("hashedPassword123");
        testUser.setName("Test User");
        testUser.setRole(Role.USER);

        testUserDto = new UserDto();
        testUserDto.setEmail("test@example.com");
        testUserDto.setUsername("testuser");
        testUserDto.setPassword("password123");
        testUserDto.setName("Test User");
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = userService.registerUser(testUserDto);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void testLoginUser_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);
        when(passwordEncoder.matches("password123", "hashedPassword123")).thenReturn(true);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt-token-123");

        // Act
        LoginResponse result = userService.loginUser(testUserDto);

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token-123", result.getToken());
        assertNotNull(result.getUser());
        assertEquals("test@example.com", result.getUser().getEmail());
    }

    @Test
    void testLoginUser_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        testUserDto.setEmail("nonexistent@example.com");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> userService.loginUser(testUserDto));
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void testLoginUser_IncorrectPassword() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpassword", "hashedPassword123")).thenReturn(false);

        testUserDto.setPassword("wrongpassword");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> userService.loginUser(testUserDto));
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("wrongpassword", "hashedPassword123");
    }

    @Test
    void testFindByEmail_Found() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

        // Act
        Optional<UserDto> result = userService.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testFindByEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        // Act
        Optional<UserDto> result = userService.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserDto result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(999L));
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        UserDto updateDto = new UserDto();
        updateDto.setEmail("updated@example.com");
        updateDto.setName("Updated User");
        updateDto.setUsername("updateduser");
        updateDto.setPassword("newpassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = userService.updateUser(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testDeleteUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void testDeleteUser_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(999L));
        verify(userRepository, times(1)).findById(999L);
    }
}

