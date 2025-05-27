package com.onlineSchool.controller;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private User adminUser;
    private User studentUser;
    
    @BeforeEach
    public void setUp() {
        // Создаем тестовых пользователей с уникальными данными
        adminUser = createTestUser(Role.ADMIN);
        studentUser = createTestUser(Role.STUDENT);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void manageUsers_ShouldReturnUsersPageWithUserList() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showCreateUserForm_ShouldReturnUserFormPageWithNewUser() throws Exception {
        mockMvc.perform(get("/admin/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-form"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", hasProperty("active", is(true))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveUser_WhenValidUser_ShouldRedirectToAdminUsers() throws Exception {
        long timestamp = System.currentTimeMillis();
        String username = "newuser_" + timestamp;
        String email = "newuser_" + timestamp + "@example.com";

        mockMvc.perform(post("/admin/users/save")
                        .with(csrf())
                        .param("username", username)
                        .param("email", email)
                        .param("rawPassword", "password123")
                        .param("role", "STUDENT")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveUser_WhenUsernameExists_ShouldReturnFormWithError() throws Exception {
        mockMvc.perform(post("/admin/users/save")
                        .with(csrf())
                        .param("username", studentUser.getUsername()) // Используем существующий username
                        .param("email", "newuser@example.com")
                        .param("rawPassword", "password123")
                        .param("role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("user", "username", "error.user"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveUser_WhenNewUserAndRawPasswordIsBlank_ShouldReturnFormWithError() throws Exception {
        long timestamp = System.currentTimeMillis();
        String username = "newuser_" + timestamp;
        String email = "newuser_" + timestamp + "@example.com";

        mockMvc.perform(post("/admin/users/save")
                        .with(csrf())
                        .param("username", username)
                        .param("email", email)
                        .param("rawPassword", "") // Empty password
                        .param("role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "error.user"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditUserForm_WhenUserExists_ShouldReturnUserFormPageWithUser() throws Exception {
        mockMvc.perform(get("/admin/users/edit/{id}", studentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-form"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditUserForm_WhenUserNotExists_ShouldThrowException() throws Exception {
        mockMvc.perform(get("/admin/users/edit/{id}", 999999L))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WhenValidUser_ShouldRedirectToAdminUsers() throws Exception {
        long timestamp = System.currentTimeMillis();
        String newUsername = "updated_" + timestamp;
        String newEmail = "updated_" + timestamp + "@example.com";

        mockMvc.perform(post("/admin/users/update")
                        .with(csrf())
                        .param("id", studentUser.getId().toString())
                        .param("username", newUsername)
                        .param("email", newEmail)
                        .param("rawPassword", "") // Password not changed
                        .param("role", "TEACHER")
                        .param("active", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WhenUsernameExistsAndChanged_ShouldReturnFormWithError() throws Exception {
        User anotherUser = createTestUser("existing");

        mockMvc.perform(post("/admin/users/update")
                        .with(csrf())
                        .param("id", studentUser.getId().toString())
                        .param("username", anotherUser.getUsername()) // Используем username другого пользователя
                        .param("email", "student@example.com")
                        .param("role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("user", "username", "error.user"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldRedirectToAdminUsers() throws Exception {
        mockMvc.perform(post("/admin/users/delete/{id}", studentUser.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }
} 