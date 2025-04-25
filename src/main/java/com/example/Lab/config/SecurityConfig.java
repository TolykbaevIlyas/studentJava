package com.example.Lab.config;

import com.example.Lab.security.JwtTokenFilter;
import com.example.Lab.security.JwtTokenProvider;
import com.example.Lab.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/auth/**").permitAll() // разрешаем регистрацию/логин без токена
                        // GET запросы на курсы доступны всем аутентифицированным пользователям
                        .requestMatchers("/courses/**").permitAll()
                        .requestMatchers("/students/**").permitAll()
                        .requestMatchers("/enrollments/**").permitAll()
                        .requestMatchers("/email/**").permitAll()
                        // Остальные эндпоинты будут иметь дополнительную защиту методом @PreAuthorize в контроллерах
                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> {}) // отключаем Basic Auth
                .addFilterBefore(new JwtTokenFilter(tokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}






