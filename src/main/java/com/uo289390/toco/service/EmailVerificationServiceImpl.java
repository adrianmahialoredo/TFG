package com.uo289390.toco.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.uo289390.toco.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link EmailVerificationService}.
 *
 * Registration codes and password reset codes live in two separate maps with the same rules,
 * so a code of one flow is never accepted by the other.
 *
 * Pending codes are kept in memory (single instance), like the Spotify OAuth2 states: they
 * only live a few minutes and losing them on a restart just means asking for a new one.
 *
 * The email is sent in the background (SMTP takes seconds, sometimes many more), so the
 * request answers straight away. If sending fails the code is discarded, so the user can ask
 * for a new one without waiting for the resend cooldown.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-13
 */
@Slf4j
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    /** Time the user has to type the code. */
    static final Duration CODE_TTL = Duration.ofMinutes(10);

    /** Minimum time between two codes for the same email, to avoid flooding an inbox. */
    static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

    /** Wrong attempts allowed before the code is discarded (6 digits: guessing is hopeless). */
    static final int MAX_ATTEMPTS = 5;

    private static final int CODE_DIGITS = 6;

    private final SecureRandom random = new SecureRandom();
    /** Registration codes, by normalized email. */
    private final Map<String, PendingCode> pendingCodes = new ConcurrentHashMap<>();
    /** Password reset codes, by normalized email. */
    private final Map<String, PendingCode> resetCodes = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final Executor emailExecutor;
    private final Clock clock;

    private static final class PendingCode {
        private final String code;
        private final Instant sentAt;
        private int failedAttempts;

        private PendingCode(String code, Instant sentAt) {
            this.code = code;
            this.sentAt = sentAt;
        }
    }

    @Autowired
    public EmailVerificationServiceImpl(UserRepository userRepository, EmailService emailService,
                                        @Qualifier("applicationTaskExecutor") Executor emailExecutor) {
        this(userRepository, emailService, emailExecutor, Clock.systemUTC());
    }

    /** Constructor with a controllable executor and clock, for tests. */
    EmailVerificationServiceImpl(UserRepository userRepository, EmailService emailService,
                                 Executor emailExecutor, Clock clock) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.emailExecutor = emailExecutor;
        this.clock = clock;
    }

    @Override
    public void sendCode(String email) {
        String key = normalize(email);
        // Checked here too: no point in verifying an email that the registration will reject anyway
        if (userRepository.existsByEmailIgnoreCase(key)) {
            throw new IllegalArgumentException("Email already registered");
        }

        issueCode(pendingCodes, key, code -> emailService.sendVerificationCode(email, code, CODE_TTL.toMinutes()));
    }

    @Override
    public void checkCode(String email, String code) {
        check(pendingCodes, email, code);
    }

    @Override
    public void consume(String email) {
        pendingCodes.remove(normalize(email));
    }

    @Override
    public void sendPasswordResetCode(String email) {
        String key = normalize(email);
        // The cooldown is applied before looking the account up, so registered and unknown
        // emails get the same answer (204, or 429 if asked again too soon)
        ensureCooldownElapsed(resetCodes, key);
        if (!userRepository.existsByEmailIgnoreCase(key)) {
            log.info("Password reset requested for an email without account; nothing is sent");
            return;
        }
        issueCode(resetCodes, key, code -> emailService.sendPasswordResetCode(email, code, CODE_TTL.toMinutes()));
    }

    @Override
    public void checkPasswordResetCode(String email, String code) {
        check(resetCodes, email, code);
    }

    @Override
    public void consumePasswordResetCode(String email) {
        resetCodes.remove(normalize(email));
    }

    /** Throws 429 if the last code of that email was sent less than {@link #RESEND_COOLDOWN} ago. */
    private void ensureCooldownElapsed(Map<String, PendingCode> codes, String key) {
        PendingCode previous = codes.get(key);
        if (previous != null && clock.instant().isBefore(previous.sentAt.plus(RESEND_COOLDOWN))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Verification code requested too recently");
        }
    }

    /** Stores a new code for the email (replacing the previous one) and sends it in the background. */
    private void issueCode(Map<String, PendingCode> codes, String key, Consumer<String> sender) {
        ensureCooldownElapsed(codes, key);
        String code = generateCode();
        PendingCode pending = new PendingCode(code, clock.instant());
        codes.put(key, pending);
        emailExecutor.execute(() -> {
            try {
                sender.accept(code);
            } catch (RuntimeException e) {
                // Already logged by the EmailService. Only this code is discarded, not a newer one
                // that a later request may have put in its place
                codes.remove(key, pending);
            }
        });
    }

    /** Checks a code of the given map without consuming it. */
    private void check(Map<String, PendingCode> codes, String email, String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Verification code is required");
        }
        String key = normalize(email);
        PendingCode pending = codes.get(key);
        if (pending == null) {
            throw new IllegalArgumentException("Verification code not requested");
        }
        if (clock.instant().isAfter(pending.sentAt.plus(CODE_TTL))) {
            codes.remove(key);
            throw new IllegalArgumentException("Verification code expired");
        }
        synchronized (pending) {
            if (!pending.code.equals(code)) {
                if (++pending.failedAttempts >= MAX_ATTEMPTS) {
                    codes.remove(key);
                    log.warn("Verification code for {} discarded after {} wrong attempts", email, MAX_ATTEMPTS);
                    throw new IllegalArgumentException("Too many wrong verification codes");
                }
                throw new IllegalArgumentException("Invalid verification code");
            }
        }
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_DIGITS);
        return String.format("%0" + CODE_DIGITS + "d", random.nextInt(bound));
    }

    /** Emails are compared case-insensitively and without surrounding spaces. */
    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
