package com.uo289390.toco.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for {@link AppConfig}: the beans it declares and their behaviour.
 */
class AppConfigTest {

    private final AppConfig appConfig = new AppConfig();

    @Test
    void restTemplate_isCreated() {
        assertNotNull(appConfig.restTemplate());
    }

    @Test
    void restTemplate_eachCallReturnsNewInstance() {
        assertNotSame(appConfig.restTemplate(), appConfig.restTemplate());
    }

    @Test
    void passwordEncoder_hashesAndMatches() {
        BCryptPasswordEncoder encoder = appConfig.passwordEncoder();

        String hash = encoder.encode("S3cret!pass");

        assertNotEquals("S3cret!pass", hash);
        assertTrue(hash.startsWith("$2"));
        assertTrue(encoder.matches("S3cret!pass", hash));
        assertFalse(encoder.matches("wrong", hash));
    }

    @Test
    void passwordEncoder_saltsEveryHash() {
        BCryptPasswordEncoder encoder = appConfig.passwordEncoder();

        assertNotEquals(encoder.encode("same"), encoder.encode("same"));
    }

    @Test
    void context_registersBothBeansAsSingletons() {
        new ApplicationContextRunner()
                .withUserConfiguration(AppConfig.class)
                .run(context -> {
                    assertSame(context.getBean(RestTemplate.class), context.getBean(RestTemplate.class));
                    assertSame(context.getBean(BCryptPasswordEncoder.class), context.getBean("passwordEncoder"));
                });
    }
}
