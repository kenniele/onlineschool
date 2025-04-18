package com.onlineSchool.controller;

import com.onlineSchool.model.Comment;
import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.User;
import com.onlineSchool.service.CommentService;
import com.onlineSchool.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    // Получить все комментарии
    @GetMapping
    public List<Comment> getAll() {
        return commentService.findAll();
    }

    // Получить комментарий по ID
    @GetMapping("/{id}")
    public ResponseEntity<Comment> getById(@PathVariable Long id) {
        Optional<Comment> opt = commentService.getCommentById(id);
        return opt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Комментарии к конкретному вебинару
    @GetMapping("/webinar/{webinarId}")
    public List<Comment> getByWebinar(@PathVariable Long webinarId) {
        return commentService.getCommentsByEntity(EntityType.WEBINAR, webinarId);
    }

    // Комментарии конкретного пользователя
    @GetMapping("/user/{userId}")
    public List<Comment> getByUser(@PathVariable Long userId) {
        return commentService.getCommentsByUser(userId);
    }

    // Создать новый комментарий
    @PostMapping
    public Comment create(@RequestBody Comment comment) {
        if (comment.getUser() == null && comment.getUserId() != null) {
            User user = userService.findById(comment.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + comment.getUserId()));
            comment.setUser(user);
        }
        return commentService.create(comment);
    }

    // Обновить существующий комментарий
    @PutMapping("/{id}")
    public ResponseEntity<Comment> update(
            @PathVariable Long id,
            @RequestBody Comment commentDetails) {

        try {
            if (commentDetails.getUser() == null && commentDetails.getUserId() != null) {
                User user = userService.findById(commentDetails.getUserId())
                        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + commentDetails.getUserId()));
                commentDetails.setUser(user);
            }
            Comment updated = commentService.updateComment(id, commentDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Удалить комментарий
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}