package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.uo289390.toco.model.SpotifyUser;
import com.uo289390.toco.model.User;

/**
 * Integration tests for SpotifyUserRepository: the two derived lookups (by the owning user and by
 * the Spotify account ID) when several accounts exist, and token persistence.
 */
@DataJpaTest
class SpotifyUserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private SpotifyUserRepository spotifyUserRepository;

    private Long linkUser(String username, String spotifyUserId) {
        User user = user(username);
        user.setSpotifyUser(spotifyUser(spotifyUserId));
        Long id = em.persistAndGetId(user, Long.class);
        em.flush();
        em.clear();
        return id;
    }

    @Test
    void findByUserId_withSeveralLinkedUsers_returnsTheAccountOfThatUser() {
        Long aliceId = linkUser("alice", "spotify-alice");
        Long bobId = linkUser("bob", "spotify-bob");

        assertEquals("spotify-alice", spotifyUserRepository.findByUserId(aliceId).orElseThrow().getSpotifyUserId());
        assertEquals("spotify-bob", spotifyUserRepository.findByUserId(bobId).orElseThrow().getSpotifyUserId());
    }

    @Test
    void findByUserId_loadsTheBackReference() {
        Long aliceId = linkUser("alice", "spotify-alice");

        SpotifyUser account = spotifyUserRepository.findByUserId(aliceId).orElseThrow();

        assertEquals(aliceId, account.getUser().getId());
    }

    @Test
    void findByUserId_unknownUser_returnsEmpty() {
        linkUser("alice", "spotify-alice");

        assertTrue(spotifyUserRepository.findByUserId(9999L).isEmpty());
    }

    @Test
    void findByUserId_accountNotLinkedToAnyUser_isNotFoundByAnyUserId() {
        Long aliceId = linkUser("alice", "spotify-alice");
        spotifyUserRepository.saveAndFlush(spotifyUser("orphan"));

        assertNull(spotifyUserRepository.findBySpotifyUserId("orphan").orElseThrow().getUser());
        assertEquals("spotify-alice", spotifyUserRepository.findByUserId(aliceId).orElseThrow().getSpotifyUserId());
    }

    @Test
    void findByUserId_null_matchesAccountsWithoutUser() {
        // Derived queries turn a null argument into "IS NULL": a null user ID finds an unlinked account
        spotifyUserRepository.saveAndFlush(spotifyUser("orphan"));
        linkUser("alice", "spotify-alice");

        assertEquals("orphan", spotifyUserRepository.findByUserId(null).orElseThrow().getSpotifyUserId());
    }

    @Test
    void findBySpotifyUserId_isExactMatch() {
        linkUser("alice", "spotify-alice");

        assertTrue(spotifyUserRepository.findBySpotifyUserId("SPOTIFY-ALICE").isEmpty());
        assertTrue(spotifyUserRepository.findBySpotifyUserId("spotify").isEmpty());
        assertTrue(spotifyUserRepository.findBySpotifyUserId("spotify-alice").isPresent());
    }

    @Test
    void save_persistsTokensAndExpiration() {
        SpotifyUser account = spotifyUser("tokens");
        Instant expiration = Instant.now().plusSeconds(1800).truncatedTo(ChronoUnit.MILLIS);
        account.setTokenExpiration(expiration);
        Long id = spotifyUserRepository.saveAndFlush(account).getId();
        em.clear();

        SpotifyUser loaded = spotifyUserRepository.findById(id).orElseThrow();
        assertEquals("access-tokens", loaded.getAccessToken());
        assertEquals("refresh-tokens", loaded.getRefreshToken());
        assertEquals(expiration, loaded.getTokenExpiration().truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    void updateAccessToken_isPersisted() {
        Long aliceId = linkUser("alice", "spotify-alice");
        SpotifyUser account = spotifyUserRepository.findByUserId(aliceId).orElseThrow();
        account.setAccessToken("new-access");
        spotifyUserRepository.saveAndFlush(account);
        em.clear();

        assertEquals("new-access", spotifyUserRepository.findBySpotifyUserId("spotify-alice").orElseThrow().getAccessToken());
        assertEquals(1, spotifyUserRepository.count());
    }
}
