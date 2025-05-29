package com.onlineSchool.service;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.*;
import com.onlineSchool.repository.*;
import com.onlineSchool.service.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки каскадного удаления через JPA @OnDelete CASCADE
 */
public class CascadeDeletionTest extends BaseIntegrationTest {

    @Autowired
    private CourseService courseService;
    
    @Autowired
    private WebinarService webinarService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private LikeService likeService;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private LikeRepository likeRepository;
    
    @Autowired
    private WebinarRepository webinarRepository;

    @Autowired
    private UserCourseEnrollmentRepository userCourseEnrollmentRepository;

    @Autowired
    private UserCourseEnrollmentService userCourseEnrollmentService;

    private User testTeacher;
    private User testStudent;
    private Course testCourse;
    private Webinar testWebinar;

    @BeforeEach
    public void setUp() {
        // Создаем тестовых пользователей
        testTeacher = createTestUser("cascadeTeacher");
        testStudent = createTestUser("cascadeStudent");
        
        // Создаем тестовый курс
        testCourse = Course.builder()
                .title("Test Cascade Course")
                .description("Course for cascade testing")
                .teacher(testTeacher)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();
        testCourse = courseService.save(testCourse);
        
        // Создаем тестовый вебинар
        testWebinar = Webinar.builder()
                .title("Test Cascade Webinar")
                .description("Webinar for cascade testing")
                .course(testCourse)
                .teacher(testTeacher)
                .startTime(LocalDateTime.now().plusDays(2))
                .duration(60)
                .status(WebinarStatus.SCHEDULED)
                .active(true)
                .build();
        testWebinar = webinarService.save(testWebinar);
    }

    @Test
    public void testCourseDeleteCascadesCommentsAndLikes() {
        // Создаем комментарии к курсу
        Comment courseComment1 = commentService.createComment(testStudent.getId(), EntityType.COURSE, testCourse.getId(), "Отличный курс!");
        Comment courseComment2 = commentService.createComment(testTeacher.getId(), EntityType.COURSE, testCourse.getId(), "Спасибо за отзыв!");
        
        // Создаем лайки к курсу
        Like courseLike1 = likeService.like(testStudent.getId(), testCourse.getId(), EntityType.COURSE);
        Like courseLike2 = likeService.like(testTeacher.getId(), testCourse.getId(), EntityType.COURSE);
        
        // Проверяем что комментарии и лайки созданы
        List<Comment> commentsBeforeDelete = commentService.getCommentsByEntity(EntityType.COURSE, testCourse.getId());
        List<Like> likesBeforeDelete = likeService.getLikesByEntity(EntityType.COURSE, testCourse.getId());
        
        assertEquals(2, commentsBeforeDelete.size(), "Должно быть 2 комментария к курсу");
        assertEquals(2, likesBeforeDelete.size(), "Должно быть 2 лайка к курсу");
        
        // Удаляем курс
        courseService.deleteById(testCourse.getId());
        
        // Проверяем что курс удален
        assertFalse(courseService.findById(testCourse.getId()).isPresent(), "Курс должен быть удален");
        
        // Проверяем что комментарии каскадно удалены
        List<Comment> commentsAfterDelete = commentService.getCommentsByEntity(EntityType.COURSE, testCourse.getId());
        assertEquals(0, commentsAfterDelete.size(), "Комментарии к курсу должны быть каскадно удалены");
        
        // Проверяем что лайки каскадно удалены
        List<Like> likesAfterDelete = likeService.getLikesByEntity(EntityType.COURSE, testCourse.getId());
        assertEquals(0, likesAfterDelete.size(), "Лайки к курсу должны быть каскадно удалены");
        
        // Проверяем через репозитории напрямую
        assertFalse(commentRepository.existsById(courseComment1.getId()), "Комментарий 1 должен быть удален");
        assertFalse(commentRepository.existsById(courseComment2.getId()), "Комментарий 2 должен быть удален");
        assertFalse(likeRepository.existsById(courseLike1.getId()), "Лайк 1 должен быть удален");
        assertFalse(likeRepository.existsById(courseLike2.getId()), "Лайк 2 должен быть удален");
    }

