package com.onlineSchool.service;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.MethodName.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, statements = {
    "DROP ALL OBJECTS;",
    "CREATE TABLE IF NOT EXISTS users (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, username VARCHAR(255) NOT NULL UNIQUE, email VARCHAR(255) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL, first_name VARCHAR(255), last_name VARCHAR(255), role VARCHAR(50) NOT NULL, active BOOLEAN DEFAULT TRUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
})
class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User testTeacher;

    @BeforeEach
    public void setUp() {
        // Создание тестовых пользователей с уникальными данными
        // createTestUser уже сохраняет пользователя через userService.save
        testUser = createTestUser(Role.STUDENT);
        testTeacher = createTestUser(Role.TEACHER);
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // when
        List<User> actualUsers = userService.findAll();

        // then
        // Ожидаем минимум 2 пользователя: созданных в setUp
        assertTrue(actualUsers.size() >= 2);
        assertTrue(actualUsers.stream().anyMatch(u -> u.getId().equals(testUser.getId())));
        assertTrue(actualUsers.stream().anyMatch(u -> u.getId().equals(testTeacher.getId())));
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // when
        Optional<User> foundUser = userService.findById(testUser.getId());

        // then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        assertEquals(testUser.getRole(), foundUser.get().getRole());
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // when
        Optional<User> foundUser = userService.findByEmail(testUser.getEmail());

        // then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        assertEquals(testUser.getRole(), foundUser.get().getRole());
    }

    @Test
    void save_NewUser_ShouldEncodePasswordAndSave() {
        // given - создаем нового пользователя с уникальными данными
        String uniqueId = UUID.randomUUID().toString();
        User newUser = User.builder()
                .username("newuser_" + uniqueId)
                .email("newuser_" + uniqueId + "@test.com")
                .password("password")
                .firstName("New")
                .lastName("User")
                .role(Role.STUDENT)
                .build();

        // when
        User savedUser = userService.save(newUser);

        // then
        assertNotNull(savedUser.getId());
        assertTrue(passwordEncoder.matches("password", savedUser.getPassword()));
        assertEquals(Role.STUDENT, savedUser.getRole());
        assertEquals("newuser_" + uniqueId, savedUser.getUsername());
    }

    @Test
    void deleteById_ShouldDeleteUser() {
        // given - создаем отдельного пользователя для удаления
        User userToDelete = createTestUser(Role.STUDENT);
        
        // when
        userService.deleteById(userToDelete.getId());

        // then
        assertFalse(userService.findById(userToDelete.getId()).isPresent());
    }

    @Test
    void existsByEmail_WhenUserExists_ShouldReturnTrue() {
        // when
        boolean exists = userService.existsByEmail(testUser.getEmail());

        // then
        assertTrue(exists);
    }

    @Test
    void existsByEmail_WhenUserDoesNotExist_ShouldReturnFalse() {
        // when
        boolean exists = userService.existsByEmail("nonexistent_" + System.currentTimeMillis() + "@test.com");

        // then
        assertFalse(exists);
    }

    @Test
    void update_WhenUserExists_ShouldUpdateAndReturnUser() {
        // given
        testUser.setFirstName("Обновленное");
        testUser.setLastName("Имя");

        // when
        User updatedUser = userService.update(testUser.getId(), testUser);

        // then
        assertEquals("Обновленное", updatedUser.getFirstName());
        assertEquals("Имя", updatedUser.getLastName());
    }

    @Test
    void update_WhenUserDoesNotExist_ShouldThrowException() {
        // given
        long nonExistentId = 999999L;

        // when/then
        assertThrows(RuntimeException.class, () -> {
            userService.update(nonExistentId, testUser);
        });
    }
} 