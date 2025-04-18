package com.onlineSchool.service;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WebinarServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private WebinarService webinarService;

    private User testTeacher;
    private Course testCourse;
    private Webinar testWebinar;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        testTeacher = User.builder()
                .username("testteacher")
                .password("password")
                .email("teacher@example.com")
                .firstName("Test")
                .lastName("Teacher")
                .role(Role.TEACHER)
                .build();
        testTeacher = userService.save(testTeacher);

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
                .build();
        testWebinar = webinarService.save(testWebinar);
    }

    @Test
    void findByCourseId_ShouldReturnWebinars() {
        List<Webinar> webinars = webinarService.findByCourseId(testCourse.getId());
        assertFalse(webinars.isEmpty());
        assertEquals(testWebinar.getId(), webinars.get(0).getId());
    }

    @Test
    void findByTeacherId_ShouldReturnWebinars() {
        List<Webinar> webinars = webinarService.findByTeacherId(testTeacher.getId());
        assertFalse(webinars.isEmpty());
        assertEquals(testWebinar.getId(), webinars.get(0).getId());
    }

    @Test
    void findUpcoming_ShouldReturnUpcomingWebinars() {
        List<Webinar> webinars = webinarService.findUpcoming();
        assertFalse(webinars.isEmpty());
        assertTrue(webinars.stream().anyMatch(w -> w.getId().equals(testWebinar.getId())));
    }

    @Test
    void findPast_ShouldReturnPastWebinars() {
        Webinar pastWebinar = Webinar.builder()
                .title("Past Webinar")
                .description("Past Description")
                .startTime(now.minusDays(1))
                .duration(60)
                .maxParticipants(100)
                .status(WebinarStatus.COMPLETED)
                .active(true)
                .course(testCourse)
                .teacher(testTeacher)
                .build();
        webinarService.save(pastWebinar);

        List<Webinar> webinars = webinarService.findPast();
        assertFalse(webinars.isEmpty());
        assertTrue(webinars.stream().anyMatch(w -> w.getTitle().equals("Past Webinar")));
    }

    @Test
    void findById_WhenWebinarExists_ShouldReturnWebinar() {
        Optional<Webinar> found = webinarService.findById(testWebinar.getId());
        assertTrue(found.isPresent());
        assertEquals(testWebinar.getTitle(), found.get().getTitle());
    }

    @Test
    void findById_WhenWebinarNotExists_ShouldReturnEmpty() {
        Optional<Webinar> found = webinarService.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void save_ShouldSaveAndReturnWebinar() {
        Webinar newWebinar = Webinar.builder()
                .title("New Webinar")
                .description("New Description")
                .startTime(now.plusDays(2))
                .duration(90)
                .maxParticipants(50)
                .status(WebinarStatus.SCHEDULED)
                .active(true)
                .course(testCourse)
                .teacher(testTeacher)
                .build();

        Webinar saved = webinarService.save(newWebinar);
        assertNotNull(saved.getId());
        assertEquals("New Webinar", saved.getTitle());
    }

    @Test
    void update_ShouldUpdateExistingWebinar() {
        testWebinar.setTitle("Updated Title");
        Webinar updated = webinarService.update(testWebinar.getId(), testWebinar);
        assertEquals("Updated Title", updated.getTitle());
    }

    @Test
    void update_WhenWebinarNotExists_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> webinarService.update(999L, testWebinar));
    }

    @Test
    void deleteById_ShouldDeleteWebinar() {
        webinarService.deleteById(testWebinar.getId());
        assertFalse(webinarService.findById(testWebinar.getId()).isPresent());
    }
} 