    @Test
    public void testWebinarDeleteCascadesCommentsAndLikes() {
        // Создаем комментарии к вебинару
        Comment webinarComment1 = commentService.createComment(testStudent.getId(), EntityType.WEBINAR, testWebinar.getId(), "Интересный вебинар!");
        Comment webinarComment2 = commentService.createComment(testTeacher.getId(), EntityType.WEBINAR, testWebinar.getId(), "Благодарю за участие!");
        
        // Создаем лайки к вебинару
        Like webinarLike1 = likeService.like(testStudent.getId(), testWebinar.getId(), EntityType.WEBINAR);
        Like webinarLike2 = likeService.like(testTeacher.getId(), testWebinar.getId(), EntityType.WEBINAR);
        
        // Проверяем что комментарии и лайки созданы
        List<Comment> commentsBeforeDelete = commentService.getCommentsByEntity(EntityType.WEBINAR, testWebinar.getId());
        List<Like> likesBeforeDelete = likeService.getLikesByEntity(EntityType.WEBINAR, testWebinar.getId());
        
        assertEquals(2, commentsBeforeDelete.size(), "Должно быть 2 комментария к вебинару");
        assertEquals(2, likesBeforeDelete.size(), "Должно быть 2 лайка к вебинару");
        
        // Удаляем вебинар
        webinarService.deleteById(testWebinar.getId());
        
        // Проверяем что вебинар удален
        assertFalse(webinarService.findById(testWebinar.getId()).isPresent(), "Вебинар должен быть удален");
        
        // Проверяем что комментарии каскадно удалены
        List<Comment> commentsAfterDelete = commentService.getCommentsByEntity(EntityType.WEBINAR, testWebinar.getId());
        assertEquals(0, commentsAfterDelete.size(), "Комментарии к вебинару должны быть каскадно удалены");
        
        // Проверяем что лайки каскадно удалены
        List<Like> likesAfterDelete = likeService.getLikesByEntity(EntityType.WEBINAR, testWebinar.getId());
        assertEquals(0, likesAfterDelete.size(), "Лайки к вебинару должны быть каскадно удалены");
        
        // Проверяем через репозитории напрямую
        assertFalse(commentRepository.existsById(webinarComment1.getId()), "Комментарий 1 должен быть удален");
        assertFalse(commentRepository.existsById(webinarComment2.getId()), "Комментарий 2 должен быть удален");
        assertFalse(likeRepository.existsById(webinarLike1.getId()), "Лайк 1 должен быть удален");
        assertFalse(likeRepository.existsById(webinarLike2.getId()), "Лайк 2 должен быть удален");
    }

