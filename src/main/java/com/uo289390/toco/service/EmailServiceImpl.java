package com.uo289390.toco.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link EmailService}, over SMTP.
 *
 * The templates are made with React Email in the {@code emails/} folder and exported to
 * {@code templates/email} as HTML and plain text, with {@code {{name}}} placeholders that
 * are replaced here.
 *
 * Without {@code spring.mail.host} Spring Boot does not create a {@link JavaMailSender}: then
 * nothing is sent and the code is written to the log, so the registration works in local
 * development without an SMTP server.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-13
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private static final String VERIFICATION_TEMPLATE = "templates/email/verification-code";
    private static final String VERIFICATION_SUBJECT = "Tu código de verificación de TOCO";
    private static final String PASSWORD_RESET_TEMPLATE = "templates/email/password-reset";
    private static final String PASSWORD_RESET_SUBJECT = "Recupera tu contraseña de TOCO";

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public EmailServiceImpl(ObjectProvider<JavaMailSender> mailSender,
                            @Value("${app.mail.from:TOCO <no-reply@toco.app>}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendVerificationCode(String to, String code, long minutes) {
        sendCode(to, code, minutes, VERIFICATION_TEMPLATE, VERIFICATION_SUBJECT, "verification code");
    }

    @Override
    public void sendPasswordResetCode(String to, String code, long minutes) {
        sendCode(to, code, minutes, PASSWORD_RESET_TEMPLATE, PASSWORD_RESET_SUBJECT, "password reset code");
    }

    /** Sends one of the code emails (HTML and plain text) or logs the code if there is no SMTP. */
    private void sendCode(String to, String code, long minutes, String template, String subject, String what) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("No SMTP server configured (MAIL_HOST): {} for {} is {}", what, to, code);
            return;
        }

        Map<String, String> values = Map.of("code", code, "minutes", String.valueOf(minutes));
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(fill(template + ".txt", values), fill(template + ".html", values));
            sender.send(message);
            log.info("Sent the {} to {}", what, to);
        } catch (MessagingException | MailException e) {
            log.error("Could not send the {} to {}: {}", what, to, e.getMessage(), e);
            throw new IllegalStateException("Could not send the " + what + " email", e);
        }
    }

    /** Reads a template from the classpath and replaces its {@code {{name}}} placeholders. */
    static String fill(String path, Map<String, String> values) {
        try {
            String template = new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
            for (Map.Entry<String, String> entry : values.entrySet()) {
                template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            return template;
        } catch (IOException e) {
            throw new UncheckedIOException("Email template not found: " + path, e);
        }
    }
}
