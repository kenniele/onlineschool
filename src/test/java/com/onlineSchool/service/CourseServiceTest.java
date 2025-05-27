package com.onlineSchool.service;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.Course;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CourseServiceTest extends BaseIntegrationTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    private User testTeacher;
    private Course testCourse;
    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
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
    }

    @Test
    void getAllCourses_ShouldReturnAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        assertFalse(courses.isEmpty());
        assertTrue(courses.stream().anyMatch(c -> c.getId().equals(testCourse.getId())));
    }

    @Test
    void getAllActiveCourses_ShouldReturnActiveCourses() {
        List<Course> courses = courseService.getAllActiveCourses();
        assertFalse(courses.isEmpty());
        assertTrue(courses.stream().anyMatch(c -> c.getId().equals(testCourse.getId())));
    }

    @Test
    void findById_WhenCourseExists_ShouldReturnCourse() {
        Optional<Course> found = courseService.findById(testCourse.getId());
        assertTrue(found.isPresent());
        assertEquals(testCourse.getTitle(), found.get().getTitle());
    }

    @Test
    void findById_WhenCourseNotExists_ShouldReturnEmpty() {
        Optional<Course> found = courseService.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void save_ShouldSaveAndReturnCourse() {
        Course newCourse = Course.builder()
                .title("New Course")
                .description("New Description")
                .price(149.99)
                .teacher(testTeacher)
                .startDate(now.plusDays(1))
                .endDate(now.plusMonths(4))
                .active(true)
                .build();

        Course saved = courseService.save(newCourse);
        assertNotNull(saved.getId());
        assertEquals("New Course", saved.getTitle());
    }

    @Test
    void update_WhenCourseExists_ShouldUpdateAndReturnCourse() {
        testCourse.setTitle("Updated Title");
        Course updated = courseService.update(testCourse.getId(), testCourse);
        assertEquals("Updated Title", updated.getTitle());
    }

    @Test
    void update_WhenCourseNotExists_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> 
            courseService.update(999L, testCourse)
        );
    }

    @Test
    void deleteById_ShouldDeleteCourse() {
        courseService.deleteById(testCourse.getId());
        assertFalse(courseService.findById(testCourse.getId()).isPresent());
    }

    @Test
    void deactivate_WhenCourseExists_ShouldDeactivateCourse() {
        courseService.deactivate(testCourse.getId());
        Optional<Course> deactivatedCourse = courseService.findById(testCourse.getId());
        assertTrue(deactivatedCourse.isPresent());
        assertFalse(deactivatedCourse.get().isActive());
    }

    @Test
    void deactivate_WhenCourseNotExists_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> 
            courseService.deactivate(999L)
        );
    }
} 