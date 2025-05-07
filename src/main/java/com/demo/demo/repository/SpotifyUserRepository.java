package com.demo.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.demo.model.SpotifyUser;

@Repository
public interface SpotifyUserRepository extends JpaRepository<SpotifyUser, Long> {
    Optional<SpotifyUser> findBySpotifyUserId(String spotifyUserId);
}