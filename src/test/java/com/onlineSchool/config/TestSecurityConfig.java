package com.onlineSchool.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf((csrf) -> csrf.disable())
            .authorizeHttpRequests((authorize) -> authorize
                .anyRequest().permitAll()
            )
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }

    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        UserDetails testUser = User.builder()
            .username("testuser")
            .password("password")
            .roles("USER")
            .build();

        UserDetails testAdmin = User.builder()
            .username("testadmin")
            .password("password")
            .roles("ADMIN")
            .build();

        UserDetails testTeacher = User.builder()
            .username("testteacher")
            .password("password")
            .roles("TEACHER")
            .build();

        return new InMemoryUserDetailsManager(testUser, testAdmin, testTeacher);
    }
} 