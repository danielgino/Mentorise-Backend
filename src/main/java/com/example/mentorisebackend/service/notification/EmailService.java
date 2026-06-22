package com.example.mentorisebackend.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String fullName, String rawToken) {
        String resetLink = frontendUrl + "/admin/reset-password?token=" + rawToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Admin Password Reset Request");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                "You requested a password reset for your admin account.\n\n" +
                "Click the link below to reset your password (valid for 15 minutes):\n" +
                resetLink + "\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Mentorise Team"
        );

        mailSender.send(message);
    }

    public void sendUserPasswordResetEmail(String toEmail, String fullName, String rawToken) {
        String resetLink = "mentorisemobile://reset-password?token=" + rawToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("איפוס סיסמה - Mentorise");
        message.setText(
                "שלום " + fullName + ",\n\n" +
                "קיבלנו בקשה לאיפוס הסיסמה שלך.\n\n" +
                "לחץ על הקישור הבא כדי לאפס את הסיסמה (בתוקף ל-15 דקות):\n" +
                resetLink + "\n\n" +
                "אם לא ביקשת זאת, ניתן להתעלם ממייל זה.\n\n" +
                "צוות Mentorise"
        );

        mailSender.send(message);
    }
}
