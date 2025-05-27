package com.onlineSchool.service;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LikeServiceTest extends BaseIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private WebinarService webinarService;

    private User testUser;
    private Webinar testWebinar;
    private Like testLike;

    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.TEACHER)
                .build();
        testUser = userService.save(testUser);

        testWebinar = Webinar.builder()
                .title("Test Webinar")
                .description("Test Description")
                .teacher(testUser)
                .startTime(LocalDateTime.now().plusDays(1))
                .duration(60)
                .maxParticipants(100)
                .status(WebinarStatus.SCHEDULED)
                .active(true)
                .build();
        testWebinar = webinarService.save(testWebinar);

        Course testCourse = createTestCourse(testUser);

        testLike = Like.builder()
                .user(testUser)
                .entityType(EntityType.COURSE)
                .entityId(testCourse.getId())
                .build();
        testLike = likeService.createLike(testLike);
    }

    @Test
    void getLikesByEntity_ShouldReturnLikes() {
        List<Like> likes = likeService.getLikesByEntity(EntityType.COURSE, testLike.getEntityId());
        assertFalse(likes.isEmpty());
        assertEquals(testLike.getId(), likes.get(0).getId());
    }

    @Test
    void getLikesByUser_ShouldReturnLikes() {
        List<Like> likes = likeService.getLikesByUser(testUser.getId());
        assertFalse(likes.isEmpty());
        assertEquals(testLike.getId(), likes.get(0).getId());
    }

    @Test
    void createLike_ShouldCreateNewLike() {
        Course newCourse = createTestCourse(testUser);
        
        Like newLike = Like.builder()
                .user(testUser)
                .entityType(EntityType.COURSE)
                .entityId(newCourse.getId())
                .build();

        Like saved = likeService.createLike(newLike);
        assertNotNull(saved.getId());
    }

    @Test
    void deleteLike_ShouldDeleteLike() {
        likeService.deleteLike(testLike.getId());
        assertThrows(EntityNotFoundException.class, () -> 
            likeService.getLikeById(testLike.getId())
        );
    }

    @Test
    void countLikes_ShouldReturnCorrectCount() {
        long count = likeService.countLikes(testLike.getEntityId(), EntityType.COURSE);
        assertEquals(1, count);
    }

    @Test
    void isLikeOwner_WhenUserIsOwner_ShouldReturnTrue() {
        assertTrue(likeService.isLikeOwner(testLike.getId(), testUser.getId()));
    }

    @Test
    void isLikeOwner_WhenUserIsNotOwner_ShouldReturnFalse() {
        User otherUser = User.builder()
                .username("otheruser")
                .password("password")
                .email("other@example.com")
                .firstName("Other")
                .lastName("User")
                .role(Role.USER)
                .build();
        otherUser = userService.save(otherUser);

        assertFalse(likeService.isLikeOwner(testLike.getId(), otherUser.getId()));
    }
} 