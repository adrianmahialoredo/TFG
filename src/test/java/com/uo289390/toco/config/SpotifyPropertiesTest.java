package com.uo289390.toco.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Tests for {@link SpotifyProperties}: bean validation of each property and binding of the
 * {@code spotify.*} properties (startup fails when they are invalid).
 */
class SpotifyPropertiesTest {

    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String REDIRECT_URI = "http://127.0.0.1:8080/api/spotify/callback";

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static Set<String> invalidFields(SpotifyProperties properties) {
        return validator.validate(properties).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Configuration
    @EnableConfigurationProperties(SpotifyProperties.class)
    static class PropertiesConfig {
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(PropertiesConfig.class);

    // ---- Validation ----

    @Test
    void validProperties_haveNoViolations() {
        assertTrue(invalidFields(new SpotifyProperties(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI)).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost/callback", "https://toco.app/api/spotify/callback"})
    void httpAndHttpsRedirectUris_areAccepted(String uri) {
        assertTrue(invalidFields(new SpotifyProperties(CLIENT_ID, CLIENT_SECRET, uri)).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankClientId_isRejected(String clientId) {
        assertEquals(Set.of("clientId"), invalidFields(new SpotifyProperties(clientId, CLIENT_SECRET, REDIRECT_URI)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankClientSecret_isRejected(String clientSecret) {
        assertEquals(Set.of("clientSecret"),
                invalidFields(new SpotifyProperties(CLIENT_ID, clientSecret, REDIRECT_URI)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ftp://example.com/callback", "toco://callback", "127.0.0.1:8080/callback", "http://"})
    void nonHttpRedirectUri_isRejected(String uri) {
        assertEquals(Set.of("redirectUri"), invalidFields(new SpotifyProperties(CLIENT_ID, CLIENT_SECRET, uri)));
    }

    @Test
    void nullRedirectUri_isRejected() {
        assertEquals(Set.of("redirectUri"), invalidFields(new SpotifyProperties(CLIENT_ID, CLIENT_SECRET, null)));
    }

    @Test
    void allMissing_reportsAllFields() {
        assertEquals(Set.of("clientId", "clientSecret", "redirectUri"),
                invalidFields(new SpotifyProperties(null, null, null)));
    }

    // ---- Value semantics (Lombok @Data) ----

    @Test
    void equalsAndHashCode_compareAllFields() {
        SpotifyProperties a = new SpotifyProperties(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);
        SpotifyProperties b = new SpotifyProperties(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new SpotifyProperties(CLIENT_ID, "other", REDIRECT_URI));
    }

    // ---- Binding ----

    @Test
    void binding_readsSpotifyPrefixedProperties() {
        contextRunner
                .withPropertyValues("spotify.client-id=" + CLIENT_ID, "spotify.client-secret=" + CLIENT_SECRET,
                        "spotify.redirect-uri=" + REDIRECT_URI)
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    SpotifyProperties properties = context.getBean(SpotifyProperties.class);
                    assertEquals(CLIENT_ID, properties.getClientId());
                    assertEquals(CLIENT_SECRET, properties.getClientSecret());
                    assertEquals(REDIRECT_URI, properties.getRedirectUri());
                });
    }

    @Test
    void binding_missingProperties_failsStartup() {
        contextRunner
                .withPropertyValues("spotify.client-id=" + CLIENT_ID)
                .run(context -> assertNotNull(context.getStartupFailure()));
    }

    @Test
    void binding_invalidRedirectUri_failsStartup() {
        contextRunner
                .withPropertyValues("spotify.client-id=" + CLIENT_ID, "spotify.client-secret=" + CLIENT_SECRET,
                        "spotify.redirect-uri=not-a-url")
                .run(context -> assertNotNull(context.getStartupFailure()));
    }
}
