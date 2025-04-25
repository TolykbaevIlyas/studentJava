package com.example.Lab.controller;

import com.example.Lab.model.AppUser;
import com.example.Lab.model.Student;
import com.example.Lab.repository.AppUserRepository;
import com.example.Lab.repository.StudentRepository;
import com.example.Lab.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          AppUserRepository userRepository,
                          StudentRepository studentRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AppUser user) {
        if(userRepository.findByUsername(user.getUsername()).isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Username is already taken"));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton("STUDENT")); // Устанавливаем роль STUDENT
        AppUser savedUser = userRepository.save(user);

        Student student = new Student();
        student.setFirstName(savedUser.getUsername());
        student.setLastName("Not Provided");
        student.setEmail(savedUser.getUsername());
        studentRepository.save(student);

        return ResponseEntity.ok(Collections.singletonMap("message", "Student registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            AppUser user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String token = tokenProvider.createToken(username, user.getRoles());
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid username/password supplied"));
        }
    }

    @PostMapping("/assign-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRole(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String role = request.get("role");
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.getRoles().add(role);
        userRepository.save(user);
        return ResponseEntity.ok(Collections.singletonMap("message", "Role assigned successfully"));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

}
