package com.onlineSchool.integration;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.config.TestSecurityConfig;
import com.onlineSchool.model.*;
import com.onlineSchool.service.ProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class ProgressIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ProgressService progressService;

    @Test
    @WithMockUser(username = "teststudent", roles = {"STUDENT"})
    public void testStudentProgressPage_ShouldWork() throws Exception {
        // 📊 СПАСАЕМ КОТЕНКА: Тест прогресса
        System.out.println("📊 ТЕСТ ПРОГРЕССА: Финальное спасение котенка!");
        
        // Arrange
        User teacher = createTestTeacher();
        User student = new User();
        student.setUsername("teststudent");
        student.setEmail("teststudent@test.com");
        student.setPassword("password");
        student.setFirstName("Test");
        student.setLastName("Student");
        student.setRole(Role.STUDENT);
        student = userService.save(student);
        
        Course course = createTestCourse(teacher);
        
        // Записываем студента на курс
        courseService.enrollStudent(course.getId(), student);
        
        // Создаем несколько вебинаров
        Webinar pastWebinar = createTestWebinar(course);
        pastWebinar.setStartTime(LocalDateTime.now().minusDays(1)); // Прошедший
        pastWebinar = webinarService.save(pastWebinar);
        
        Webinar futureWebinar = createTestWebinar(course);
        futureWebinar.setStartTime(LocalDateTime.now().plusDays(1)); // Будущий
        futureWebinar = webinarService.save(futureWebinar);
        
        // Добавляем студента как участника обоих вебинаров
        webinarService.addParticipant(pastWebinar.getId(), student);
        webinarService.addParticipant(futureWebinar.getId(), student);
        
        System.out.println("✅ Настроена тестовая ситуация с курсом и вебинарами");
        
        // Act & Assert: Проверяем страницу прогресса
        mockMvc.perform(get("/student/progress"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/progress"))
                .andExpect(model().attributeExists("student"))
                .andExpect(model().attributeExists("enrolledCourses"))
                .andExpect(model().attributeExists("courseProgressMap"))
                .andExpect(model().attributeExists("totalCourses"))
                .andExpect(model().attributeExists("completedWebinars"))
                .andExpect(model().attributeExists("overallProgress"));
        
        System.out.println("✅ Страница прогресса загружается корректно!");
        
        // Проверяем логику подсчета прогресса
        int courseProgress = progressService.calculateCourseProgress(course, student);
        assertThat(courseProgress).isEqualTo(50); // 1 из 2 вебинаров завершен
        
        Map<String, Object> stats = progressService.getProgressStatistics(student);
        assertThat(stats.get("totalCourses")).isEqualTo(1);
        assertThat(stats.get("completedWebinars")).isEqualTo(1L); // Только прошедший вебинар
        assertThat(stats.get("totalAttendedWebinars")).isEqualTo(2); // Всего участвует в 2
        
        System.out.println("✅ Логика подсчета прогресса работает корректно!");
        System.out.println("📊 Прогресс курса: " + courseProgress + "%");
        System.out.println("📊 Статистика: " + stats);
        
        System.out.println("🐱 КОТЕНОК ПОЛНОСТЬЮ СПАСЕН! Прогресс работает!");
    }
    
    @Test
    @WithMockUser(username = "teststudent", roles = {"STUDENT"})
    public void testStudentDashboard_ShouldShowProgress() throws Exception {
        // Arrange
        User teacher = createTestTeacher();
        User student = new User();
        student.setUsername("teststudent");
        student.setEmail("teststudent@test.com");
        student.setPassword("password");
        student.setFirstName("Test");
        student.setLastName("Student");
        student.setRole(Role.STUDENT);
        student = userService.save(student);
        
        Course course = createTestCourse(teacher);
        courseService.enrollStudent(course.getId(), student);
        
        // Act & Assert
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/dashboard"))
                .andExpect(model().attributeExists("student"))
                .andExpect(model().attributeExists("enrolledCourses"))
                .andExpect(model().attributeExists("courseProgressMap"))
                .andExpect(model().attributeExists("coursesCount"))
                .andExpect(model().attributeExists("overallProgress"));
        
        System.out.println("✅ Dashboard студента работает корректно!");
    }
    
    @Test
    public void testProgressService_CalculatesCorrectly() {
        // Arrange
        User teacher = createTestTeacher();
        User student = createTestUser();
        Course course = createTestCourse(teacher);
        
        // Создаем 4 вебинара
        Webinar webinar1 = createTestWebinar(course);
        webinar1.setStartTime(LocalDateTime.now().minusDays(3));
        webinar1 = webinarService.save(webinar1);
        
        Webinar webinar2 = createTestWebinar(course);
        webinar2.setStartTime(LocalDateTime.now().minusDays(2));
        webinar2 = webinarService.save(webinar2);
        
        Webinar webinar3 = createTestWebinar(course);
        webinar3.setStartTime(LocalDateTime.now().minusDays(1));
        webinar3 = webinarService.save(webinar3);
        
        Webinar webinar4 = createTestWebinar(course);
        webinar4.setStartTime(LocalDateTime.now().plusDays(1)); // Будущий
        webinar4 = webinarService.save(webinar4);
        
        // Студент участвует во всех вебинарах
        webinarService.addParticipant(webinar1.getId(), student);
        webinarService.addParticipant(webinar2.getId(), student);
        webinarService.addParticipant(webinar3.getId(), student);
        webinarService.addParticipant(webinar4.getId(), student);
        
        // Act
        int progress = progressService.calculateCourseProgress(course, student);
        
        // Assert
        assertThat(progress).isEqualTo(75); // 3 из 4 вебинаров завершены
        
        System.out.println("✅ ProgressService рассчитывает прогресс корректно: " + progress + "%");
    }
    
    @Test
    public void testProgressService_HandlesEmptyCourse() {
        // Arrange
        User teacher = createTestTeacher();
        User student = createTestUser();
        Course emptyCourse = createTestCourse(teacher);
        
        // Act
        int progress = progressService.calculateCourseProgress(emptyCourse, student);
        Map<String, Object> stats = progressService.getProgressStatistics(student);
        
        // Assert
        assertThat(progress).isEqualTo(0);
        assertThat(stats.get("totalCourses")).isEqualTo(0); // Студент не записан на курс
        
        System.out.println("✅ ProgressService корректно обрабатывает пустые курсы!");
    }
} 