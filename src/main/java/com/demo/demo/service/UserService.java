package com.demo.demo.service;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.demo.demo.dto.UserDto;
import com.demo.demo.model.User;
import com.demo.demo.model.Role;
import com.demo.demo.repository.UserRepository;
import com.demo.demo.security.JwtUtil;

import lombok.AllArgsConstructor;


/**
 * Service class for managing users.
 */
@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    //private final AuthenticationManager authenticationManager;
    //private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user with encrypted password.
     * @param userDto The user to register.
     * @return The registered user.
     */
    public User registerUser(UserDto userDto) {
        // Encrypt the password before saving
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setName(userDto.getName());
        user.setRole(Role.USER); // Set default role
        return userRepository.save(user);
    }

    /**
     * Login a user by authenticating their credentials.
     * @param userDto The user credentials.
     * @return A JWT token if authentication is successful.
     */
    public String loginUser(UserDto userDto) {
        // Autenticación del usuario
        //authenticationManager.authenticate(
            //new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword())
        //);

        // Generación del token JWT
        return jwtUtil.generateToken(userDto.getUsername());
    }

    /**
     * Find a user by email.
     * @param email The email to search for.
     * @return The user with the given email.
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User updateUser(Long id, UserDto updatedUser) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEmail(updatedUser.getEmail());
        user.setUsername(updatedUser.getUsername());
        user.setPassword(updatedUser.getPassword());
        if (user.getRole() == null) {
            user.setRole(Role.USER); // Ensure role is not null
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}
