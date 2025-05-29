package com.onlineSchool.config;

import com.onlineSchool.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Отключаем CSRF для API
            )
            .authorizeHttpRequests(auth -> auth
                // Разрешить доступ ко всем статическим ресурсам
                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/*.html", "/*.ico", "/favicon.ico").permitAll()
                // Разрешить доступ к страницам авторизации и регистрации
                .requestMatchers("/login", "/register", "/h2-console/**").permitAll()
                // Разрешить доступ к главной странице и курсам для всех
                .requestMatchers("/", "/courses", "/courses/**").permitAll()
                // Разрешить публичный доступ к курсам через API, но вебинары только для авторизованных
                .requestMatchers("GET", "/api/courses/**").permitAll()
                // Разрешить GET запросы к комментариям для всех пользователей
                .requestMatchers("GET", "/api/comments/**").permitAll()
                .requestMatchers("GET", "/api/webinars/**").authenticated()
                // Вебинары доступны только авторизованным пользователям
                .requestMatchers("/webinars", "/webinars/**").authenticated()
                // Профиль требует аутентификации
                .requestMatchers("/profile").authenticated()
                // Дашборд требует аутентификации
                .requestMatchers("/dashboard").authenticated()
                // Административные страницы только для админов
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Страницы учителей только для учителей
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                // Страницы студентов только для студентов
                .requestMatchers("/student/**").hasRole("STUDENT")
                // API endpoints требуют аутентификации (кроме GET для курсов и комментариев)
                .requestMatchers("/api/**").authenticated()
                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .userDetailsService(customUserDetailsService);

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 