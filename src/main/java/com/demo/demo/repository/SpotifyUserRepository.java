package com.demo.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.demo.model.SpotifyUser;

/**
 * Repository interface for managing SpotifyUser entities.
 * 
 * This interface extends JpaRepository to provide CRUD operations
 * for SpotifyUser entities, including methods to find users by their Spotify user ID.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface SpotifyUserRepository extends JpaRepository<SpotifyUser, Long> {

    /**
     * Finds a SpotifyUser by their Spotify user ID.
     * 
     * @param spotifyUserId the Spotify user ID to search for
     * @return an Optional containing the SpotifyUser if found, or empty if not found
     */
    Optional<SpotifyUser> findBySpotifyUserId(String spotifyUserId);

}