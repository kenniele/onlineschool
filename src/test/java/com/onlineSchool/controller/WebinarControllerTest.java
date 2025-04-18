package com.onlineSchool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.*;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class WebinarControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private WebinarService webinarService;

    private User testTeacher;
    private User testStudent;
    private Course testCourse;
    private Webinar testWebinar;
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

        // Создаем тестовый вебинар
        testWebinar = Webinar.builder()
                .title("Test Webinar")
                .description("Test Description")
                .startTime(now.plusDays(1))
                .duration(60)
                .maxParticipants(100)
                .status(WebinarStatus.SCHEDULED)
                .active(true)
                .course(testCourse)
                .teacher(testTeacher)
                .participants(new HashSet<>())
                .build();
        testWebinar = webinarService.save(testWebinar);
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void getAllWebinars_ShouldReturnWebinars() throws Exception {
        mockMvc.perform(get("/api/webinars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Test Webinar")));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void getWebinarsByCourse_ShouldReturnWebinars() throws Exception {
        mockMvc.perform(get("/api/webinars/course/{courseId}", testCourse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Test Webinar")));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void getWebinarsByTeacher_ShouldReturnWebinars() throws Exception {
        mockMvc.perform(get("/api/webinars/teacher/{teacherId}", testTeacher.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Test Webinar")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUpcomingWebinars_ShouldReturnWebinars() throws Exception {
        mockMvc.perform(get("/api/webinars/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Test Webinar")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPastWebinars_ShouldReturnEmptyListWhenNoPastWebinars() throws Exception {
        mockMvc.perform(get("/api/webinars/past"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void getWebinarById_ShouldReturnWebinar() throws Exception {
        mockMvc.perform(get("/api/webinars/{id}", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testWebinar.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Test Webinar")));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createWebinar_ShouldReturnCreatedWebinar() throws Exception {
        Map<String, Object> webinarMap = new HashMap<>();
        webinarMap.put("title", "New Webinar");
        webinarMap.put("description", "New Description");
        webinarMap.put("startTime", now.plusDays(2).toString());
        webinarMap.put("duration", 90);
        webinarMap.put("maxParticipants", 50);
        webinarMap.put("courseId", testCourse.getId());
        webinarMap.put("teacherId", testTeacher.getId());
        webinarMap.put("status", "SCHEDULED");
        webinarMap.put("active", true);

        mockMvc.perform(post("/api/webinars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webinarMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New Webinar")))
                .andExpect(jsonPath("$.description", is("New Description")));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void updateWebinar_ShouldReturnUpdatedWebinar() throws Exception {
        Map<String, Object> webinarMap = new HashMap<>();
        webinarMap.put("id", testWebinar.getId());
        webinarMap.put("title", "Updated Webinar");
        webinarMap.put("description", "Updated Description");
        webinarMap.put("startTime", testWebinar.getStartTime().toString());
        webinarMap.put("duration", testWebinar.getDuration());
        webinarMap.put("maxParticipants", testWebinar.getMaxParticipants());
        webinarMap.put("courseId", testCourse.getId());
        webinarMap.put("teacherId", testTeacher.getId());
        webinarMap.put("status", testWebinar.getStatus().toString());
        webinarMap.put("active", testWebinar.isActive());

        mockMvc.perform(put("/api/webinars/{id}", testWebinar.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webinarMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Webinar")))
                .andExpect(jsonPath("$.description", is("Updated Description")));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void deleteWebinar_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/webinars/{id}", testWebinar.getId()))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/webinars/{id}", testWebinar.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void startWebinar_ShouldReturnUpdatedWebinar() throws Exception {
        mockMvc.perform(post("/api/webinars/{id}/start", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void completeWebinar_ShouldReturnUpdatedWebinar() throws Exception {
        // Сначала запускаем вебинар
        webinarService.start(testWebinar.getId());
        
        mockMvc.perform(post("/api/webinars/{id}/complete", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void cancelWebinar_ShouldReturnUpdatedWebinar() throws Exception {
        mockMvc.perform(post("/api/webinars/{id}/cancel", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void addParticipant_ShouldReturnUpdatedWebinar() throws Exception {
        mockMvc.perform(post("/api/webinars/{id}/participants/{userId}", 
                testWebinar.getId(), testStudent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void removeParticipant_ShouldReturnUpdatedWebinar() throws Exception {
        // Сначала добавляем участника
        webinarService.addParticipant(testWebinar.getId(), testStudent.getId());
        
        mockMvc.perform(delete("/api/webinars/{id}/participants/{userId}", 
                testWebinar.getId(), testStudent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void deactivateWebinar_ShouldDeactivateWebinar() throws Exception {
        mockMvc.perform(post("/api/webinars/{id}/deactivate", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getWebinarsByParticipant_ShouldReturnWebinars() throws Exception {
        // Сначала добавляем участника
        webinarService.addParticipant(testWebinar.getId(), testStudent.getId());
        
        mockMvc.perform(get("/api/webinars/participant/{participantId}", testStudent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testWebinar.getId().intValue())));
    }
} 