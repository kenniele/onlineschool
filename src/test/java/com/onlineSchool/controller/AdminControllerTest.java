package com.onlineSchool.controller;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        // Очистка тестовых данных перед каждым тестом
        clearTestData();
    }

    @Test
    void manageUsers_ShouldReturnUsersPageWithUserList() throws Exception {
        // Создаем тестового пользователя
        createTestUser("test");
        
        mockMvc.perform(get("/admin/users")
                .with(user("user").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void showCreateUserForm_ShouldReturnUserFormPageWithNewUser() throws Exception {
        mockMvc.perform(get("/admin/users/new")
                .with(user("user").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"))
                .andExpect(model().attributeExists("user"));
    }

    @Test 
    void saveUser_WhenValidUser_ShouldRedirectToAdminUsers() throws Exception {
        long timestamp = System.currentTimeMillis();
        String username = "newuser_" + timestamp;
        String email = "newuser_" + timestamp + "@example.com";
        
        mockMvc.perform(post("/admin/users/save")
                .with(user("user").roles("ADMIN"))
                .param("username", username)
                .param("email", email)
                .param("firstName", "Test")
                .param("lastName", "User")
                .param("rawPassword", "password123")
                .param("role", "STUDENT")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    void saveUser_WhenUsernameExists_ShouldReturnFormWithError() throws Exception {
        // Создаем пользователя для конфликта
        User existingUser = createTestUser("existing");

        mockMvc.perform(post("/admin/users/save")
                .with(user("user").roles("ADMIN"))
                .param("username", existingUser.getUsername())  // Существующий username
                .param("email", "student@example.com")
                .param("firstName", "Test")
                .param("lastName", "User")
                .param("rawPassword", "password123")
                .param("role", "STUDENT")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"))
                .andExpect(model().attributeHasFieldErrors("user", "username"));
    }

    @Test
    void saveUser_WhenNewUserAndRawPasswordIsBlank_ShouldReturnFormWithError() throws Exception {
        long timestamp = System.currentTimeMillis();
        String username = "newuser_" + timestamp;
        String email = "newuser_" + timestamp + "@example.com";
        
        mockMvc.perform(post("/admin/users/save")
                .with(user("user").roles("ADMIN"))
                .param("username", username)
                .param("email", email)
                .param("firstName", "Test")
                .param("lastName", "User")
                .param("rawPassword", "")  // Пустой пароль
                .param("role", "STUDENT")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"));
    }

    @Test
    void showEditUserForm_WhenUserExists_ShouldReturnUserFormPageWithUser() throws Exception {
        // Создаем пользователя для редактирования
        User existingUser = createTestUser("existing");

        mockMvc.perform(get("/admin/users/edit/" + existingUser.getId())
                .with(user("user").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditUserForm_WhenUserNotExists_ShouldThrowException() throws Exception {
        mockMvc.perform(get("/admin/users/edit/{id}", 999999L))
               .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_WhenValidUser_ShouldRedirectToAdminUsers() throws Exception {
        // Создаем пользователя для обновления
        User existingUser = createTestUser("existing");
        
        long timestamp = System.currentTimeMillis();
        String newUsername = "updated_" + timestamp;
        String newEmail = "updated_" + timestamp + "@example.com";
        
        mockMvc.perform(post("/admin/users/save")  // Используем /save вместо /update
                .with(user("user").roles("ADMIN"))
                .param("id", existingUser.getId().toString())
                .param("username", newUsername)
                .param("email", newEmail)
                .param("firstName", "Updated")
                .param("lastName", "User")
                .param("role", "TEACHER")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    void updateUser_WhenUsernameExistsAndChanged_ShouldReturnFormWithError() throws Exception {
        // Создаем двух пользователей
        User existingUser1 = createTestUser("existing1");
        User existingUser2 = createTestUser("existing2");
        
        // Пытаемся изменить username на уже существующий
        mockMvc.perform(post("/admin/users/save")  // Используем /save вместо /update
                .with(user("user").roles("ADMIN"))
                .param("id", existingUser2.getId().toString())
                .param("username", existingUser1.getUsername())  // Существующий username
                .param("email", "student@example.com")
                .param("firstName", "Test")
                .param("lastName", "User")
                .param("role", "STUDENT")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"))
                .andExpect(model().attributeHasFieldErrors("user", "username"));
    }

    @Test
    void deleteUser_ShouldRedirectToAdminUsers() throws Exception {
        // Создаем пользователя для удаления
        User userToDelete = createTestUser("toDelete");
        
        mockMvc.perform(post("/admin/users/delete/" + userToDelete.getId())
                .with(user("user").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }
} 