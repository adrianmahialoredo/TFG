package com.uo289390.toco.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.mail.internet.MimeMessage;

/**
 * Unit tests for EmailServiceImpl: the exported templates and the SMTP fallback.
 */
class EmailServiceImplTest {

    @SuppressWarnings("unchecked")
    private static ObjectProvider<JavaMailSender> provider(JavaMailSender sender) {
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        return provider;
    }

    @Test
    void fill_replacesEveryPlaceholderOfTheExportedTemplates() {
        Map<String, String> values = Map.of("code", "482913", "minutes", "10");

        for (String template : new String[] {"verification-code", "password-reset"}) {
            for (String extension : new String[] {".html", ".txt"}) {
                String content = EmailServiceImpl.fill("templates/email/" + template + extension, values);
                assertTrue(content.contains("482913"), "Code missing in " + template + extension);
                assertTrue(content.contains("10 minutos"), "Lifetime missing in " + template + extension);
                assertFalse(content.contains("{{"), "Placeholder left in " + template + extension);
            }
        }
    }

    @Test
    void fill_missingTemplate_throwsUncheckedIOException() {
        java.io.UncheckedIOException ex = assertThrows(java.io.UncheckedIOException.class,
                () -> EmailServiceImpl.fill("templates/email/does-not-exist.txt", Map.of()));

        assertTrue(ex.getMessage().contains("does-not-exist.txt"));
    }

    @Test
    void fill_withoutValues_leavesPlaceholdersUntouched() {
        String content = EmailServiceImpl.fill("templates/email/verification-code.txt", Map.of());

        assertTrue(content.contains("{{code}}"));
    }

    @Test
    void sendVerificationCode_withoutSmtpServer_doesNotFail() {
        EmailServiceImpl service = new EmailServiceImpl(provider(null), "TOCO <no-reply@toco.app>");

        assertDoesNotThrow(() -> service.sendVerificationCode("john@example.com", "482913", 10));
    }

    @Test
    void sendVerificationCode_sendsAMessageToTheAddress() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage message = new JavaMailSenderImpl().createMimeMessage();
        when(sender.createMimeMessage()).thenReturn(message);
        EmailServiceImpl service = new EmailServiceImpl(provider(sender), "TOCO <no-reply@toco.app>");

        service.sendVerificationCode("john@example.com", "482913", 10);

        verify(sender).send(message);
        assertEquals("john@example.com", message.getAllRecipients()[0].toString());
        assertEquals("Tu código de verificación de TOCO", message.getSubject());
    }

    @Test
    void sendVerificationCode_smtpError_throwsIllegalState() {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());
        doThrow(new MailSendException("SMTP down")).when(sender).send(any(MimeMessage.class));
        EmailServiceImpl service = new EmailServiceImpl(provider(sender), "TOCO <no-reply@toco.app>");

        assertThrows(IllegalStateException.class, () -> service.sendVerificationCode("john@example.com", "482913", 10));
    }

    @Test
    void sendPasswordResetCode_withoutSmtpServer_doesNotFail() {
        EmailServiceImpl service = new EmailServiceImpl(provider(null), "TOCO <no-reply@toco.app>");

        assertDoesNotThrow(() -> service.sendPasswordResetCode("john@example.com", "730215", 10));
    }

    @Test
    void sendPasswordResetCode_sendsAMessageWithItsOwnSubject() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage message = new JavaMailSenderImpl().createMimeMessage();
        when(sender.createMimeMessage()).thenReturn(message);
        EmailServiceImpl service = new EmailServiceImpl(provider(sender), "TOCO <no-reply@toco.app>");

        service.sendPasswordResetCode("john@example.com", "730215", 10);

        verify(sender).send(message);
        assertEquals("john@example.com", message.getAllRecipients()[0].toString());
        assertEquals("Recupera tu contraseña de TOCO", message.getSubject());
    }

    @Test
    void sendPasswordResetCode_smtpError_throwsIllegalState() {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());
        doThrow(new MailSendException("SMTP down")).when(sender).send(any(MimeMessage.class));
        EmailServiceImpl service = new EmailServiceImpl(provider(sender), "TOCO <no-reply@toco.app>");

        assertThrows(IllegalStateException.class, () -> service.sendPasswordResetCode("john@example.com", "730215", 10));
    }
}
