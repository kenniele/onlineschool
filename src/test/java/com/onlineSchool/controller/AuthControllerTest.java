package com.onlineSchool.controller;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.service.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Очистка тестовых данных перед каждым тестом, если это необходимо
        // clearTestData(); // Предполагается, что этот метод есть в BaseIntegrationTest
    }

    @Test
    void registerUser_Success() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "testuserReg")
                .param("email", "testuserReg@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection()) // Ожидаем редирект
                .andExpect(redirectedUrl("/login?registered"));

        entityManager.flush();
        entityManager.clear();

        // Проверяем, что пользователь действительно сохранен в базе
        User savedUser = userService.findByUsername("testuserReg").orElse(null);
        assertNotNull(savedUser);
        assertEquals("testuserReg@example.com", savedUser.getEmail());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        assertEquals(Role.STUDENT, savedUser.getRole());
        assertTrue(savedUser.isActive());
    }

    @Test
    void registerUser_PasswordMismatch() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "testuserRegMismatch")
                .param("email", "testuserRegMismatch@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password456")) // Несовпадающий пароль
                .andExpect(status().isOk()) // Остаемся на странице регистрации
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Пароли не совпадают"));
    }

    @Test
    void registerUser_EmailExists() throws Exception {
        // Сначала создадим пользователя с таким email
        User existingUser = User.builder().username("existingUserMail").email("exists@example.com").password(passwordEncoder.encode("password")).role(Role.STUDENT).active(true).build(); // Пароль нужно кодировать
        userService.save(existingUser);

        mockMvc.perform(post("/register")
                .param("username", "newUserEmail")
                .param("email", "exists@example.com") // Существующий email
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Пользователь с таким email уже существует"));
    }

    @Test
    void registerUser_UsernameExists() throws Exception {
        // Сначала создадим пользователя с таким username
        User existingUser = User.builder().username("existingUsername").email("someother@example.com").password(passwordEncoder.encode("password")).role(Role.STUDENT).active(true).build(); // Пароль нужно кодировать
        userService.save(existingUser);

        mockMvc.perform(post("/register")
                .param("username", "existingUsername") // Существующий username
                .param("email", "newemail@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Пользователь с таким именем уже существует"));
    }
} 