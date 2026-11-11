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
        // Runs the "background" send in the calling thread, so the tests see it already done
        service = new EmailVerificationServiceImpl(userRepository, emailService, Runnable::run, clock);
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
    void sendCode_sendingFails_doesNotThrowAndCanBeRetriedStraightAway() {
        doThrow(new IllegalStateException("SMTP down"))
                .doNothing()
                .when(emailService).sendVerificationCode(anyString(), anyString(), anyLong());

        assertDoesNotThrow(() -> service.sendCode(EMAIL));
        assertDoesNotThrow(() -> service.sendCode(EMAIL));
    }

    @Test
    void sendCode_sendingFails_discardsTheCode() {
        doThrow(new IllegalStateException("SMTP down"))
                .when(emailService).sendVerificationCode(anyString(), anyString(), anyLong());

        String code = sendAndCaptureCode();

        assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, code));
    }

    @Test
    void sendCode_answersBeforeTheEmailIsSent() {
        java.util.List<Runnable> queued = new java.util.ArrayList<>();
        service = new EmailVerificationServiceImpl(userRepository, emailService, queued::add, clock);

        service.sendCode(EMAIL);

        verifyNoInteractions(emailService);
        assertEquals(1, queued.size());
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

    // ---------- password reset ----------

    /** Requests a password reset code for a registered email and returns the one that was "emailed". */
    private String sendAndCaptureResetCode() {
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(true);
        service.sendPasswordResetCode(EMAIL);
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).sendPasswordResetCode(eq(EMAIL), code.capture(), anyLong());
        return code.getValue();
    }

    @Test
    void sendPasswordResetCode_registeredEmail_sendsASixDigitCode() {
        String code = sendAndCaptureResetCode();

        assertTrue(code.matches("\\d{6}"), "Expected 6 digits, got " + code);
        verify(emailService).sendPasswordResetCode(EMAIL, code, EmailVerificationServiceImpl.CODE_TTL.toMinutes());
        verify(emailService, never()).sendVerificationCode(anyString(), anyString(), anyLong());
    }

    @Test
    void sendPasswordResetCode_unknownEmail_sendsNothingButAnswersLikeARegisteredOne() {
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(false);

        assertDoesNotThrow(() -> service.sendPasswordResetCode(EMAIL));

        verifyNoInteractions(emailService);
        // Same message as a wrong code for a registered email: it does not reveal that there is no account
        String message = assertThrows(IllegalArgumentException.class,
                () -> service.checkPasswordResetCode(EMAIL, "not-a-code")).getMessage();
        assertEquals("Invalid verification code", message);
    }

    @Test
    void sendPasswordResetCode_unknownEmailAskedAgainTooSoon_returnsTooManyRequests() {
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(false);
        service.sendPasswordResetCode(EMAIL);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.sendPasswordResetCode(EMAIL));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
        verifyNoInteractions(emailService);
    }

    @Test
    void sendPasswordResetCode_askedAgainTooSoon_returnsTooManyRequests() {
        sendAndCaptureResetCode();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.sendPasswordResetCode(EMAIL));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
    }

    @Test
    void sendPasswordResetCode_afterCooldown_replacesTheCode() {
        String first = sendAndCaptureResetCode();
        clock.advance(EmailVerificationServiceImpl.RESEND_COOLDOWN.plusSeconds(1));

        service.sendPasswordResetCode(EMAIL);
        ArgumentCaptor<String> codes = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(2)).sendPasswordResetCode(eq(EMAIL), codes.capture(), anyLong());
        String second = codes.getAllValues().get(1);

        assertDoesNotThrow(() -> service.checkPasswordResetCode(EMAIL, second));
        if (!first.equals(second)) {
            assertThrows(IllegalArgumentException.class, () -> service.checkPasswordResetCode(EMAIL, first));
        }
    }

    @Test
    void checkPasswordResetCode_rightCode_passesAndIsNotConsumed() {
        String code = sendAndCaptureResetCode();

        assertDoesNotThrow(() -> service.checkPasswordResetCode(EMAIL, code));
        assertDoesNotThrow(() -> service.checkPasswordResetCode("  JOHN@example.com ", code));
    }

    @Test
    void checkPasswordResetCode_expired_isRejected() {
        String code = sendAndCaptureResetCode();
        clock.advance(EmailVerificationServiceImpl.CODE_TTL.plusSeconds(1));

        assertEquals("Verification code expired",
                assertThrows(IllegalArgumentException.class, () -> service.checkPasswordResetCode(EMAIL, code)).getMessage());
    }

    @Test
    void checkPasswordResetCode_tooManyWrongAttempts_discardsTheCode() {
        String code = sendAndCaptureResetCode();
        String wrong = code.equals("000000") ? "111111" : "000000";

        for (int i = 1; i < EmailVerificationServiceImpl.MAX_ATTEMPTS; i++) {
            assertEquals("Invalid verification code",
                    assertThrows(IllegalArgumentException.class, () -> service.checkPasswordResetCode(EMAIL, wrong)).getMessage());
        }
        assertEquals("Too many wrong verification codes",
                assertThrows(IllegalArgumentException.class, () -> service.checkPasswordResetCode(EMAIL, wrong)).getMessage());
        assertThrows(IllegalArgumentException.class, () -> service.checkPasswordResetCode(EMAIL, code));
    }

    @Test
    void resetAndRegistrationCodes_areIndependent() {
        String resetCode = sendAndCaptureResetCode();

        // A password reset code does not verify a registration, and the other way round
        assertThrows(IllegalArgumentException.class, () -> service.checkCode(EMAIL, resetCode));
        service.consume(EMAIL);
        assertDoesNotThrow(() -> service.checkPasswordResetCode(EMAIL, resetCode));
    }

    @Test
    void consumePasswordResetCode_discardsTheCode() {
        String code = sendAndCaptureResetCode();

        service.consumePasswordResetCode(EMAIL);

        assertThrows(IllegalArgumentException.class, () -> service.checkPasswordResetCode(EMAIL, code));
    }

    @Test
    void sendPasswordResetCode_smtpFailure_discardsTheCodeSoItCanBeAskedAgain() {
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(true);
        doThrow(new IllegalStateException("SMTP down"))
                .when(emailService).sendPasswordResetCode(eq(EMAIL), anyString(), anyLong());

        service.sendPasswordResetCode(EMAIL);

        // No cooldown left behind: the user can ask for a new code straight away
        assertDoesNotThrow(() -> service.sendPasswordResetCode(EMAIL));
    }
}
