package com.onlineSchool;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.onlineSchool.config.TestSecurityConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.onlineSchool.model.Comment;
import com.onlineSchool.model.Course;
import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.Like;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.service.CommentService;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.LikeService;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {OnlineSchoolApplication.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected UserService userService;
    
    @Autowired
    protected CourseService courseService;
    
    @Autowired
    protected WebinarService webinarService;
    
    @Autowired
    protected CommentService commentService;
    
    @Autowired
    protected LikeService likeService;
    
    /**
     * Создает тестового пользователя
     */
    protected User createTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        user.setActive(true);
        return userService.save(user);
    }
    
    /**
     * Создает тестового пользователя с указанным суффиксом
     */
    protected User createTestUser(String suffix) {
        User user = new User();
        user.setUsername("testuser_" + suffix);
        user.setEmail("test_" + suffix + "@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        user.setActive(true);
        return userService.save(user);
    }
    
    /**
     * Создает тестового преподавателя
     */
    protected User createTestTeacher() {
        User teacher = new User();
        teacher.setUsername("testteacher");
        teacher.setEmail("teacher@example.com");
        teacher.setPassword("password");
        teacher.setRole(Role.TEACHER);
        teacher.setActive(true);
        return userService.save(teacher);
    }
    
    /**
     * Создает тестовый курс
     */
    protected Course createTestCourse(User teacher) {
        Course course = new Course();
        course.setTitle("Test Course");
        course.setDescription("This is a test course");
        course.setPrice(199.99);
        course.setTeacher(teacher);
        course.setStartDate(LocalDateTime.now());
        course.setEndDate(LocalDateTime.now().plusMonths(3));
        course.setActive(true);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        course.setWebinars(new ArrayList<>());
        course.setStudents(new HashSet<>());
        return courseService.save(course);
    }
    
    /**
     * Создает тестовый вебинар
     */
    protected Webinar createTestWebinar(Course course, User teacher) {
        Webinar webinar = new Webinar();
        webinar.setTitle("Test Webinar");
        webinar.setDescription("This is a test webinar");
        webinar.setCourse(course);
        webinar.setTeacher(teacher);
        webinar.setStartTime(LocalDateTime.now().plusDays(1));
        webinar.setDuration(120);
        webinar.setActive(true);
        return webinarService.save(webinar);
    }
    
    /**
     * Создает тестовый вебинар для курса
     */
    protected Webinar createTestWebinar(Course course) {
        Webinar webinar = new Webinar();
        webinar.setTitle("Test Webinar");
        webinar.setDescription("This is a test webinar");
        webinar.setCourse(course);
        webinar.setTeacher(course.getTeacher());
        webinar.setStartTime(LocalDateTime.now().plusDays(1));
        webinar.setDuration(120);
        webinar.setActive(true);
        return webinarService.save(webinar);
    }
    
    /**
     * Создает тестовый комментарий
     */
    protected Comment createTestComment(User user, Webinar webinar) {
        Comment comment = new Comment();
        comment.setContent("Test Comment");
        comment.setUser(user);
        comment.setEntityId(webinar.getId());
        comment.setEntityType(EntityType.WEBINAR);
        comment.setCreatedAt(LocalDateTime.now());
        return commentService.create(comment);
    }
    
    /**
     * Создает тестовый лайк
     */
    protected Like createTestLike(User user, Webinar webinar) {
        Like like = new Like();
        like.setUser(user);
        like.setEntityId(webinar.getId());
        like.setEntityType(EntityType.WEBINAR);
        like.setCreatedAt(LocalDateTime.now());
        return likeService.createLike(like);
    }
    
    /**
     * Очищает тестовые данные
     * Можно реализовать при необходимости
     */
    protected void clearTestData() {
        // Реализация при необходимости
    }
} 