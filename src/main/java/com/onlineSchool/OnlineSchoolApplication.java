package com.onlineSchool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.onlineSchool.model")
@EnableJpaRepositories("com.onlineSchool.repository")
public class OnlineSchoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineSchoolApplication.class, args);
    }
} 