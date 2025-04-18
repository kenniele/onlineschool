package com.onlineSchool.service;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User testTeacher;

    @BeforeEach
    void setUp() {
        // Очистка тестовых данных
        clearTestData();

        // Создание тестового пользователя-студента
        testUser = User.builder()
                .username("student_test")
                .email("student@test.com")
                .password("password123")
                .firstName("Иван")
                .lastName("Студентов")
                .role(Role.STUDENT)
                .build();
        testUser = userService.save(testUser);

        // Создание тестового пользователя-преподавателя
        testTeacher = User.builder()
                .username("teacher_test")
                .email("teacher@test.com")
                .password("password456")
                .firstName("Петр")
                .lastName("Преподавателев")
                .role(Role.TEACHER)
                .build();
        testTeacher = userService.save(testTeacher);
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // when
        List<User> actualUsers = userService.findAll();

        // then
        assertEquals(2, actualUsers.size());
        assertTrue(actualUsers.stream().anyMatch(u -> u.getEmail().equals("student@test.com")));
        assertTrue(actualUsers.stream().anyMatch(u -> u.getEmail().equals("teacher@test.com")));
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
        Optional<User> foundUser = userService.findByEmail("student@test.com");

        // then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        assertEquals(testUser.getRole(), foundUser.get().getRole());
    }

    @Test
    void save_NewUser_ShouldEncodePasswordAndSave() {
        // given
        User newUser = User.builder()
                .username("new_test")
                .email("new@test.com")
                .password("newpassword")
                .firstName("Новый")
                .lastName("Пользователь")
                .role(Role.STUDENT)
                .build();

        // when
        User savedUser = userService.save(newUser);

        // then
        assertNotNull(savedUser.getId());
        assertTrue(passwordEncoder.matches("newpassword", savedUser.getPassword()));
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
        boolean exists = userService.existsByEmail("student@test.com");

        // then
        assertTrue(exists);
    }

    @Test
    void existsByEmail_WhenUserDoesNotExist_ShouldReturnFalse() {
        // when
        boolean exists = userService.existsByEmail("nonexistent@test.com");

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
        User nonExistentUser = User.builder()
                .id(999L)
                .email("nonexistent@test.com")
                .build();

        // when/then
        assertThrows(RuntimeException.class, () -> {
            userService.update(999L, nonExistentUser);
        });
    }
} 