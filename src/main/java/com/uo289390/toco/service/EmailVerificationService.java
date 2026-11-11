package com.uo289390.toco.service;

/**
 * Service contract for one-time codes sent by email: the one that verifies the email of a
 * registration and the one that allows resetting a forgotten password.
 *
 * The registration code is requested before the account exists; the registration then sends
 * it along with the user data, and the account is only created if it matches. The password
 * reset code is requested for an existing account and is sent along with the new password.
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

    // ---------- password reset ("Olvidé mi contraseña") ----------

    /**
     * Generates a code to reset the password of an account and sends it to its email.
     * These codes are independent from the registration ones and follow the same rules
     * (6 digits, 10 minutes, 60 s between codes, 5 attempts).
     *
     * If no account has that email nothing is sent, but the call ends the same way, so the
     * response does not reveal which emails are registered.
     *
     * @param email The email of the account.
     * @throws org.springframework.web.server.ResponseStatusException (429) if a code was asked
     *         for that email too recently.
     */
    void sendPasswordResetCode(String email);

    /**
     * Checks a password reset code without consuming it, so the app can validate the code
     * before asking for the new password.
     *
     * @param email The email the code was sent to.
     * @param code  The code typed by the user.
     * @throws IllegalArgumentException with the same messages as {@link #checkCode}.
     */
    void checkPasswordResetCode(String email, String code);

    /**
     * Discards the password reset code of an email once the password was changed.
     *
     * @param email The email of the account.
     */
    void consumePasswordResetCode(String email);
}
