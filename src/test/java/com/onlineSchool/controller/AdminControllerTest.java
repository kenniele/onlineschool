package com.onlineSchool.controller;

import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.service.UserService;
import com.onlineSchool.config.TestSecurityConfig;
// import com.onlineSchool.service.CustomUserDetailsService; // Больше не мокаем, используем из TestSecurityConfig
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
// import org.springframework.security.crypto.password.PasswordEncoder; // Больше не мокаем, используем из TestSecurityConfig
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService; // Оставляем мок для UserService

    // @MockBean
    // private PasswordEncoder passwordEncoder; // Убираем этот мок

    // @MockBean
    // private CustomUserDetailsService customUserDetailsService; // Убираем этот мок

    private User adminUser;
    private User studentUser;
    
    // Нам понадобится PasswordEncoder из TestSecurityConfig для настройки моков, если он где-то нужен
    // Но в setUp он не используется для кодирования, а только для when(passwordEncoder.encode...)
    // Однако, since we're now using the real PasswordEncoder from TestSecurityConfig, 
    // the when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword"); in setUp might conflict or be redundant.
    // Let's remove it for now and see if tests pass, as TestSecurityConfig should provide a working encoder.
    // Если какой-то тест явно ожидает, что passwordEncoder.encode() вернет "encodedPassword", его нужно будет пересмотреть.

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin"); // Это имя должно совпадать с тем, что в TestSecurityConfig для админа, если мы его используем для @WithMockUser
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);

        studentUser = new User();
        studentUser.setId(2L);
        studentUser.setUsername("student");
        studentUser.setEmail("student@example.com");
        studentUser.setRole(Role.STUDENT);
        studentUser.setActive(true);

        // Убираем мокирование PasswordEncoder, так как теперь используется реальный бин
        // when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword"); 
        
        // Мокирование CustomUserDetailsService также убираем, так как TestSecurityConfig предоставляет InMemoryUserDetailsManager
        // when(customUserDetailsService.loadUserByUsername("admin")).thenReturn(adminUser);
        // Если тесты полагаются на конкретного пользователя из CustomUserDetailsService, они должны быть адаптированы
        // под пользователей, определенных в TestSecurityConfig. testUserDetailsService() создает testadmin.
        // Если @WithMockUser(roles="ADMIN") используется, он должен корректно работать с UserDetailsService из TestSecurityConfig.
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void manageUsers_ShouldReturnUsersPageWithUserList() throws Exception {
        List<User> users = Arrays.asList(adminUser, studentUser);
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"))
                .andExpect(model().attribute("users", users));
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
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(userService.save(any(User.class))).thenReturn(studentUser);

        mockMvc.perform(post("/admin/users/save")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        .param("rawPassword", "password123")
                        .param("role", "STUDENT")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveUser_WhenUsernameExists_ShouldReturnFormWithError() throws Exception {
        when(userService.existsByUsername("existinguser")).thenReturn(true);

        mockMvc.perform(post("/admin/users/save")
                        .with(csrf())
                        .param("username", "existinguser")
                        .param("email", "newuser@example.com")
                        .param("rawPassword", "password123")
                        .param("role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("user", "username", "error.user"));
        
        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveUser_WhenNewUserAndRawPasswordIsBlank_ShouldReturnFormWithError() throws Exception {
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(userService.existsByEmail(anyString())).thenReturn(false);

        mockMvc.perform(post("/admin/users/save")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        .param("rawPassword", "") // Empty password
                        .param("role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "error.user"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditUserForm_WhenUserExists_ShouldReturnUserFormPageWithUser() throws Exception {
        when(userService.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));

        mockMvc.perform(get("/admin/users/edit/{id}", studentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-form"))
                .andExpect(model().attribute("user", studentUser));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditUserForm_WhenUserNotExists_ShouldThrowException() throws Exception {
        when(userService.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/users/edit/{id}", 999L))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WhenValidUser_ShouldRedirectToAdminUsers() throws Exception {
        when(userService.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(userService.existsByEmail(anyString())).thenReturn(false);  
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/admin/users/update")
                        .with(csrf())
                        .param("id", studentUser.getId().toString())
                        .param("username", "updatedstudent")
                        .param("email", "updatedstudent@example.com")
                        .param("rawPassword", "") // Password not changed
                        .param("role", "TEACHER")
                        .param("active", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WhenUsernameExistsAndChanged_ShouldReturnFormWithError() throws Exception {
        User anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setUsername("existinguser");

        when(userService.findById(studentUser.getId())).thenReturn(Optional.of(studentUser)); 
        when(userService.existsByUsername("existinguser")).thenReturn(true); 

        mockMvc.perform(post("/admin/users/update")
                        .with(csrf())
                        .param("id", studentUser.getId().toString())
                        .param("username", "existinguser")
                        .param("email", "student@example.com")
                        .param("role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("user", "username", "error.user"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldRedirectToAdminUsers() throws Exception {
        mockMvc.perform(post("/admin/users/delete/{id}", studentUser.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(1)).deleteById(studentUser.getId());
    }
} 