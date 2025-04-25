package com.example.Lab.config;

import com.example.Lab.model.AppUser;
import com.example.Lab.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner init(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Если учётная запись с именем "teacher" не найдена, создаём её.
            if (userRepository.findByUsername("teacher").isEmpty()) {
                AppUser teacher = new AppUser();
                teacher.setUsername("teacher");
                // Задаём пароль (например, "teacher123") и кодируем его
                teacher.setPassword(passwordEncoder.encode("teacher123"));
                // Назначаем роль учителя
                teacher.setRoles(Collections.singleton("TEACHER"));
                userRepository.save(teacher);
                System.out.println("Teacher account created: username=teacher, password=teacher123");
            }
            if (userRepository.findByUsername("admin").isEmpty()) {
                AppUser teacher = new AppUser();
                teacher.setUsername("admin");
                // Задаём пароль (например, "teacher123") и кодируем его
                teacher.setPassword(passwordEncoder.encode("admin"));
                // Назначаем роль учителя
                teacher.setRoles(Collections.singleton("ADMIN"));
                userRepository.save(teacher);
                System.out.println("Teacher account created: username=admin, password=admin");
            }
        };
    }
}
