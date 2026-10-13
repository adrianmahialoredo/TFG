package com.uo289390.toco.service;

/**
 * Service contract for verifying the email of a registration with a one-time code.
 *
 * The code is requested before the account exists; the registration then sends it along
 * with the user data, and the account is only created if it matches.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-13
 */
public interface EmailVerificationService {

    /**
     * Generates a new code for an email and sends it, replacing any previous one.
     *
     * @param email The email address to verify.
     * @throws IllegalArgumentException if the email is already registered.
     * @throws org.springframework.web.server.ResponseStatusException (429) if a code was sent
     *         to that email too recently.
     */
    void sendCode(String email);

    /**
     * Checks a code without consuming it, so a registration that fails for another reason
     * (e.g. a taken username) can be retried with the same code.
     *
     * @param email The email address the code was sent to.
     * @param code  The code typed by the user.
     * @throws IllegalArgumentException if the code is blank, there is no pending code, it expired, it was
     *         failed too many times, or it does not match.
     */
    void checkCode(String email, String code);

    /**
     * Discards the code of an email once the registration succeeded.
     *
     * @param email The verified email address.
     */
    void consume(String email);
}
