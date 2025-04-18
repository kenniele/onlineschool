package com.onlineSchool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.Comment;
import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.model.WebinarStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class CommentControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User testTeacher;
    private Webinar testWebinar;
    private Comment testComment;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        // Создаем тестового пользователя
        testUser = User.builder()
                .username("testuser")
                .password("password")
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .build();
        testUser = userService.save(testUser);
        
        // Создаем тестового преподавателя
        testTeacher = User.builder()
                .username("testteacher")
                .password("password")
                .email("teacher@example.com")
                .firstName("Test")
                .lastName("Teacher")
                .role(Role.TEACHER)
                .build();
        testTeacher = userService.save(testTeacher);

        // Создаем тестовый вебинар
        testWebinar = Webinar.builder()
                .title("Test Webinar")
                .description("Test Description")
                .startTime(now.plusDays(1))
                .duration(60)
                .maxParticipants(100)
                .status(WebinarStatus.SCHEDULED)
                .active(true)
                .teacher(testTeacher)
                .build();
        testWebinar = webinarService.save(testWebinar);

        // Создаем тестовый комментарий
        testComment = Comment.builder()
                .content("Test comment")
                .user(testUser)
                .entityType(EntityType.WEBINAR)
                .entityId(testWebinar.getId())
                .build();
        testComment = commentService.create(testComment);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllComments_ShouldReturnComments() throws Exception {
        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].content", is("Test comment")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCommentById_ShouldReturnComment() throws Exception {
        mockMvc.perform(get("/api/comments/{id}", testComment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testComment.getId().intValue())))
                .andExpect(jsonPath("$.content", is("Test comment")))
                .andExpect(jsonPath("$.user.id", is(testUser.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCommentById_WhenCommentNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/comments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCommentsByWebinar_ShouldReturnComments() throws Exception {
        mockMvc.perform(get("/api/comments/webinar/{webinarId}", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].content", is("Test comment")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCommentsByUser_ShouldReturnComments() throws Exception {
        mockMvc.perform(get("/api/comments/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].content", is("Test comment")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createComment_ShouldReturnCreatedComment() throws Exception {
        Comment newComment = Comment.builder()
                .content("New comment")
                .user(testUser)
                .entityType(EntityType.WEBINAR)
                .entityId(testWebinar.getId())
                .build();

        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("New comment")))
                .andExpect(jsonPath("$.user.id", is(testUser.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateComment_ShouldReturnUpdatedComment() throws Exception {
        Comment updatedComment = Comment.builder()
                .id(testComment.getId())
                .content("Updated comment")
                .user(testUser)
                .entityType(EntityType.WEBINAR)
                .entityId(testWebinar.getId())
                .build();

        mockMvc.perform(put("/api/comments/{id}", testComment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testComment.getId().intValue())))
                .andExpect(jsonPath("$.content", is("Updated comment")))
                .andExpect(jsonPath("$.user.id", is(testUser.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateComment_WhenCommentNotExists_ShouldReturnNotFound() throws Exception {
        Comment updatedComment = Comment.builder()
                .id(999L)
                .content("Updated comment")
                .user(testUser)
                .entityType(EntityType.WEBINAR)
                .entityId(testWebinar.getId())
                .build();

        mockMvc.perform(put("/api/comments/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedComment)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteComment_ShouldDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/comments/{id}", testComment.getId()))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/comments/{id}", testComment.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteComment_WhenCommentNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/comments/999"))
                .andExpect(status().isNotFound());
    }
} 