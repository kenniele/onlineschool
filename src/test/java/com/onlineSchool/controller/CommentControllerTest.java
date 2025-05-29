package com.onlineSchool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.Comment;
import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.model.WebinarStatus;
import com.onlineSchool.repository.CommentRepository;
import com.onlineSchool.repository.UserRepository;
import com.onlineSchool.repository.WebinarRepository;
import com.onlineSchool.service.CommentService;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private WebinarService webinarService;
    
    @Autowired
    private CommentService commentService;

    private User testUser;
    private User testTeacher;
    private User testAdmin;
    private Webinar testWebinar;
    private Comment testComment;

    @BeforeEach
    public void setUp() {
        // Создаем тестовых пользователей с уникальными данными
        testUser = createTestUser(Role.STUDENT);
        testTeacher = createTestTeacher();
        testAdmin = createTestAdmin();
        
        // Создаем тестовый вебинар
        testWebinar = createTestWebinar();
        
        // Создаем тестовый комментарий
        testComment = createTestComment(testUser, testWebinar);
    }

    @Test
    void getAllComments_ShouldReturnComments() throws Exception {
        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].content", containsString("Test Comment")));
    }

    @Test
    void getCommentById_ShouldReturnComment() throws Exception {
        mockMvc.perform(get("/api/comments/{id}", testComment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testComment.getId().intValue())))
                .andExpect(jsonPath("$.content", containsString("Test Comment")))
                .andExpect(jsonPath("$.user.id", is(testUser.getId().intValue())));
    }

    @Test
    void getCommentById_WhenCommentNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/comments/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCommentsByWebinar_ShouldReturnComments() throws Exception {
        mockMvc.perform(get("/api/comments/webinar/{webinarId}", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].content", containsString("Test Comment")));
    }

    @Test
    void getCommentsByUser_ShouldReturnComments() throws Exception {
        mockMvc.perform(get("/api/comments/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].content", containsString("Test Comment")));
    }

    @Test
    @WithMockUser(username = "student_1_123456789", roles = "STUDENT")  // Используем имя созданного пользователя
    void createComment_ShouldReturnCreatedComment() throws Exception {
        // Обновляем пользователя чтобы имя совпадало с @WithMockUser
        testUser.setUsername("student_1_123456789");
        userService.save(testUser);

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("content", "New comment");
        commentMap.put("entityType", "WEBINAR");
        commentMap.put("entityId", testWebinar.getId());

        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment.content", is("New comment")))
                .andExpect(jsonPath("$.comment.user.username", is("student_1_123456789")));
    }

    @Test
    @WithMockUser(username = "not_authorized")
    void createComment_WithoutAuth_ShouldReturnForbidden() throws Exception {
        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("content", "New comment");
        commentMap.put("entityType", "WEBINAR");
        commentMap.put("entityId", testWebinar.getId());

        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentMap)))
                .andExpect(status().isBadRequest()); // Пользователь не найден - 400
    }

    @Test  
    @WithMockUser(username = "admin_1_123456789", roles = "ADMIN")
    void updateComment_AsAdmin_ShouldReturnUpdatedComment() throws Exception {
        // Обновляем админа чтобы имя совпадало с @WithMockUser
        testAdmin.setUsername("admin_1_123456789");
        userService.save(testAdmin);

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("id", testComment.getId());
        commentMap.put("content", "Updated comment by admin");
        commentMap.put("entityType", "WEBINAR");
        commentMap.put("entityId", testWebinar.getId());

        mockMvc.perform(put("/api/comments/{id}", testComment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Updated comment by admin")));
    }

    @Test
    @WithMockUser(username = "different_user", roles = "STUDENT")
    void updateComment_AsNonAuthor_ShouldReturnForbidden() throws Exception {
        // Создаем другого пользователя
        User differentUser = createTestUser(Role.STUDENT);
        differentUser.setUsername("different_user");
        userService.save(differentUser);

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("id", testComment.getId());
        commentMap.put("content", "Updated comment");
        commentMap.put("entityType", "WEBINAR");
        commentMap.put("entityId", testWebinar.getId());

        mockMvc.perform(put("/api/comments/{id}", testComment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentMap)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "different_user2", roles = "STUDENT")
    void updateComment_WhenCommentNotExists_ShouldReturnForbidden() throws Exception {
        // Создаем пользователя
        User differentUser = createTestUser(Role.STUDENT);
        differentUser.setUsername("different_user2");
        userService.save(differentUser);

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("id", 999L);
        commentMap.put("content", "Updated comment");
        commentMap.put("entityType", "WEBINAR");
        commentMap.put("entityId", testWebinar.getId());

        mockMvc.perform(put("/api/comments/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentMap)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin_2_123456789", roles = "ADMIN") 
    void deleteComment_AsAdmin_ShouldDeleteComment() throws Exception {
        // Обновляем админа чтобы имя совпадало с @WithMockUser
        testAdmin.setUsername("admin_2_123456789");
        userService.save(testAdmin);

        mockMvc.perform(delete("/api/comments/{id}", testComment.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "different_user3", roles = "STUDENT")
    void deleteComment_AsNonAuthor_ShouldReturnForbidden() throws Exception {
        // Создаем другого пользователя
        User differentUser = createTestUser(Role.STUDENT);
        differentUser.setUsername("different_user3");
        userService.save(differentUser);

        mockMvc.perform(delete("/api/comments/{id}", testComment.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "different_user4", roles = "STUDENT")
    void deleteComment_WhenCommentNotExists_ShouldReturnForbidden() throws Exception {
        // Создаем пользователя
        User differentUser = createTestUser(Role.STUDENT);
        differentUser.setUsername("different_user4");
        userService.save(differentUser);

        mockMvc.perform(delete("/api/comments/999"))
                .andExpect(status().isForbidden());
    }
} 