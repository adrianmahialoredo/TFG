package com.uo289390.toco.service;

/**
 * Service contract for the emails the application sends.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-13
 */
public interface EmailService {

    /**
     * Sends the verification code of the registration.
     *
     * @param to      The email address to verify.
     * @param code    The verification code.
     * @param minutes Minutes until the code expires, shown in the email.
     * @throws IllegalStateException if the email could not be sent.
     */
    void sendVerificationCode(String to, String code, long minutes);

    /**
     * Sends the code to reset a forgotten password.
     *
     * @param to      The email of the account.
     * @param code    The password reset code.
     * @param minutes Minutes until the code expires, shown in the email.
     * @throws IllegalStateException if the email could not be sent.
     */
    void sendPasswordResetCode(String to, String code, long minutes);
}
