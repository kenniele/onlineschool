package com.onlineSchool;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.onlineSchool.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
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
import com.onlineSchool.model.WebinarStatus;
import com.onlineSchool.service.CommentService;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.LikeService;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {OnlineSchoolApplication.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@Transactional
@Rollback
// Убираем @Sql так как schema.sql использует MySQL синтаксис AUTO_INCREMENT
// Позволяем Hibernate создавать схему автоматически для тестов
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
    
    // Счетчики для уникальных значений
    private static final AtomicLong userCounter = new AtomicLong(0);
    private static final AtomicLong courseCounter = new AtomicLong(0);
    private static final AtomicLong webinarCounter = new AtomicLong(0);
    
    @BeforeEach
    protected void setUpBase() {
        // Очистка данных перед каждым тестом
        clearTestData();
    }
    
    /**
     * Создает тестового пользователя с уникальным именем
     */
    protected User createTestUser(String username) {
        long timestamp = System.currentTimeMillis();
        User user = new User();
        user.setUsername(username + timestamp);
        user.setEmail(username + timestamp + "@test.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.STUDENT);
        return userService.save(user);
    }
    
    /**
     * Создает тестового учителя
     */
    protected User createTestTeacher(String username) {
        long timestamp = System.currentTimeMillis();
        User user = new User();
        user.setUsername(username + timestamp);
        user.setEmail(username + timestamp + "@teacher.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("Teacher");
        user.setRole(Role.TEACHER);
        return userService.save(user);
    }
    
    /**
     * Создает тестового администратора
     */
    protected User createTestAdmin(String username) {
        long timestamp = System.currentTimeMillis();
        User user = new User();
        user.setUsername(username + timestamp);
        user.setEmail(username + timestamp + "@admin.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("Admin");
        user.setRole(Role.ADMIN);
        return userService.save(user);
    }
    
    /**
     * Создает тестового пользователя с указанной ролью
     */
    protected User createTestUser(Role role) {
        long counter = userCounter.incrementAndGet();
        long timestamp = System.currentTimeMillis();
        User user = new User();
        user.setUsername(role.name().toLowerCase() + "_" + counter + "_" + timestamp);
        user.setEmail(role.name().toLowerCase() + "_" + counter + "_" + timestamp + "@example.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName(role.name());
        user.setRole(role);
        return userService.save(user);
    }
    
    /**
     * Создает тестового пользователя с уникальными данными
     */
    protected User createTestUser() {
        long counter = userCounter.incrementAndGet();
        return createTestUser("testuser_" + counter);
    }
    
    /**
     * Создает тестового преподавателя
     */
    protected User createTestTeacher() {
        return createTestUser(Role.TEACHER);
    }
    
    /**
     * Создает тестового администратора
     */
    protected User createTestAdmin() {
        return createTestUser(Role.ADMIN);
    }
    
    /**
     * Создает тестовый курс с уникальными данными
     */
    protected Course createTestCourse(User teacher) {
        long counter = courseCounter.incrementAndGet();
        long timestamp = System.currentTimeMillis();
        
        Course course = new Course();
        course.setTitle("Test Course " + counter + " " + timestamp);
        course.setDescription("This is a test course " + counter);
        course.setPrice(199.99 + counter);
        course.setTeacher(teacher);
        course.setStartDate(LocalDateTime.now().plusDays(counter));
        course.setEndDate(LocalDateTime.now().plusMonths(3).plusDays(counter));
        course.setActive(true);
        course.setWebinars(new ArrayList<>());
        course.setStudents(new HashSet<>());
        return courseService.save(course);
    }
    
    /**
     * Создает тестовый курс для указанного преподавателя
     */
    protected Course createTestCourse() {
        User teacher = createTestTeacher();
        return createTestCourse(teacher);
    }
    
    /**
     * Создает тестовый вебинар с уникальными данными
     */
    protected Webinar createTestWebinar(Course course, User teacher) {
        long counter = webinarCounter.incrementAndGet();
        long timestamp = System.currentTimeMillis();
        
        Webinar webinar = new Webinar();
        webinar.setTitle("Test Webinar " + counter + " " + timestamp);
        webinar.setDescription("This is a test webinar " + counter);
        webinar.setCourse(course);
        webinar.setTeacher(teacher);
        webinar.setStartTime(LocalDateTime.now().plusDays(1).plusHours(counter));
        webinar.setDuration(60 + (int)(counter % 120));
        webinar.setMaxParticipants(50 + (int)(counter % 50));
        webinar.setActive(true);
        webinar.setStatus(WebinarStatus.SCHEDULED);
        return webinarService.save(webinar);
    }
    
    /**
     * Создает тестовый вебинар для курса
     */
    protected Webinar createTestWebinar(Course course) {
        return createTestWebinar(course, course.getTeacher());
    }
    
    /**
     * Создает независимый тестовый вебинар
     */
    protected Webinar createTestWebinar() {
        Course course = createTestCourse();
        return createTestWebinar(course);
    }
    
    /**
     * Создает тестовый комментарий
     */
    protected Comment createTestComment(User user, Long entityId, EntityType entityType) {
        long timestamp = System.currentTimeMillis();
        Comment comment = new Comment();
        comment.setContent("Test Comment " + timestamp);
        comment.setUser(user);
        comment.setEntityId(entityId);
        comment.setEntityType(entityType);
        return commentService.create(comment);
    }
    
    /**
     * Создает тестовый комментарий для вебинара
     */
    protected Comment createTestComment(User user, Webinar webinar) {
        return createTestComment(user, webinar.getId(), EntityType.WEBINAR);
    }
    
    /**
     * Создает тестовый лайк
     */
    protected Like createTestLike(User user, Long entityId, EntityType entityType) {
        Like like = new Like();
        like.setUser(user);
        like.setEntityId(entityId);
        like.setEntityType(entityType);
        return likeService.createLike(like);
    }
    
    /**
     * Создает тестовый лайк для вебинара
     */
    protected Like createTestLike(User user, Webinar webinar) {
        return createTestLike(user, webinar.getId(), EntityType.WEBINAR);
    }
    
    /**
     * Очищает тестовые данные
     */
    protected void clearTestData() {
        try {
            // Транзакция автоматически откатится после теста
            // благодаря @Rollback аннотации
        } catch (Exception e) {
            // Игнорируем ошибки очистки
        }
    }
} 