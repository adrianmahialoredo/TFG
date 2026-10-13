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

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public EmailServiceImpl(ObjectProvider<JavaMailSender> mailSender,
                            @Value("${app.mail.from:TOCO <no-reply@toco.app>}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendVerificationCode(String to, String code, long minutes) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("No SMTP server configured (MAIL_HOST): verification code for {} is {}", to, code);
            return;
        }

        Map<String, String> values = Map.of("code", code, "minutes", String.valueOf(minutes));
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(VERIFICATION_SUBJECT);
            helper.setText(fill(VERIFICATION_TEMPLATE + ".txt", values), fill(VERIFICATION_TEMPLATE + ".html", values));
            sender.send(message);
            log.info("Verification code sent to {}", to);
        } catch (MessagingException | MailException e) {
            log.error("Could not send the verification code to {}: {}", to, e.getMessage(), e);
            throw new IllegalStateException("Could not send the verification email", e);
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
