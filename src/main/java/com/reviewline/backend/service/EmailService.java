package com.reviewline.backend.service;

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

    public void sendVerificationEmail(String toEmail, String name, String token) {
        String verificationLink = frontendUrl + "/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify your Reviewline account");
        message.setText(
                "Hi " + name + ",\n\n" +
                        "Thanks for signing up for Reviewline. Please verify your email by clicking the link below:\n\n" +
                        verificationLink + "\n\n" +
                        "If you didn't create this account, you can safely ignore this email.\n\n" +
                        "— Reviewline"
        );

        mailSender.send(message);
    }
}