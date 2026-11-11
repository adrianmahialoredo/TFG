package com.uo289390.toco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uo289390.toco.model.User;

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
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a User entity by its email.
     * 
     * @param email the email of the user to search for
     * @return the User entity associated with the given email, if present
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email exists, ignoring case.
     * Used before registering, so that "Ana@x.com" and "ana@x.com" count as the same account.
     *
     * @param email the email to look for
     * @return true if a user with that email (in any case) exists
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Finds a user by email ignoring case. Used to reset a forgotten password, where the
     * user may type the email with a different case than in the registration.
     *
     * @param email the email to look for
     * @return the user with that email (in any case), if present
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Finds a User entity by its username.
     * 
     * @param username the username of the user to search for
     * @return the User entity associated with the given username, if present
     */
    Optional<User> findByUsername(String username);
}
