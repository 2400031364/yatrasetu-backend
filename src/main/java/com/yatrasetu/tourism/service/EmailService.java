package com.yatrasetu.tourism.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String toEmail, String otp) {
        // Always print to the console — lets you test the whole OTP flow
        // immediately even before real SMTP credentials are configured.
        System.out.println("========== YATRASETU OTP ==========");
        System.out.println("To: " + toEmail);
        System.out.println("OTP: " + otp + " (valid 5 minutes)");
        System.out.println("====================================");

        if (fromAddress == null || fromAddress.isBlank()) {
            System.out.println("[EmailService] spring.mail.username is not set — skipping real email send. "
                    + "Use the OTP printed above to test, or configure SMTP in application.properties.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Your YatraSetu sign-in code");
            message.setText("Your one-time sign-in code is: " + otp + "\n\nThis code expires in 5 minutes. "
                    + "If you didn't request this, you can ignore this email.");
            mailSender.send(message);
        } catch (Exception e) {
            // Never let a broken SMTP config break the OTP flow itself — the
            // console fallback above already lets the user log in.
            System.err.println("[EmailService] Failed to send OTP email: " + e.getMessage());
        }
    }
}
