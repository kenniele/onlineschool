package com.onlineSchool.controller;

import com.onlineSchool.model.User;
import com.onlineSchool.model.Role;
import com.onlineSchool.service.UserService;
import com.onlineSchool.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@onlineSchool.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.STUDENT);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        when(userService.findAll()).thenReturn(Arrays.asList(testUser));
        
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(testUser.getId()))
                .andExpect(jsonPath("$[0].email").value(testUser.getEmail()))
                .andExpect(jsonPath("$[0].firstName").value(testUser.getFirstName()))
                .andExpect(jsonPath("$[0].lastName").value(testUser.getLastName()));
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
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(testUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testUser.getLastName()));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void getUserById_WhenNotAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WhenValidInput_ShouldReturnCreatedUser() throws Exception {
        when(userService.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(userService.save(any(User.class))).thenReturn(testUser);
        
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@onlineSchool.com\",\"password\":\"password\",\"firstName\":\"Test\",\"lastName\":\"User\",\"role\":\"STUDENT\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(testUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testUser.getLastName()));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WhenEmailExists_ShouldReturnBadRequest() throws Exception {
        when(userService.existsByEmail(testUser.getEmail())).thenReturn(true);
        
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@onlineSchool.com\",\"password\":\"password\",\"firstName\":\"Test\",\"lastName\":\"User\",\"role\":\"STUDENT\"}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void createUser_WhenNotAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@onlineSchool.com\",\"password\":\"password\",\"firstName\":\"Test\",\"lastName\":\"User\",\"role\":\"STUDENT\"}"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WhenUserExists_ShouldReturnUpdatedUser() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(userService.save(any(User.class))).thenReturn(testUser);
        
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@onlineSchool.com\",\"password\":\"password\",\"firstName\":\"Updated\",\"lastName\":\"User\",\"role\":\"STUDENT\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.empty());
        
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@onlineSchool.com\",\"password\":\"password\",\"firstName\":\"Test\",\"lastName\":\"User\",\"role\":\"STUDENT\"}"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void updateUser_WhenNotAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@onlineSchool.com\",\"password\":\"password\",\"firstName\":\"Test\",\"lastName\":\"User\",\"role\":\"STUDENT\"}"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WhenUserExists_ShouldReturnOk() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());
        
        verify(userService).deleteById(1L);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.empty());
        
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound());
        
        verify(userService, never()).deleteById(anyLong());
    }
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void deleteUser_WhenNotAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }
} 