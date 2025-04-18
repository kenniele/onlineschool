package com.onlineSchool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.Course;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class CourseControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    private User testTeacher;
    private User testStudent;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        // Создаем преподавателя
        testTeacher = createTestTeacher();
        
        // Создаем студента
        testStudent = createTestUser();
        
        // Создаем курс
        testCourse = createTestCourse(testTeacher);
        testCourse.setTitle("Test Course");
        testCourse.setDescription("Test Description");
        testCourse.setPrice(100.0);
        courseService.save(testCourse);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCourses_ShouldReturnCourses() throws Exception {
        // Given в setUp

        // When
        ResultActions result = mockMvc.perform(get("/api/courses")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is(testCourse.getTitle())));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCourses_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/courses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllActiveCourses_ShouldReturnActiveCourses() throws Exception {
        // Given в setUp
        courseService.activate(testCourse.getId());

        // When
        ResultActions result = mockMvc.perform(get("/api/courses/active")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is(testCourse.getTitle())))
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    @Test
    void getCourseById_ShouldReturnCourse() throws Exception {
        // Given в setUp

        // When
        ResultActions result = mockMvc.perform(get("/api/courses/{id}", testCourse.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCourse.getId().intValue())))
                .andExpect(jsonPath("$.title", is(testCourse.getTitle())))
                .andExpect(jsonPath("$.description", is(testCourse.getDescription())))
                .andExpect(jsonPath("$.price", is(testCourse.getPrice())));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createCourse_WithTeacherRole_ShouldCreateAndReturnCourse() throws Exception {
        // Given
        Course newCourse = new Course();
        newCourse.setTitle("New Course");
        newCourse.setDescription("New Description");
        newCourse.setPrice(200.0);
        newCourse.setTeacher(testTeacher);
        newCourse.setStartDate(LocalDateTime.now());
        newCourse.setEndDate(LocalDateTime.now().plusDays(30));

        // When
        ResultActions result = mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCourse)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(newCourse.getTitle())))
                .andExpect(jsonPath("$.description", is(newCourse.getDescription())))
                .andExpect(jsonPath("$.price", is(newCourse.getPrice())));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCourse_WithUserRole_ShouldReturnForbidden() throws Exception {
        // Given
        Course newCourse = new Course();
        newCourse.setTitle("New Course");
        newCourse.setDescription("New Description");
        newCourse.setPrice(200.0);
        newCourse.setTeacher(testTeacher);
        newCourse.setStartDate(LocalDateTime.now());
        newCourse.setEndDate(LocalDateTime.now().plusDays(30));

        // When & Then
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCourse)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void updateCourse_WithTeacherRole_ShouldUpdateAndReturnCourse() throws Exception {
        // Given
        testCourse.setTitle("Updated Title");
        testCourse.setDescription("Updated Description");
        testCourse.setPrice(300.0);

        // When
        ResultActions result = mockMvc.perform(put("/api/courses/{id}", testCourse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCourse)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCourse.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.price", is(300.0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCourse_WithAdminRole_ShouldDeleteCourse() throws Exception {
        // Given в setUp

        // When
        ResultActions result = mockMvc.perform(delete("/api/courses/{id}", testCourse.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk());

        // Проверяем что курс удален
        mockMvc.perform(get("/api/courses/{id}", testCourse.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void deleteCourse_WithTeacherRole_ShouldReturnForbidden() throws Exception {
        // Given в setUp

        // When & Then
        mockMvc.perform(delete("/api/courses/{id}", testCourse.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCourse_WithAdminRole_ShouldActivateCourse() throws Exception {
        // Given в setUp

        // When
        ResultActions result = mockMvc.perform(post("/api/courses/{id}/activate", testCourse.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateCourse_WithAdminRole_ShouldDeactivateCourse() throws Exception {
        // Given
        courseService.activate(testCourse.getId());

        // When
        ResultActions result = mockMvc.perform(post("/api/courses/{id}/deactivate", testCourse.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addAndRemoveStudent_ShouldUpdateEnrollments() throws Exception {
        // Given в setUp

        // When добавляем студента
        ResultActions addResult = mockMvc.perform(post("/api/courses/{id}/students/{studentId}", 
                testCourse.getId(), testStudent.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        addResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.students", hasSize(1)))
                .andExpect(jsonPath("$.students[0].id", is(testStudent.getId().intValue())));

        // When удаляем студента
        ResultActions removeResult = mockMvc.perform(delete("/api/courses/{id}/students/{studentId}", 
                testCourse.getId(), testStudent.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        removeResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.students", hasSize(0)));
    }
} 