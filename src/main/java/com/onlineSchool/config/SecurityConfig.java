package com.onlineSchool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Публичные эндпоинты
                .requestMatchers("/api/auth/**", "/register", "/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses/{id}").permitAll()
                
                // Эндпоинты для администраторов
                .requestMatchers(HttpMethod.GET, "/api/courses").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/courses/*/deactivate").hasRole("ADMIN")
                
                // Эндпоинты для преподавателей
                .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("TEACHER")
                
                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {})
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/courses")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 