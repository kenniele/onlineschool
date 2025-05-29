package com.onlineSchool.integration;

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
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class LikeIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "testuser", roles = {"STUDENT"})
    public void testLikeToggle_ShouldWorkCorrectly() throws Exception {
        // 🐱 СПАСАЕМ КОТЕНКА: Тест лайков
        System.out.println("🐱 ТЕСТ ЛАЙКОВ: Начинаем спасение котенка!");
        
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
        
        // Act & Assert: Ставим лайк
        ResultActions likeResult = mockMvc.perform(post("/api/likes/toggle/WEBINAR/" + webinar.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likesCount").value(1))
                .andExpect(jsonPath("$.message").value("Лайк поставлен"));
        
        System.out.println("✅ Лайк успешно поставлен!");
        
        // Проверяем что лайк сохранился в БД
        List<Like> likes = likeService.getLikesByEntity(EntityType.WEBINAR, webinar.getId());
        assertThat(likes).hasSize(1);
        assertThat(likes.get(0).getUser().getUsername()).isEqualTo("testuser");
        
        System.out.println("✅ Лайк найден в базе данных!");
        
        // Act & Assert: Убираем лайк
        ResultActions unlikeResult = mockMvc.perform(post("/api/likes/toggle/WEBINAR/" + webinar.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.liked").value(false))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.message").value("Лайк убран"));
        
        System.out.println("✅ Лайк успешно убран!");
        
        // Проверяем что лайк удалился из БД
        List<Like> likesAfterUnlike = likeService.getLikesByEntity(EntityType.WEBINAR, webinar.getId());
        assertThat(likesAfterUnlike).hasSize(0);
        
        System.out.println("🐱 КОТЕНОК СПАСЕН! Лайки работают корректно!");
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"STUDENT"})
    public void testLikeCount_ShouldReturnCorrectCount() throws Exception {
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
        
        // Добавляем лайк через сервис
        likeService.like(testUser.getId(), webinar.getId(), EntityType.WEBINAR);
        
        // Act & Assert
        mockMvc.perform(get("/api/likes/count/WEBINAR/" + webinar.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
        
        System.out.println("✅ Подсчет лайков работает корректно!");
    }
    
    @Test
    public void testLikeWithoutAuth_ShouldReturn403() throws Exception {
        // Arrange
        User teacher = createTestTeacher();
        Course course = createTestCourse(teacher);
        
        // Act & Assert
        mockMvc.perform(post("/api/likes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"entityId\":" + course.getId() + ",\"entityType\":\"COURSE\"}"))
                .andExpect(status().isForbidden());
    }
} 