    @Test
    public void testCourseDeleteCascadesWebinarsAndTheirCommentsAndLikes() {
        // Создаем второй вебинар для курса
        Webinar testWebinar2 = Webinar.builder()
                .title("Second Test Webinar")
                .description("Second webinar for cascade testing")
                .course(testCourse)
                .teacher(testTeacher)
                .startTime(LocalDateTime.now().plusDays(3))
                .duration(90)
                .status(WebinarStatus.SCHEDULED)
                .active(true)
                .build();
        testWebinar2 = webinarService.save(testWebinar2);
        
        // Создаем комментарии и лайки к курсу
        Comment courseComment = commentService.createComment(testStudent.getId(), EntityType.COURSE, testCourse.getId(), "Курс супер!");
        Like courseLike = likeService.like(testStudent.getId(), testCourse.getId(), EntityType.COURSE);
        
        // Создаем комментарии и лайки к первому вебинару
        Comment webinar1Comment = commentService.createComment(testStudent.getId(), EntityType.WEBINAR, testWebinar.getId(), "Первый вебинар отличный!");
        Like webinar1Like = likeService.like(testStudent.getId(), testWebinar.getId(), EntityType.WEBINAR);
        
        // Создаем комментарии и лайки ко второму вебинару
        Comment webinar2Comment = commentService.createComment(testStudent.getId(), EntityType.WEBINAR, testWebinar2.getId(), "Второй вебинар тоже хорош!");
        Like webinar2Like = likeService.like(testStudent.getId(), testWebinar2.getId(), EntityType.WEBINAR);
        
        // Проверяем что все создано
        assertTrue(courseService.findById(testCourse.getId()).isPresent(), "Курс должен существовать");
        assertTrue(webinarService.findById(testWebinar.getId()).isPresent(), "Первый вебинар должен существовать");
        assertTrue(webinarService.findById(testWebinar2.getId()).isPresent(), "Второй вебинар должен существовать");
        
        assertEquals(1, commentService.getCommentsByEntity(EntityType.COURSE, testCourse.getId()).size(), "Должен быть 1 комментарий к курсу");
        assertEquals(1, commentService.getCommentsByEntity(EntityType.WEBINAR, testWebinar.getId()).size(), "Должен быть 1 комментарий к первому вебинару");
        assertEquals(1, commentService.getCommentsByEntity(EntityType.WEBINAR, testWebinar2.getId()).size(), "Должен быть 1 комментарий ко второму вебинару");
        
        // Удаляем курс
        courseService.deleteById(testCourse.getId());
        
        // Проверяем что курс удален
        assertFalse(courseService.findById(testCourse.getId()).isPresent(), "Курс должен быть удален");
        
        // Проверяем что вебинары каскадно удалены
        assertFalse(webinarService.findById(testWebinar.getId()).isPresent(), "Первый вебинар должен быть каскадно удален");
        assertFalse(webinarService.findById(testWebinar2.getId()).isPresent(), "Второй вебинар должен быть каскадно удален");
        
        // Проверяем что комментарии курса каскадно удалены
        assertEquals(0, commentService.getCommentsByEntity(EntityType.COURSE, testCourse.getId()).size(), "Комментарии к курсу должны быть каскадно удалены");
        
        // Проверяем что комментарии вебинаров каскадно удалены
        assertEquals(0, commentService.getCommentsByEntity(EntityType.WEBINAR, testWebinar.getId()).size(), "Комментарии к первому вебинару должны быть каскадно удалены");
        assertEquals(0, commentService.getCommentsByEntity(EntityType.WEBINAR, testWebinar2.getId()).size(), "Комментарии ко второму вебинару должны быть каскадно удалены");
        
        // Проверяем что лайки каскадно удалены
        assertEquals(0, likeService.getLikesByEntity(EntityType.COURSE, testCourse.getId()).size(), "Лайки к курсу должны быть каскадно удалены");
        assertEquals(0, likeService.getLikesByEntity(EntityType.WEBINAR, testWebinar.getId()).size(), "Лайки к первому вебинару должны быть каскадно удалены");
        assertEquals(0, likeService.getLikesByEntity(EntityType.WEBINAR, testWebinar2.getId()).size(), "Лайки ко второму вебинару должны быть каскадно удалены");
        
        // Проверяем через репозитории напрямую
        assertFalse(commentRepository.existsById(courseComment.getId()), "Комментарий к курсу должен быть удален");
        assertFalse(commentRepository.existsById(webinar1Comment.getId()), "Комментарий к первому вебинару должен быть удален");
        assertFalse(commentRepository.existsById(webinar2Comment.getId()), "Комментарий ко второму вебинару должен быть удален");
        assertFalse(likeRepository.existsById(courseLike.getId()), "Лайк к курсу должен быть удален");
        assertFalse(likeRepository.existsById(webinar1Like.getId()), "Лайк к первому вебинару должен быть удален");
        assertFalse(likeRepository.existsById(webinar2Like.getId()), "Лайк ко второму вебинару должен быть удален");
        assertFalse(webinarRepository.existsById(testWebinar.getId()), "Первый вебинар должен быть удален");
        assertFalse(webinarRepository.existsById(testWebinar2.getId()), "Второй вебинар должен быть удален");
    }

