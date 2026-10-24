package com.uo289390.toco.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Unit tests for the plain entities without dedicated tests: {@link Preferences}, {@link Settings},
 * {@link SpotifyUser} and {@link Role}. They have no custom equals/hashCode, so equality is identity.
 */
class SimpleEntitiesTest {

    // ---- Preferences ----

    @Test
    void preferences_allArgsConstructor_setsEveryField() {
        User user = new User();
        Genre rock = new Genre();
        rock.setId(1L);
        Instrument guitar = new Instrument(2L, "https://example.com/g.png", "Guitar");

        Preferences preferences = new Preferences(5L, user, 70, Set.of(rock), Set.of(guitar));

        assertEquals(5L, preferences.getId());
        assertSame(user, preferences.getUser());
        assertEquals(70, preferences.getDifficultyLevel());
        assertEquals(Set.of(rock), preferences.getFavoriteGenres());
        assertEquals(Set.of(guitar), preferences.getFavoriteInstruments());
    }

    @Test
    void preferences_defaultCollections_areMutableAndIndependent() {
        Preferences a = new Preferences();
        Preferences b = new Preferences();
        Genre rock = new Genre();
        rock.setId(1L);

        a.getFavoriteGenres().add(rock);

        assertEquals(1, a.getFavoriteGenres().size());
        assertTrue(b.getFavoriteGenres().isEmpty());
        assertNotSame(a.getFavoriteInstruments(), b.getFavoriteInstruments());
    }

    @Test
    void preferences_favouriteGenres_deduplicateById() {
        Preferences preferences = new Preferences();
        Genre rock = new Genre();
        rock.setId(1L);
        Genre sameRock = new Genre();
        sameRock.setId(1L);

        preferences.getFavoriteGenres().add(rock);
        preferences.getFavoriteGenres().add(sameRock);

        assertEquals(1, preferences.getFavoriteGenres().size());
    }

    @Test
    void preferences_equalityIsIdentity() {
        Preferences a = new Preferences();
        a.setId(1L);
        Preferences b = new Preferences();
        b.setId(1L);

        assertNotEquals(a, b);
        assertEquals(a, a);
    }

    // ---- Settings ----

    @Test
    void settings_defaults_areDisabled() {
        Settings settings = new Settings();

        assertNull(settings.getId());
        assertFalse(settings.isNotifications());
        assertFalse(settings.isSound());
    }

    @Test
    void settings_setters_updateFlags() {
        Settings settings = new Settings();
        settings.setId(3L);
        settings.setNotifications(true);
        settings.setSound(true);

        assertEquals(3L, settings.getId());
        assertTrue(settings.isNotifications());
        assertTrue(settings.isSound());
    }

    // ---- SpotifyUser ----

    @Test
    void spotifyUser_defaults_areEmpty() {
        SpotifyUser spotifyUser = new SpotifyUser();

        assertNull(spotifyUser.getId());
        assertNull(spotifyUser.getSpotifyUserId());
        assertNull(spotifyUser.getAccessToken());
        assertNull(spotifyUser.getRefreshToken());
        assertNull(spotifyUser.getTokenExpiration());
        assertNull(spotifyUser.getUser());
    }

    @Test
    void spotifyUser_allArgsConstructor_setsEveryField() {
        User user = new User();
        Instant expiration = Instant.parse("2026-01-01T00:00:00Z");

        SpotifyUser spotifyUser = new SpotifyUser(1L, "sp-id", "access", "refresh", expiration, user);

        assertEquals(1L, spotifyUser.getId());
        assertEquals("sp-id", spotifyUser.getSpotifyUserId());
        assertEquals("access", spotifyUser.getAccessToken());
        assertEquals("refresh", spotifyUser.getRefreshToken());
        assertEquals(expiration, spotifyUser.getTokenExpiration());
        assertSame(user, spotifyUser.getUser());
    }

    @Test
    void spotifyUser_equalityIsIdentity() {
        Set<SpotifyUser> set = new HashSet<>();
        set.add(new SpotifyUser(1L, "sp", "a", "r", Instant.EPOCH, null));
        set.add(new SpotifyUser(1L, "sp", "a", "r", Instant.EPOCH, null));

        assertEquals(2, set.size());
    }

    // ---- Role ----

    @ParameterizedTest
    @EnumSource(Role.class)
    void role_valueOfRoundTripsName(Role role) {
        assertSame(role, Role.valueOf(role.name()));
    }

    @Test
    void role_valueOf_isCaseSensitive() {
        assertThrows(IllegalArgumentException.class, () -> Role.valueOf("admin"));
        assertThrows(IllegalArgumentException.class, () -> Role.valueOf("ROLE_ADMIN"));
    }
}
