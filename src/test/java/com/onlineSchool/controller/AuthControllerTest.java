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
    public void setUp() {
        // Очистка тестовых данных перед каждым тестом
        clearTestData();
    }

    @Test
    void registerUser_Success() throws Exception {
        long timestamp = System.currentTimeMillis();
        String username = "testuserReg_" + timestamp;
        String email = "testuserReg_" + timestamp + "@example.com";
        
        mockMvc.perform(post("/register")
                .param("username", username)
                .param("email", email)
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("firstName", "Test")
                .param("lastName", "User"))
                .andExpect(status().is3xxRedirection()) // Ожидаем редирект
                .andExpect(redirectedUrl("/login?registered"));

        entityManager.flush();
        entityManager.clear();

        // Проверяем, что пользователь действительно сохранен в базе
        User savedUser = userService.findByUsername(username).orElse(null);
        assertNotNull(savedUser);
        assertEquals(email, savedUser.getEmail());
        assertEquals(username, savedUser.getUsername());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        assertEquals(Role.STUDENT, savedUser.getRole());
        // Проверяем что пользователь сохранен в базе
        assertTrue(userService.findById(savedUser.getId()).isPresent());
    }

    @Test
    void registerUser_PasswordMismatch() throws Exception {
        long timestamp = System.currentTimeMillis();
        String username = "testuserRegMismatch_" + timestamp;
        String email = "testuserRegMismatch_" + timestamp + "@example.com";
        
        mockMvc.perform(post("/register")
                .param("username", username)
                .param("email", email)
                .param("password", "password123")
                .param("confirmPassword", "password456") // Несовпадающий пароль
                .param("firstName", "Test")
                .param("lastName", "User"))
                .andExpect(status().isOk()) // Остаемся на странице регистрации
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Пароли не совпадают"));
    }

    @Test
    void registerUser_EmailExists() throws Exception {
        // Сначала создадим пользователя с помощью BaseIntegrationTest
        User existingUser = createTestUser("existing");

        long timestamp = System.currentTimeMillis();
        String newUsername = "newUserEmail_" + timestamp;
        
        mockMvc.perform(post("/register")
                .param("username", newUsername)
                .param("email", existingUser.getEmail()) // Существующий email
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("firstName", "Test")
                .param("lastName", "User"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Пользователь с таким email уже существует"));
    }

    @Test
    void registerUser_UsernameExists() throws Exception {
        // Сначала создадим пользователя с помощью BaseIntegrationTest
        User existingUser = createTestUser("existing2");

        long timestamp = System.currentTimeMillis();
        String newEmail = "newemail_" + timestamp + "@example.com";
        
        mockMvc.perform(post("/register")
                .param("username", existingUser.getUsername()) // Существующий username
                .param("email", newEmail)
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("firstName", "Test")
                .param("lastName", "User"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Пользователь с таким именем уже существует"));
    }
} 