    @Test
    public void testMultipleCascadeDeletions() {
        // Создаем несколько курсов и вебинаров с комментариями и лайками
        Course course2 = Course.builder()
                .title("Second Cascade Course")
                .description("Second course for cascade testing")
                .teacher(testTeacher)
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(35))
                .active(true)
                .build();
        course2 = courseService.save(course2);
        
        // Создаем комментарии и лайки к обоим курсам
        Comment course1Comment = commentService.createComment(testStudent.getId(), EntityType.COURSE, testCourse.getId(), "Первый курс классный!");
        Comment course2Comment = commentService.createComment(testStudent.getId(), EntityType.COURSE, course2.getId(), "Второй курс тоже хорош!");
        
        Like course1Like = likeService.like(testStudent.getId(), testCourse.getId(), EntityType.COURSE);
        Like course2Like = likeService.like(testStudent.getId(), course2.getId(), EntityType.COURSE);
        
        // Создаем комментарии и лайки к вебинару первого курса
        Comment webinarComment = commentService.createComment(testStudent.getId(), EntityType.WEBINAR, testWebinar.getId(), "Вебинар интересный!");
        Like webinarLike = likeService.like(testStudent.getId(), testWebinar.getId(), EntityType.WEBINAR);
        
        // Проверяем начальное состояние
        assertEquals(1, commentService.getCommentsByEntity(EntityType.COURSE, testCourse.getId()).size());
        assertEquals(1, commentService.getCommentsByEntity(EntityType.COURSE, course2.getId()).size());
        assertEquals(1, commentService.getCommentsByEntity(EntityType.WEBINAR, testWebinar.getId()).size());
        assertEquals(1, likeService.getLikesByEntity(EntityType.COURSE, testCourse.getId()).size());
        assertEquals(1, likeService.getLikesByEntity(EntityType.COURSE, course2.getId()).size());
        assertEquals(1, likeService.getLikesByEntity(EntityType.WEBINAR, testWebinar.getId()).size());
        
        // Удаляем первый курс
        courseService.deleteById(testCourse.getId());
        
        // Проверяем что первый курс и его связанные данные удалены
        assertFalse(courseService.findById(testCourse.getId()).isPresent());
        assertFalse(webinarService.findById(testWebinar.getId()).isPresent());
        assertEquals(0, commentService.getCommentsByEntity(EntityType.COURSE, testCourse.getId()).size());
        assertEquals(0, commentService.getCommentsByEntity(EntityType.WEBINAR, testWebinar.getId()).size());
        assertEquals(0, likeService.getLikesByEntity(EntityType.COURSE, testCourse.getId()).size());
        assertEquals(0, likeService.getLikesByEntity(EntityType.WEBINAR, testWebinar.getId()).size());
        
        // Проверяем что второй курс и его данные остались нетронутыми
        assertTrue(courseService.findById(course2.getId()).isPresent());
        assertEquals(1, commentService.getCommentsByEntity(EntityType.COURSE, course2.getId()).size());
        assertEquals(1, likeService.getLikesByEntity(EntityType.COURSE, course2.getId()).size());
        
        // Удаляем второй курс
        courseService.deleteById(course2.getId());
        
