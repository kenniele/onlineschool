package com.onlineSchool.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.config.TestSecurityConfig;
import com.onlineSchool.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class CommentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser", roles = {"STUDENT"})
    public void testCreateComment_ShouldWorkCorrectly() throws Exception {
        // 💬 СПАСАЕМ КОТЕНКА: Тест комментариев
        System.out.println("💬 ТЕСТ КОММЕНТАРИЕВ: Продолжаем спасение котенка!");
        
        // Arrange
        User teacher = createTestTeacher();
        Course course = createTestCourse(teacher);
        Webinar webinar = createTestWebinar(course);
        
        // Создаем пользователя для теста
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@test.com");
        testUser.setPassword("password");
        testUser.setRole(Role.STUDENT);
        testUser.setActive(true);
        testUser = userService.save(testUser);
        
        System.out.println("✅ Создан тестовый вебинар ID: " + webinar.getId());
        
        // Подготавливаем данные комментария
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("content", "Это тестовый комментарий для спасения котенка!");
        commentData.put("entityType", "WEBINAR");
        commentData.put("entityId", webinar.getId());
        
        String commentJson = objectMapper.writeValueAsString(commentData);
        System.out.println("📝 Отправляем комментарий: " + commentJson);
        
        // Act & Assert: Создаем комментарий
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Это тестовый комментарий для спасения котенка!"))
                .andExpect(jsonPath("$.entityType").value("WEBINAR"))
                .andExpect(jsonPath("$.entityId").value(webinar.getId().intValue()))
                .andExpect(jsonPath("$.user.username").value("testuser"));
        
        System.out.println("✅ Комментарий успешно создан!");
        
        // Проверяем что комментарий сохранился в БД
        List<Comment> comments = commentService.getCommentsByEntity(EntityType.WEBINAR, webinar.getId());
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("Это тестовый комментарий для спасения котенка!");
        assertThat(comments.get(0).getUser().getUsername()).isEqualTo("testuser");
        
        System.out.println("✅ Комментарий найден в базе данных!");
        System.out.println("🐱 КОТЕНОК ПОЧТИ СПАСЕН! Комментарии работают!");
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"STUDENT"})
    public void testGetCommentsByWebinar_ShouldReturnComments() throws Exception {
        // Arrange
        User teacher = createTestTeacher();
        Course course = createTestCourse(teacher);
        Webinar webinar = createTestWebinar(course);
        
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@test.com");
        testUser.setPassword("password");
        testUser.setRole(Role.STUDENT);
        testUser.setActive(true);
        testUser = userService.save(testUser);
        
        // Создаем комментарий через сервис
        Comment comment = createTestComment(testUser, webinar);
        
        // Act & Assert
        mockMvc.perform(get("/api/comments/webinar/" + webinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].content").value(comment.getContent()))
                .andExpect(jsonPath("$[0].user.username").value("testuser"));
        
        System.out.println("✅ Получение комментариев работает корректно!");
    }
    
    @Test
    public void testCreateCommentWithoutAuth_ShouldReturn403() throws Exception {
        // Arrange
        User teacher = createTestTeacher();
        Course course = createTestCourse(teacher);
        Webinar webinar = createTestWebinar(course);
        
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("content", "Test comment");
        commentData.put("entityType", "WEBINAR");
        commentData.put("entityId", webinar.getId());
        
        String commentJson = objectMapper.writeValueAsString(commentData);
        
        // Act & Assert
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"STUDENT"})
    public void testCreateCommentWithInvalidData_ShouldHandleError() throws Exception {
        // Arrange
        Map<String, Object> invalidCommentData = new HashMap<>();
        invalidCommentData.put("content", "");  // Пустой контент
        invalidCommentData.put("entityType", "WEBINAR");
        // Отсутствует entityId
        
        String commentJson = objectMapper.writeValueAsString(invalidCommentData);
        
        // Act & Assert
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
                .andExpect(status().is4xxClientError());
        
        System.out.println("✅ Валидация данных работает!");
    }
} 