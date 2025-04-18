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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class CourseControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User testTeacher;
    private User testStudent;
    private Course testCourse;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
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

        // Создаем тестового студента
        testStudent = User.builder()
                .username("teststudent")
                .password("password")
                .email("student@example.com")
                .firstName("Test")
                .lastName("Student")
                .role(Role.USER)
                .build();
        testStudent = userService.save(testStudent);

        // Создаем тестовый курс
        testCourse = Course.builder()
                .title("Test Course")
                .description("Test Description")
                .price(99.99)
                .teacher(testTeacher)
                .startDate(now)
                .endDate(now.plusMonths(3))
                .active(true)
                .build();
        testCourse = courseService.save(testCourse);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCourses_ShouldReturnCoursesList() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Test Course")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllActiveCourses_ShouldReturnActiveCoursesList() throws Exception {
        mockMvc.perform(get("/api/courses/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Test Course")))
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCoursesByTeacherId_ShouldReturnCoursesList() throws Exception {
        mockMvc.perform(get("/api/courses/teacher/{teacherId}", testTeacher.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Test Course")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCoursesByStudentId_ShouldReturnCoursesList() throws Exception {
        // Сначала добавляем студента на курс
        courseService.addStudent(testCourse.getId(), testStudent.getId());
        
        mockMvc.perform(get("/api/courses/student/{studentId}", testStudent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Course")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCourseById_WhenCourseExists_ShouldReturnCourse() throws Exception {
        mockMvc.perform(get("/api/courses/{id}", testCourse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCourse.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Test Course")))
                .andExpect(jsonPath("$.description", is("Test Description")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCourseById_WhenCourseNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/courses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createCourse_WhenValidInput_ShouldReturnCreatedCourse() throws Exception {
        Course newCourse = Course.builder()
                .title("New Course")
                .description("New Description")
                .price(149.99)
                .teacher(testTeacher)
                .startDate(now.plusDays(1))
                .endDate(now.plusMonths(4))
                .active(true)
                .build();

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCourse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New Course")))
                .andExpect(jsonPath("$.description", is("New Description")))
                .andExpect(jsonPath("$.price", is(149.99)));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void updateCourse_WhenCourseExists_ShouldReturnUpdatedCourse() throws Exception {
        testCourse.setTitle("Updated Course");
        testCourse.setDescription("Updated Description");

        mockMvc.perform(put("/api/courses/{id}", testCourse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCourse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Course")))
                .andExpect(jsonPath("$.description", is("Updated Description")));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void deleteCourse_ShouldDeleteCourse() throws Exception {
        mockMvc.perform(delete("/api/courses/{id}", testCourse.getId()))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/courses/{id}", testCourse.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void activateCourse_ShouldActivateCourse() throws Exception {
        // Сначала деактивируем курс
        courseService.deactivate(testCourse.getId());
        
        mockMvc.perform(post("/api/courses/{id}/activate", testCourse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void deactivateCourse_ShouldDeactivateCourse() throws Exception {
        mockMvc.perform(post("/api/courses/{id}/deactivate", testCourse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void addStudent_ShouldAddStudentToCourse() throws Exception {
        mockMvc.perform(post("/api/courses/{id}/students/{studentId}", 
                testCourse.getId(), testStudent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students", hasSize(1)))
                .andExpect(jsonPath("$.students[0].id", is(testStudent.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void removeStudent_ShouldRemoveStudentFromCourse() throws Exception {
        // Сначала добавляем студента на курс
        courseService.addStudent(testCourse.getId(), testStudent.getId());
        
        mockMvc.perform(delete("/api/courses/{id}/students/{studentId}", 
                testCourse.getId(), testStudent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students", hasSize(0)));
    }
} 