package com.onlineSchool.config;

import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.repository.UserRepository;
import com.onlineSchool.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("adminpassword")); // Задаем пароль администратора
            admin.setEmail("admin@onlineschool.com");
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.info("Создан администратор по умолчанию: admin / adminpassword");
        }
        
        // Исправляем sequences после создания данных
        fixSequences();
    }
    
    private void fixSequences() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            log.info("Исправление sequences PostgreSQL...");
            
            // Исправляем все sequences для корректной автогенерации ID
            String[] sequenceFixes = {
                "SELECT setval('users_id_seq', COALESCE((SELECT MAX(id)+1 FROM users), 1), false);",
                "SELECT setval('courses_id_seq', COALESCE((SELECT MAX(id)+1 FROM courses), 1), false);", 
                "SELECT setval('webinars_id_seq', COALESCE((SELECT MAX(id)+1 FROM webinars), 1), false);",
                "SELECT setval('likes_id_seq', COALESCE((SELECT MAX(id)+1 FROM likes), 1), false);",
                "SELECT setval('comments_id_seq', COALESCE((SELECT MAX(id)+1 FROM comments), 1), false);"
            };
            
            for (String sql : sequenceFixes) {
                try {
                    statement.execute(sql);
                    log.info("✅ Выполнено: " + sql.split(" ")[2]); // Показываем название sequence
                } catch (Exception e) {
                    log.warn("⚠️ Ошибка при исправлении sequence: " + e.getMessage());
                }
            }
            
            log.info("✅ Sequences исправлены успешно!");
            
        } catch (Exception e) {
            log.error("❌ Ошибка при исправлении sequences: " + e.getMessage());
        }
    }
} 