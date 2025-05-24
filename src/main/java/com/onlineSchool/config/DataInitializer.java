package com.onlineSchool.config;

import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("adminpassword")); // Задаем пароль администратора
            admin.setEmail("admin@onlineschool.com");
            admin.setRole(Role.ADMIN);
            admin.setActive(true); // Активируем пользователя
            // При необходимости можно задать имя и фамилию
            // admin.setFirstName("Admin");
            // admin.setLastName("User");
            userRepository.save(admin);
            System.out.println("Создан администратор по умолчанию: admin / adminpassword");
        }
    }
} 