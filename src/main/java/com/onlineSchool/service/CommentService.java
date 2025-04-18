package com.onlineSchool.service;

import com.onlineSchool.model.Comment;
import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.User;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final WebinarService webinarService;

    @Transactional(readOnly = true)
    public List<Comment> findByWebinarId(Long webinarId) {
        return commentRepository.findByWebinarId(webinarId);
    }

    @Transactional(readOnly = true)
    public List<Comment> findByAuthorId(Long authorId) {
        return commentRepository.findByAuthorId(authorId);
    }

    @Transactional(readOnly = true)
    public List<Comment> findLatestByWebinarId(Long webinarId) {
        return commentRepository.findLatestByWebinarId(webinarId);
    }

    @Transactional
    public Comment create(Comment comment) {
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment update(Long id, Comment comment) {
        Comment existingComment = findById(id);
        existingComment.setContent(comment.getContent());
        return commentRepository.save(existingComment);
    }

    @Transactional
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));
    }

    @Transactional
    public Comment createComment(Long webinarId, Long authorId, String text) {
        User author = userService.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Webinar webinar = webinarService.findById(webinarId)
                .orElseThrow(() -> new EntityNotFoundException("Webinar not found"));
                
        Comment comment = new Comment();
        comment.setContent(text);
        comment.setUser(author);
        comment.setEntityType(EntityType.WEBINAR);
        comment.setEntityId(webinarId);
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByEntity(EntityType entityType, Long entityId) {
        return commentRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<Comment> getCommentsByUser(Long userId) {
        return commentRepository.findByUserId(userId);
    }

    public List<Comment> getCommentsByUserAndEntity(Long userId, EntityType entityType, Long entityId) {
        return commentRepository.findByUserIdAndEntityTypeAndEntityId(userId, entityType, entityId);
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    @Transactional
    public Comment createComment(Long userId, EntityType entityType, Long entityId, String content) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder()
                .user(user)
                .entityType(entityType)
                .entityId(entityId)
                .content(content)
                .build();

        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(Long id, Comment comment) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found");
        }
        comment.setId(id);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found");
        }
        commentRepository.deleteById(id);
    }

    public boolean isUserCommentOwner(Long userId, Long commentId) {
        return commentRepository.existsByIdAndUserId(commentId, userId);
    }
} 