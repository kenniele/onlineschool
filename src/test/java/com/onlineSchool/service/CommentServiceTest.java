package com.onlineSchool.service;

import com.onlineSchool.BaseIntegrationTest;
import com.onlineSchool.model.Comment;
import com.onlineSchool.model.Course;
import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.User;
import com.onlineSchool.model.Webinar;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentServiceTest extends BaseIntegrationTest {

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private WebinarService webinarService;
    
    private User testUser;
    private Webinar testWebinar;
    private Comment testComment;
    private Course testCourse;
    
    @BeforeEach
    public void setUp() {
        // Используем методы из BaseIntegrationTest
        testUser = createTestUser("testUser");
        testCourse = createTestCourse(testUser);
        testWebinar = createTestWebinar(testCourse);
        
        // Создаем тестовый комментарий
        testComment = Comment.builder()
                .content("Test Comment")
                .user(testUser)
                .entityType(EntityType.WEBINAR)
                .entityId(testWebinar.getId())
                .build();
        testComment = commentService.create(testComment);
    }
    
    @Test
    void findByWebinarId_ShouldReturnComments() {
        List<Comment> comments = commentService.findByWebinarId(testWebinar.getId());
        
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        assertEquals(testComment.getContent(), comments.get(0).getContent());
    }
    
    @Test
    void findByAuthorId_ShouldReturnComments() {
        List<Comment> comments = commentService.findByAuthorId(testUser.getId());
        
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        assertEquals(testComment.getContent(), comments.get(0).getContent());
    }
    
    @Test
    void createComment_ShouldSaveComment() {
        Comment newComment = Comment.builder()
                .content("New Comment")
                .user(testUser)
                .entityType(EntityType.WEBINAR)
                .entityId(testWebinar.getId())
                .build();
                
        Comment savedComment = commentService.create(newComment);
        
        assertNotNull(savedComment.getId());
        assertEquals("New Comment", savedComment.getContent());
    }
    
    @Test
    void updateComment_ShouldUpdateContent() {
        testComment.setContent("Updated Content");
        Comment updatedComment = commentService.update(testComment.getId(), testComment);
        
        assertEquals("Updated Content", updatedComment.getContent());
    }
    
    @Test
    void deleteComment_ShouldRemoveComment() {
        commentService.delete(testComment.getId());
        
        assertThrows(EntityNotFoundException.class, () -> 
            commentService.findById(testComment.getId())
        );
    }
    
    @Test
    void isCommentOwner_ShouldReturnTrue_WhenUserIsOwner() {
        boolean isOwner = commentService.isUserCommentOwner(testUser.getId(), testComment.getId());
        
        assertTrue(isOwner);
    }
    
    @Test
    void isCommentOwner_ShouldReturnFalse_WhenUserIsNotOwner() {
        User anotherUser = createTestUser("anotherUser");
        
        boolean isOwner = commentService.isUserCommentOwner(anotherUser.getId(), testComment.getId());
        
        assertFalse(isOwner);
    }
} 