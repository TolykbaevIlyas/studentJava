package com.example.Lab.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSimpleEmail(String[] to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendEnrollmentConfirmation(String to, String studentName, String courseTitle, String enrollmentDate) {
        String subject = "Enrollment Confirmation";
        String body = String.format(
                "Dear %s,\n\nYou have been successfully enrolled in the course \"%s\" starting from %s.\n\nBest regards,",
                studentName, courseTitle, enrollmentDate
        );
        sendSimpleEmail(new String[]{to}, subject, body);
    }

    public void sendMassEmail(List<String> recipients, String subject, String body) {
        for (String email : recipients) {
            sendSimpleEmail(new String[]{email}, subject, body);
        }
    }


    public void sendHtmlEmail(String[] to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    public void sendEmailWithAttachment(String[] to, String subject, String body, MultipartFile file)
            throws MessagingException, IOException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);

        helper.addAttachment(file.getOriginalFilename(), new ByteArrayResource(file.getBytes()));

        mailSender.send(message);
    }
}