        // Проверяем что второй курс тоже удален
        assertFalse(courseService.findById(course2.getId()).isPresent());
        assertEquals(0, commentService.getCommentsByEntity(EntityType.COURSE, course2.getId()).size());
        assertEquals(0, likeService.getLikesByEntity(EntityType.COURSE, course2.getId()).size());
    }

    @Test
    public void testDeleteEmptyCourse() {
        // Создаем курс без вебинаров, комментариев и лайков
        Course emptyCourse = Course.builder()
                .title("Empty Course")
                .description("Course without any content")
                .teacher(testTeacher)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();
        emptyCourse = courseService.save(emptyCourse);
        
        // Проверяем что курс существует
        assertTrue(courseService.findById(emptyCourse.getId()).isPresent());
        
        // Удаляем пустой курс
        courseService.deleteById(emptyCourse.getId());
        
        // Проверяем что курс удален
        assertFalse(courseService.findById(emptyCourse.getId()).isPresent());
    }

    @Test
    public void testDeleteEmptyWebinar() {
        // Создаем вебинар без комментариев и лайков
        Webinar emptyWebinar = Webinar.builder()
                .title("Empty Webinar")
                .description("Webinar without comments or likes")
                .course(testCourse)
                .teacher(testTeacher)
                .startTime(LocalDateTime.now().plusDays(2))
                .duration(60)
                .status(WebinarStatus.SCHEDULED)
                .active(true)
                .build();
        emptyWebinar = webinarService.save(emptyWebinar);
        
        // Проверяем что вебинар существует
        assertTrue(webinarService.findById(emptyWebinar.getId()).isPresent());
        
        // Удаляем пустой вебинар
        webinarService.deleteById(emptyWebinar.getId());
        
        // Проверяем что вебинар удален
        assertFalse(webinarService.findById(emptyWebinar.getId()).isPresent());
    }

    @Test
    public void testDeleteNonExistentEntities() {
        // Тест удаления несуществующих сущностей
        Long nonExistentId = 999999L;
        
        // Проверяем что исключения выбрасываются правильно
        assertThrows(EntityNotFoundException.class, () -> {
            courseService.deleteById(nonExistentId);
        }, "Должно выбрасываться исключение при удалении несуществующего курса");
        
        assertThrows(EntityNotFoundException.class, () -> {
            webinarService.deleteById(nonExistentId);
        }, "Должно выбрасываться исключение при удалении несуществующего вебинара");
    }

    @Test
    public void testCascadeDeleteWithManyEntities() {
        // Создаем курс с множеством вебинаров, комментариев и лайков
        Course bigCourse = Course.builder()
                .title("Big Course")
                .description("Course with many webinars and interactions")
                .teacher(testTeacher)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(60))
                .active(true)
                .build();
        bigCourse = courseService.save(bigCourse);
        
        // Создаем 5 вебинаров
        List<Webinar> webinars = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Webinar webinar = Webinar.builder()
                    .title("Webinar " + i)
                    .description("Description " + i)
                    .course(bigCourse)
                    .teacher(testTeacher)
                    .startTime(LocalDateTime.now().plusDays(i))
                    .duration(60)
                    .status(WebinarStatus.SCHEDULED)
                    .active(true)
                    .build();
            webinars.add(webinarService.save(webinar));
        }
        
        // Создаем по 3 комментария и 2 лайка для каждого вебинара
        for (Webinar webinar : webinars) {
            for (int j = 1; j <= 3; j++) {
                commentService.createComment(testStudent.getId(), EntityType.WEBINAR, webinar.getId(), "Comment " + j + " for webinar " + webinar.getTitle());
            }
            likeService.like(testStudent.getId(), webinar.getId(), EntityType.WEBINAR);
            likeService.like(testTeacher.getId(), webinar.getId(), EntityType.WEBINAR);
        }
        
        // Создаем комментарии и лайки для курса
        for (int i = 1; i <= 5; i++) {
            commentService.createComment(testStudent.getId(), EntityType.COURSE, bigCourse.getId(), "Course comment " + i);
        }
        likeService.like(testStudent.getId(), bigCourse.getId(), EntityType.COURSE);
        likeService.like(testTeacher.getId(), bigCourse.getId(), EntityType.COURSE);
        
        // Проверяем что все создано
        assertEquals(5, webinarService.findByCourseId(bigCourse.getId()).size());
        assertEquals(5, commentService.getCommentsByEntity(EntityType.COURSE, bigCourse.getId()).size());
        assertEquals(2, likeService.getLikesByEntity(EntityType.COURSE, bigCourse.getId()).size());
        
        // Подсчитываем общее количество комментариев и лайков вебинаров
        int totalWebinarComments = 0;
        int totalWebinarLikes = 0;
        for (Webinar webinar : webinars) {
            totalWebinarComments += commentService.getCommentsByEntity(EntityType.WEBINAR, webinar.getId()).size();
            totalWebinarLikes += likeService.getLikesByEntity(EntityType.WEBINAR, webinar.getId()).size();
        }
        assertEquals(15, totalWebinarComments); // 5 вебинаров * 3 комментария
        assertEquals(10, totalWebinarLikes); // 5 вебинаров * 2 лайка
        
        // Удаляем курс
        courseService.deleteById(bigCourse.getId());
        
        // Проверяем что все удалено
        assertFalse(courseService.findById(bigCourse.getId()).isPresent());
        assertEquals(0, webinarService.findByCourseId(bigCourse.getId()).size());
        assertEquals(0, commentService.getCommentsByEntity(EntityType.COURSE, bigCourse.getId()).size());
        assertEquals(0, likeService.getLikesByEntity(EntityType.COURSE, bigCourse.getId()).size());
        
        // Проверяем что все вебинары и их данные удалены
        for (Webinar webinar : webinars) {
            assertFalse(webinarService.findById(webinar.getId()).isPresent());
            assertEquals(0, commentService.getCommentsByEntity(EntityType.WEBINAR, webinar.getId()).size());
            assertEquals(0, likeService.getLikesByEntity(EntityType.WEBINAR, webinar.getId()).size());
        }
    }

    @Test
    public void testCourseDeleteCascadesUserEnrollments() {
        // Создаем второго студента
        User testStudent2 = createTestUser(Role.STUDENT);
        testStudent2.setUsername("teststudent2");
        testStudent2 = userService.save(testStudent2);
        
        // Записываем студентов на курс
        UserCourseEnrollment enrollment1 = UserCourseEnrollment.builder()
                .user(testStudent)
                .course(testCourse)
                .status(EnrollmentStatus.IN_PROGRESS)
                .build();
        enrollment1 = userCourseEnrollmentRepository.save(enrollment1);
        
        UserCourseEnrollment enrollment2 = UserCourseEnrollment.builder()
                .user(testStudent2)
                .course(testCourse)
                .status(EnrollmentStatus.COMPLETED)
                .build();
        enrollment2 = userCourseEnrollmentRepository.save(enrollment2);
        
        // Проверяем что записи созданы
        assertEquals(2, userCourseEnrollmentRepository.findByCourse(testCourse).size(), "Должно быть 2 записи на курс");
        assertTrue(userCourseEnrollmentRepository.existsById(enrollment1.getId()), "Первая запись должна существовать");
        assertTrue(userCourseEnrollmentRepository.existsById(enrollment2.getId()), "Вторая запись должна существовать");
        
        // Удаляем курс
        courseService.deleteById(testCourse.getId());
        
        // Проверяем что курс удален
        assertFalse(courseService.findById(testCourse.getId()).isPresent(), "Курс должен быть удален");
        
        // Проверяем что записи на курс каскадно удалены
        assertEquals(0, userCourseEnrollmentRepository.findByCourse(testCourse).size(), "Записи на курс должны быть каскадно удалены");
        assertFalse(userCourseEnrollmentRepository.existsById(enrollment1.getId()), "Первая запись должна быть удалена");
        assertFalse(userCourseEnrollmentRepository.existsById(enrollment2.getId()), "Вторая запись должна быть удалена");
        
        // Проверяем что студенты остались
        assertTrue(userService.findById(testStudent.getId()).isPresent(), "Первый студент должен остаться");
        assertTrue(userService.findById(testStudent2.getId()).isPresent(), "Второй студент должен остаться");
    }

    @Test
    public void testUserDeleteCascadesEnrollmentsCommentsAndLikes() {
        // Создаем второй курс
        Course testCourse2 = Course.builder()
                .title("Test Course 2")
                .description("Second course for user deletion testing")
                .teacher(testTeacher)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(40))
                .active(true)
                .build();
        testCourse2 = courseService.save(testCourse2);
        
        // Записываем студента на оба курса
        UserCourseEnrollment enrollment1 = UserCourseEnrollment.builder()
                .user(testStudent)
                .course(testCourse)
                .status(EnrollmentStatus.IN_PROGRESS)
                .build();
        enrollment1 = userCourseEnrollmentRepository.save(enrollment1);
        
        UserCourseEnrollment enrollment2 = UserCourseEnrollment.builder()
                .user(testStudent)
                .course(testCourse2)
                .status(EnrollmentStatus.NOT_STARTED)
                .build();
        enrollment2 = userCourseEnrollmentRepository.save(enrollment2);
        
        // Создаем комментарии и лайки от студента
        Comment courseComment = commentService.createComment(testStudent.getId(), EntityType.COURSE, testCourse.getId(), "Отличный курс!");
        Comment webinarComment = commentService.createComment(testStudent.getId(), EntityType.WEBINAR, testWebinar.getId(), "Интересный вебинар!");
        
        Like courseLike = likeService.like(testStudent.getId(), testCourse.getId(), EntityType.COURSE);
        Like webinarLike = likeService.like(testStudent.getId(), testWebinar.getId(), EntityType.WEBINAR);
        
        // Проверяем что данные созданы
        assertEquals(2, userCourseEnrollmentRepository.findByUser(testStudent).size(), "Должно быть 2 записи студента");
        assertTrue(commentRepository.existsById(courseComment.getId()), "Комментарий к курсу должен существовать");
        assertTrue(commentRepository.existsById(webinarComment.getId()), "Комментарий к вебинару должен существовать");
        assertTrue(likeRepository.existsById(courseLike.getId()), "Лайк к курсу должен существовать");
        assertTrue(likeRepository.existsById(webinarLike.getId()), "Лайк к вебинару должен существовать");
        
        // Удаляем студента
        userService.deleteById(testStudent.getId());
        
        // Проверяем что студент удален
        assertFalse(userService.findById(testStudent.getId()).isPresent(), "Студент должен быть удален");
        
        // Проверяем что записи студента на курсы каскадно удалены
        assertEquals(0, userCourseEnrollmentRepository.findByUser(testStudent).size(), "Записи студента должны быть каскадно удалены");
        assertFalse(userCourseEnrollmentRepository.existsById(enrollment1.getId()), "Первая запись должна быть удалена");
        assertFalse(userCourseEnrollmentRepository.existsById(enrollment2.getId()), "Вторая запись должна быть удалена");
        
        // Проверяем что комментарии студента каскадно удалены
        assertFalse(commentRepository.existsById(courseComment.getId()), "Комментарий к курсу должен быть удален");
        assertFalse(commentRepository.existsById(webinarComment.getId()), "Комментарий к вебинару должен быть удален");
        
        // Проверяем что лайки студента каскадно удалены
        assertFalse(likeRepository.existsById(courseLike.getId()), "Лайк к курсу должен быть удален");
        assertFalse(likeRepository.existsById(webinarLike.getId()), "Лайк к вебинару должен быть удален");
        
        // Проверяем что курсы и вебинар остались
        assertTrue(courseService.findById(testCourse.getId()).isPresent(), "Первый курс должен остаться");
        assertTrue(courseService.findById(testCourse2.getId()).isPresent(), "Второй курс должен остаться");
        assertTrue(webinarService.findById(testWebinar.getId()).isPresent(), "Вебинар должен остаться");
    }
} 