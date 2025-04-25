package com.example.Lab.controller;

import com.example.Lab.dto.EmailAttachmentRequest;
import com.example.Lab.dto.EmailRequest;
import com.example.Lab.dto.MassEmailRequest;
import com.example.Lab.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-simple")
    public ResponseEntity<String> sendSimpleEmail(@RequestBody EmailRequest request) {
        emailService.sendSimpleEmail(request.getTo(), request.getSubject(), request.getBody());
        return ResponseEntity.ok("Simple email sent.");
    }

    @PostMapping("/send-html")
    public ResponseEntity<String> sendHtmlEmail(@RequestBody EmailRequest request) throws MessagingException {
        emailService.sendHtmlEmail(request.getTo(), request.getSubject(), request.getBody());
        return ResponseEntity.ok("HTML email sent.");
    }

    @PostMapping(value = "/send-with-attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendEmailWithAttachment(
            @RequestParam("to") String[] to,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam("file") MultipartFile file
    ) throws MessagingException, IOException {
        emailService.sendEmailWithAttachment(to, subject, body, file);
        return ResponseEntity.ok("Email with attachment sent.");
    }

    @PostMapping("/send-to-students")
    public ResponseEntity<String> sendMassEmail(@RequestBody MassEmailRequest request) {
        try {
            emailService.sendMassEmail(request.getEmails(), request.getSubject(), request.getBody());
            return ResponseEntity.ok("Mass email sent.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

}
