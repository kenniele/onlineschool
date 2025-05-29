package com.onlineSchool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.config.TestSecurityConfig;
import com.onlineSchool.model.*;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
    private User testAdmin;
    private Course testCourse;
    private Webinar testWebinar;
    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
        now = LocalDateTime.now();
        
        // Создаем тестового преподавателя с нужным username
        testTeacher = User.builder()
                .username("testteacher")
                .password("password")
                .email("teacher@example.com")
                .firstName("Test")
                .lastName("Teacher")
                .role(Role.TEACHER)
                .build();
        testTeacher = userService.save(testTeacher);

        // Создаем тестового студента с правильной ролью
        testStudent = User.builder()
                .username("teststudent")
                .password("password")
                .email("student@example.com")
                .firstName("Test")
                .lastName("Student")
                .role(Role.STUDENT)
                .build();
        testStudent = userService.save(testStudent);

        // Создаем админа
        testAdmin = User.builder()
                .username("testadmin")
                .password("password")
                .email("admin@example.com")
                .firstName("Test")
                .lastName("Admin")
                .role(Role.ADMIN)
                .build();
        testAdmin = userService.save(testAdmin);

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
    @WithMockUser(username="testteacher", roles = "TEACHER")
    void getAllWebinars_ShouldReturnWebinars() throws Exception {
        mockMvc.perform(get("/api/webinars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Test Webinar")));
    }

    @Test
    @WithMockUser(username="testteacher", roles = "TEACHER")
    void getWebinarsByCourse_ShouldReturnWebinars() throws Exception {
        mockMvc.perform(get("/api/webinars/course/{courseId}", testCourse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Test Webinar")));
    }

    @Test
    @WithMockUser(username="testteacher", roles = "TEACHER")
    void getWebinarsByTeacher_ShouldReturnWebinars() throws Exception {
        mockMvc.perform(get("/api/webinars/teacher/{teacherId}", testTeacher.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Test Webinar")));
    }

    @Test
    @WithMockUser(username="teststudent", roles = "STUDENT")
    void getUpcomingWebinars_ShouldReturnWebinars() throws Exception {
        mockMvc.perform(get("/api/webinars/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Test Webinar")));
    }

    @Test
    @WithMockUser(username="teststudent", roles = "STUDENT")
    void getPastWebinars_ShouldReturnEmptyListWhenNoPastWebinars() throws Exception {
        mockMvc.perform(get("/api/webinars/past"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username="testteacher", roles = "TEACHER")
    void getWebinarById_ShouldReturnWebinar() throws Exception {
        mockMvc.perform(get("/api/webinars/{id}", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testWebinar.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Test Webinar")));
    }

    @Test
    @WithMockUser(username="testteacher", roles = "TEACHER")
    void createWebinar_ShouldReturnCreatedWebinar() throws Exception {
        // Создаем валидный объект Webinar
        Webinar newWebinar = Webinar.builder()
                .title("New Webinar")
                .description("New Description")
                .startTime(now.plusDays(2))
                .duration(90)
                .maxParticipants(50)
                .status(WebinarStatus.SCHEDULED)
                .active(true)
                .course(testCourse)
                .build();

        mockMvc.perform(post("/api/webinars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newWebinar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New Webinar")))
                .andExpect(jsonPath("$.description", is("New Description")))
                .andExpect(jsonPath("$.teacher.id", is(testTeacher.getId().intValue())));
    }

    @Test
    @WithMockUser(username="testteacher", roles = "TEACHER")
    void updateWebinar_ShouldReturnUpdatedWebinar() throws Exception {
        // Создаем обновленный вебинар того же преподавателя
        Webinar updatedWebinar = Webinar.builder()
                .id(testWebinar.getId())
                .title("Updated Webinar")
                .description("Updated Description")
                .startTime(testWebinar.getStartTime())
                .duration(testWebinar.getDuration())
                .maxParticipants(testWebinar.getMaxParticipants())
                .course(testCourse)
                .teacher(testTeacher)
                .status(testWebinar.getStatus())
                .active(testWebinar.isActive())
                .build();

        mockMvc.perform(put("/api/webinars/{id}", testWebinar.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedWebinar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Webinar")))
                .andExpect(jsonPath("$.description", is("Updated Description")));
    }

    @Test
    @WithMockUser(username="testadmin", roles = "ADMIN")
    void deleteWebinar_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/webinars/{id}", testWebinar.getId()))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/webinars/{id}", testWebinar.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="testteacher", roles = "TEACHER")
    void startWebinar_ShouldReturnUpdatedWebinar() throws Exception {
        mockMvc.perform(post("/api/webinars/{id}/start", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    @WithMockUser(username="testteacher", roles = "TEACHER")
    void completeWebinar_ShouldReturnUpdatedWebinar() throws Exception {
        // Сначала запускаем вебинар
        webinarService.start(testWebinar.getId());
        
        mockMvc.perform(post("/api/webinars/{id}/complete", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @WithMockUser(username="testteacher", roles = "TEACHER")
    void cancelWebinar_ShouldReturnUpdatedWebinar() throws Exception {
        mockMvc.perform(post("/api/webinars/{id}/cancel", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @WithMockUser(username="testteacher", roles = "TEACHER")
    void addParticipant_ShouldReturnUpdatedWebinar() throws Exception {
        mockMvc.perform(post("/api/webinars/{id}/participants/{userId}", 
                testWebinar.getId(), testStudent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants", hasSize(1)));
    }

    @Test
    @WithMockUser(username="testteacher", roles = "TEACHER")
    void removeParticipant_ShouldReturnUpdatedWebinar() throws Exception {
        // Сначала добавляем участника
        webinarService.addParticipant(testWebinar.getId(), testStudent.getId());
        
        mockMvc.perform(delete("/api/webinars/{id}/participants/{userId}", 
                testWebinar.getId(), testStudent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants", hasSize(0)));
    }

    @Test
    @WithMockUser(username="testadmin", roles = "ADMIN")
    void deactivateWebinar_ShouldDeactivateWebinar() throws Exception {
        mockMvc.perform(post("/api/webinars/{id}/deactivate", testWebinar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @WithMockUser(username="teststudent", roles = "STUDENT")
    void getWebinarsByParticipant_ShouldReturnWebinars() throws Exception {
        // Сначала добавляем участника
        webinarService.addParticipant(testWebinar.getId(), testStudent.getId());
        
        mockMvc.perform(get("/api/webinars/participant/{participantId}", testStudent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testWebinar.getId().intValue())));
    }

    // НОВЫЕ ТЕСТЫ для новых endpoints
    @Test
    @WithMockUser(username="teststudent", roles = "STUDENT")
    void joinWebinar_WithStudentRole_ShouldJoinSuccessfully() throws Exception {
        mockMvc.perform(post("/api/webinars/{id}/join", testWebinar.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.joined", is(true)))
                .andExpect(jsonPath("$.message", containsString("успешно записались")));
    }

    @Test
    @WithMockUser(username="teststudent", roles = "STUDENT")
    void leaveWebinar_WithStudentRole_ShouldLeaveSuccessfully() throws Exception {
        // Сначала записываемся на вебинар
        webinarService.addParticipant(testWebinar.getId(), testStudent);
        
        mockMvc.perform(delete("/api/webinars/{id}/join", testWebinar.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.joined", is(false)))
                .andExpect(jsonPath("$.message", containsString("отписались")));
    }

    @Test
    @WithMockUser(username="teststudent", roles = "STUDENT")
    void getParticipationStatus_WithStudentRole_ShouldReturnStatus() throws Exception {
        mockMvc.perform(get("/api/webinars/{id}/participation-status", testWebinar.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joined", is(false)))
                .andExpect(jsonPath("$.webinarId", is(testWebinar.getId().intValue())))
                .andExpect(jsonPath("$.username", is("teststudent")));
    }
} 