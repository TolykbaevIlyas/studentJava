package com.example.Lab.dto;

public class StudentDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public StudentDTO(Long id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    // Геттеры и, если нужно, сеттеры
    public Long getId() {
        return id;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() {
        return email;
    }
}
