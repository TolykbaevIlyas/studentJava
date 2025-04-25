package com.example.Lab.dto;

import java.util.List;

public class MassEmailRequest {
    private List<String> emails;
    private String subject;
    private String body;

    // Геттеры
    public List<String> getEmails() {
        return emails;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    // Сеттеры
    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
