package com.onlineSchool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.Comment;
import com.onlineSchool.model.Course;
import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.Like;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.LikeService;
import com.onlineSchool.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class LikeControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    private Like testLike;
    private User testUser;
    private Course testCourse;
    private User testTeacher;

    @BeforeEach
    void setUp() {
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

        // Создаем тестовый курс
        testCourse = Course.builder()
                .title("Test Course")
                .description("Test Description")
                .price(100.0)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .teacher(testTeacher)
                .active(true)
                .build();
        testCourse = courseService.save(testCourse);

        // Создаем тестовый лайк
        testLike = Like.builder()
                .user(testUser)
                .entityType(EntityType.COURSE)
                .entityId(testCourse.getId())
                .build();
        testLike = likeService.createLike(testLike);
    }

    @Test
    @WithMockUser(username = "testuser")
    public void createLike_WhenValidInput_ShouldReturnCreatedLike() throws Exception {
        // Удаляем существующий лайк, чтобы избежать уникального ограничения
        likeService.deleteLike(testLike.getId());
        
        Like newLike = Like.builder()
                .user(testUser)
                .entityType(EntityType.COURSE)
                .entityId(testCourse.getId())
                .build();

        mockMvc.perform(post("/api/likes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLike)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$.entityType").value("COURSE"))
                .andExpect(jsonPath("$.entityId").value(testCourse.getId().intValue()));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void deleteLike_WhenLikeExists_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/likes/{id}", testLike.getId()))
                .andExpect(status().isNoContent());

        // Проверяем, что лайк удален
        mockMvc.perform(get("/api/likes/{id}", testLike.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void getLikesByEntity_ShouldReturnLikesList() throws Exception {
        mockMvc.perform(get("/api/likes/entity/{entityType}/{entityId}", 
                testLike.getEntityType(), testLike.getEntityId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].entityType").value(testLike.getEntityType().toString()))
                .andExpect(jsonPath("$[0].entityId").value(testLike.getEntityId().intValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLikesByUser_ShouldReturnLikesList() throws Exception {
        mockMvc.perform(get("/api/likes/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].user.id").value(testUser.getId().intValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLikesByUserAndEntity_ShouldReturnLikesList() throws Exception {
        mockMvc.perform(get("/api/likes/user/{userId}/entity/{entityType}/{entityId}", 
                testUser.getId(), testLike.getEntityType(), testLike.getEntityId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].user.id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$[0].entityType").value(testLike.getEntityType().toString()))
                .andExpect(jsonPath("$[0].entityId").value(testLike.getEntityId().intValue()));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void getLikeById_WhenLikeExists_ShouldReturnLike() throws Exception {
        mockMvc.perform(get("/api/likes/{id}", testLike.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testLike.getId().intValue()))
                .andExpect(jsonPath("$.user.id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$.entityType").value(testLike.getEntityType().toString()))
                .andExpect(jsonPath("$.entityId").value(testLike.getEntityId().intValue()));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void getLikeById_WhenLikeDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/likes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void countLikes_ShouldReturnCount() throws Exception {
        mockMvc.perform(get("/api/likes/count/{entityType}/{entityId}", 
                testLike.getEntityType(), testLike.getEntityId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void hasLiked_WhenUserLiked_ShouldReturnTrue() throws Exception {
        mockMvc.perform(get("/api/likes/has-liked/{userId}/{entityType}/{entityId}", 
                testUser.getId(), testLike.getEntityType(), testLike.getEntityId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void hasLiked_WhenUserNotLiked_ShouldReturnFalse() throws Exception {
        // Создаем другого пользователя, который не ставил лайк
        User otherUser = User.builder()
                .username("otheruser")
                .password("password")
                .email("other@example.com")
                .firstName("Other")
                .lastName("User")
                .role(Role.USER)
                .build();
        otherUser = userService.save(otherUser);

        mockMvc.perform(get("/api/likes/has-liked/{userId}/{entityType}/{entityId}", 
                otherUser.getId(), testLike.getEntityType(), testLike.getEntityId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
} 