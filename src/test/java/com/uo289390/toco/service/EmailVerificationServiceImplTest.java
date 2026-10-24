package com.uo289390.toco.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.UserRepository;

/**
 * Unit tests for EmailVerificationServiceImpl.
 * The clock is mutable so expiry and the resend cooldown are tested without waiting.
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    private static final String EMAIL = "john@example.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    private MutableClock clock;
    private EmailVerificationServiceImpl service;

    /** Clock whose instant the test moves forward. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-13T10:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        service = new EmailVerificationServiceImpl(userRepository, emailService, clock);
    }

    /** Requests a code and returns the one that was "emailed". */
    private String sendAndCaptureCode() {
        service.sendCode(EMAIL);
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).sendVerificationCode(eq(EMAIL), code.capture(), anyLong());
        return code.getValue();
    }

    @Test
    void sendCode_sendsASixDigitCodeWithItsLifetime() {
        String code = sendAndCaptureCode();

        assertTrue(code.matches("\\d{6}"), "Expected 6 digits, got " + code);
        verify(emailService).sendVerificationCode(EMAIL, code, EmailVerificationServiceImpl.CODE_TTL.toMinutes());
    }

    @Test
    void sendCode_emailAlreadyRegistered_throwsWithoutSending() {
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.sendCode(EMAIL));

        assertEquals("Email already registered", ex.getMessage());
        verifyNoInteractions(emailService);
    }

    @Test
    void sendCode_emailRegisteredWithOtherCase_throwsWithoutSending() {
        // The lookup uses the normalized email, so " John@Example.COM " matches john@example.com
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.sendCode(" John@Example.COM "));

        assertEquals("Email already registered", ex.getMessage());
        verifyNoInteractions(emailService);
    }

    @Test
    void sendCode_againBeforeCooldown_isTooManyRequests() {
        service.sendCode(EMAIL);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.sendCode(EMAIL));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
        verify(emailService, times(1)).sendVerificationCode(anyString(), anyString(), anyLong());
    }

    @Test
    void sendCode_afterCooldown_replacesThePreviousCode() {
        String first = sendAndCaptureCode();
        clock.advance(EmailVerificationServiceImpl.RESEND_COOLDOWN);
        String second = sendAndCaptureCode();

        assertDoesNotThrow(() -> service.checkCode(EMAIL, second));
        if (!first.equals(second)) {
            assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, first));
        }
    }

    @Test
    void sendCode_sendingFails_canBeRetriedStraightAway() {
        doThrow(new IllegalStateException("SMTP down"))
                .doNothing()
                .when(emailService).sendVerificationCode(anyString(), anyString(), anyLong());

        assertThrows(IllegalStateException.class, () -> service.sendCode(EMAIL));
        assertDoesNotThrow(() -> service.sendCode(EMAIL));
    }

    @Test
    void checkCode_rightCode_passesAndCanBeCheckedAgain() {
        String code = sendAndCaptureCode();

        assertDoesNotThrow(() -> service.checkCode(EMAIL, code));
        // Not consumed: a registration rejected for another reason can be retried
        assertDoesNotThrow(() -> service.checkCode(EMAIL, code));
    }

    @Test
    void checkCode_ignoresEmailCaseAndSpaces() {
        String code = sendAndCaptureCode();

        assertDoesNotThrow(() -> service.checkCode("  John@Example.COM ", code));
    }

    @Test
    void checkCode_blankCode_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, " "));

        assertEquals("Verification code is required", ex.getMessage());
    }

    @Test
    void checkCode_nullCode_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, null));

        assertEquals("Verification code is required", ex.getMessage());
    }

    @Test
    void checkCode_nullEmail_isNotRequested() {
        sendAndCaptureCode();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.checkCode(null, "123456"));

        assertEquals("Verification code not requested", ex.getMessage());
    }

    @Test
    void consume_nullEmail_doesNotDiscardOtherCodes() {
        String code = sendAndCaptureCode();

        assertDoesNotThrow(() -> service.consume(null));

        assertDoesNotThrow(() -> service.checkCode(EMAIL, code));
    }

    @Test
    void checkCode_notRequested_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, "123456"));

        assertEquals("Verification code not requested", ex.getMessage());
    }

    @Test
    void checkCode_wrongCode_throws() {
        String code = sendAndCaptureCode();
        String wrong = code.equals("000000") ? "111111" : "000000";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, wrong));

        assertEquals("Invalid verification code", ex.getMessage());
    }

    @Test
    void checkCode_expired_throwsAndDiscardsTheCode() {
        String code = sendAndCaptureCode();
        clock.advance(EmailVerificationServiceImpl.CODE_TTL.plusSeconds(1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, code));

        assertEquals("Verification code expired", ex.getMessage());
        assertEquals("Verification code not requested",
                assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, code)).getMessage());
    }

    @Test
    void checkCode_tooManyWrongAttempts_discardsEvenTheRightCode() {
        String code = sendAndCaptureCode();
        String wrong = code.equals("000000") ? "111111" : "000000";

        for (int i = 1; i < EmailVerificationServiceImpl.MAX_ATTEMPTS; i++) {
            assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, wrong));
        }
        IllegalArgumentException last = assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, wrong));

        assertEquals("Too many wrong verification codes", last.getMessage());
        assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, code));
    }

    @Test
    void consume_discardsTheCode() {
        String code = sendAndCaptureCode();

        service.consume(EMAIL);

        assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, code));
    }
}
