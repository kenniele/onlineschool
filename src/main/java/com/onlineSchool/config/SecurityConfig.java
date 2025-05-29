package com.onlineSchool.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineSchool.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    @Value("${cors.allowed-origins:http://localhost:8080,http://127.0.0.1:8080}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Отключаем CSRF для API
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(apiAuthenticationEntryPoint())
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
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            private final ObjectMapper objectMapper = new ObjectMapper();
            
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response,
                    AuthenticationException authException) throws IOException {
                
                // Проверяем, является ли это API запросом
                String requestURI = request.getRequestURI();
                boolean isApiRequest = requestURI.startsWith("/api/");
                
                if (isApiRequest) {
                    // Для API запросов возвращаем JSON
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Unauthorized");
                    errorResponse.put("message", "Authentication required");
                    errorResponse.put("path", requestURI);
                    
                    String jsonResponse = objectMapper.writeValueAsString(errorResponse);
                    response.getWriter().write(jsonResponse);
                } else {
                    // Для обычных страниц делаем редирект на логин
                    response.sendRedirect("/login");
                }
            }
        };
    }
} 