package com.demo.demo.controller;

import com.demo.demo.dto.UserDto;
import com.demo.demo.model.User;
import com.demo.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing users.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody UserDto updatedUser) {
        return userService.updateUser(id, updatedUser);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @DeleteMapping("/all")
    public void deleteAllUsers() {
        try {
            userService.deleteAllUsers();
            
        } catch (Exception e) {
            throw new RuntimeException("Error deleting all users: " + e.getMessage(), e);
        }
    }
}
