package com.onlineSchool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Разрешить доступ ко всем статическим ресурсам
                .requestMatchers("/css/**", "/js/**", "/images/**", "/*.html", "/*.ico").permitAll()
                // Разрешить доступ к страницам авторизации и регистрации
                .requestMatchers("/", "/login", "/register", "/courses", "/h2-console/**").permitAll()
                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") // Указываем кастомную страницу логина
                .loginProcessingUrl("/login") // URL, на который будет отправляться форма логина
                .defaultSuccessUrl("/", true) // Изменил на /
                .failureUrl("/login?error=true") // Страница при ошибке логина
                .permitAll() // Разрешаем доступ к элементам формы логина всем
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // URL для выхода
                .logoutSuccessUrl("/login?logout=true") // Страница после успешного выхода
                .permitAll()
            );

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 