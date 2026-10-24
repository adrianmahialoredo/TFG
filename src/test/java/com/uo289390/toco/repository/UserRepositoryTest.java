package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import com.uo289390.toco.repository.SpotifyUserRepository;
import com.uo289390.toco.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.Role;
import com.uo289390.toco.model.SpotifyUser;
import com.uo289390.toco.model.User;

/**
 * Integration tests for UserRepository and SpotifyUserRepository: lookups, unique constraints,
 * cascades to preferences and Spotify account, and auditing timestamps.
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpotifyUserRepository spotifyUserRepository;

    @Test
    void findByEmail_findsExistingUser() {
        User saved = userRepository.saveAndFlush(user("alice"));

        assertEquals(saved.getId(), userRepository.findByEmail("alice@example.com").orElseThrow().getId());
    }

    @Test
    void findByEmail_isCaseSensitiveAndReturnsEmptyWhenMissing() {
        userRepository.saveAndFlush(user("alice"));

        assertTrue(userRepository.findByEmail("missing@example.com").isEmpty());
        assertTrue(userRepository.findByEmail("ALICE@example.com").isEmpty());
    }

    @Test
    void existsByEmailIgnoreCase_matchesAnyCase() {
        userRepository.saveAndFlush(user("alice"));

        assertTrue(userRepository.existsByEmailIgnoreCase("alice@example.com"));
        assertTrue(userRepository.existsByEmailIgnoreCase("ALICE@Example.com"));
        assertFalse(userRepository.existsByEmailIgnoreCase("missing@example.com"));
    }

    @Test
    void findByUsername_findsExistingUser() {
        userRepository.saveAndFlush(user("bob"));

        assertEquals("bob@example.com", userRepository.findByUsername("bob").orElseThrow().getEmail());
        assertTrue(userRepository.findByUsername("nobody").isEmpty());
    }

    @Test
    void save_generatesIdAndTimestampsAndDefaults() {
        User saved = userRepository.saveAndFlush(user("carol"));

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(Role.USER, saved.getRole());
        assertEquals(1, saved.getDifficultyLevel());
    }

    @Test
    void duplicateEmail_violatesUniqueConstraint() {
        userRepository.saveAndFlush(user("dave"));
        User duplicate = user("dave2");
        duplicate.setEmail("dave@example.com");

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(duplicate));
    }

    @Test
    void duplicateUsername_violatesUniqueConstraint() {
        userRepository.saveAndFlush(user("erin"));
        User duplicate = user("erin");
        duplicate.setEmail("other@example.com");

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(duplicate));
    }

    @Test
    void preferences_areCascadedAndShareUserId() {
        Genre rock = em.persist(genre(1L, "Rock"));
        Genre jazz = em.persist(genre(2L, "Jazz"));
        Instrument guitar = em.persist(instrument(1L, "Guitar"));
        User user = user("frank");
        user.getPreferences().setDifficultyLevel(40);
        user.getPreferences().setFavoriteGenres(Set.of(rock, jazz));
        user.getPreferences().setFavoriteInstruments(Set.of(guitar));
        Long id = userRepository.saveAndFlush(user).getId();
        em.clear();

        User loaded = userRepository.findById(id).orElseThrow();
        assertEquals(id, loaded.getPreferences().getId());
        assertEquals(40, loaded.getPreferences().getDifficultyLevel());
        assertEquals(Set.of(1L, 2L), Set.copyOf(loaded.getPreferences().getFavoriteGenres().stream().map(Genre::getId).toList()));
        assertEquals(List.of(1L), loaded.getPreferences().getFavoriteInstruments().stream().map(Instrument::getId).toList());
    }

    @Test
    void spotifyUser_isCascadedFromUserAndFoundByBothKeys() {
        User user = user("grace");
        user.setSpotifyUser(spotifyUser("spotify-grace"));
        Long userId = userRepository.saveAndFlush(user).getId();
        em.clear();

        SpotifyUser byUser = spotifyUserRepository.findByUserId(userId).orElseThrow();
        assertEquals("spotify-grace", byUser.getSpotifyUserId());
        assertEquals(byUser.getId(), spotifyUserRepository.findBySpotifyUserId("spotify-grace").orElseThrow().getId());
    }

    @Test
    void spotifyUserLookups_returnEmptyWhenMissing() {
        Long userId = userRepository.saveAndFlush(user("henry")).getId();

        assertTrue(spotifyUserRepository.findByUserId(userId).isEmpty());
        assertTrue(spotifyUserRepository.findBySpotifyUserId("unknown").isEmpty());
    }

    @Test
    void unlinkingSpotifyUser_removesOrphan() {
        User user = user("ivy");
        user.setSpotifyUser(spotifyUser("spotify-ivy"));
        User saved = userRepository.saveAndFlush(user);

        saved.setSpotifyUser(null);
        userRepository.saveAndFlush(saved);

        assertTrue(spotifyUserRepository.findBySpotifyUserId("spotify-ivy").isEmpty());
        assertEquals(0, spotifyUserRepository.count());
    }

    @Test
    void duplicateSpotifyUserId_violatesUniqueConstraint() {
        spotifyUserRepository.saveAndFlush(spotifyUser("same"));

        assertThrows(DataIntegrityViolationException.class, () -> spotifyUserRepository.saveAndFlush(spotifyUser("same")));
    }
}
