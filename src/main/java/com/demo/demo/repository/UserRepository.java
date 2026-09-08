package com.demo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.demo.demo.model.User;

import java.util.Optional;

/**
 * Repository interface for managing User entities.
 * 
 * This interface extends JpaRepository to provide CRUD operations
 * for User entities, including methods to find users by their email.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a User entity by its email.
     * 
     * @param email the email of the user to search for
     * @return the User entity associated with the given email, if present
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a User entity by its username.
     * 
     * @param username the username of the user to search for
     * @return the User entity associated with the given username, if present
     */
    Optional<User> findByUsername(String username);
}
