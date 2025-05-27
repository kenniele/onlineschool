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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.MethodName.class)
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
        // given
        User newUser = createTestUser("new");

        // when
        User savedUser = userService.save(newUser);

        // then
        assertNotNull(savedUser.getId());
        assertTrue(passwordEncoder.matches("password", savedUser.getPassword()));
        assertEquals(Role.STUDENT, savedUser.getRole());
    }

    @Test
    void deleteById_ShouldDeleteUser() {
        // when
        userService.deleteById(testUser.getId());

        // then
        assertFalse(userService.findById(testUser.getId()).isPresent());
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