package com.onlineSchool.controller;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.User;
import com.onlineSchool.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class UserControllerTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testUser.setRole(Role.STUDENT);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].email").exists());
    }
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void getAllUsers_WhenNotAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void getUserById_WhenNotAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WhenValidInput_ShouldReturnCreatedUser() throws Exception {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new_user@onlineSchool.com");
        newUser.setPassword("password");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setRole(Role.STUDENT);
        
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(newUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(newUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(newUser.getLastName()));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WhenEmailExists_ShouldReturnBadRequest() throws Exception {
        User duplicateUser = new User();
        duplicateUser.setUsername("duplicate");
        duplicateUser.setEmail(testUser.getEmail());
        duplicateUser.setPassword("password");
        duplicateUser.setFirstName("Duplicate");
        duplicateUser.setLastName("User");
        duplicateUser.setRole(Role.STUDENT);
        
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void createUser_WhenNotAdmin_ShouldReturnForbidden() throws Exception {
        User user = new User();
        user.setUsername("forbiddenuser");
        user.setPassword("password");
        user.setEmail("forbidden@example.com");
        user.setRole(Role.STUDENT);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WhenUserExists_ShouldReturnUpdatedUser() throws Exception {
        testUser.setFirstName("Updated");
        
        mockMvc.perform(put("/api/users/" + testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        User nonExistentUser = new User();
        nonExistentUser.setId(999L);
        nonExistentUser.setUsername("nonexistent");
        nonExistentUser.setEmail("nonexistent@onlineSchool.com");
        nonExistentUser.setPassword("password");
        nonExistentUser.setFirstName("NonExistent");
        nonExistentUser.setLastName("User");
        nonExistentUser.setRole(Role.STUDENT);
        
        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentUser)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void updateUser_WhenNotAdmin_ShouldReturnForbidden() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setRole(Role.STUDENT);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WhenUserExists_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId()))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void deleteUser_WhenNotAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }
} 