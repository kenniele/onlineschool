package com.onlineSchool.controller;

import com.onlineSchool.model.Comment;
import com.onlineSchool.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // Получить все комментарии
    @GetMapping
    public List<Comment> getAll() {
        return commentRepository.findAll();
    }

    // Получить комментарий по ID
    @GetMapping("/{id}")
    public ResponseEntity<Comment> getById(@PathVariable Long id) {
        Optional<Comment> opt = commentRepository.findById(id);
        return opt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Комментарии к конкретному вебинару
    @GetMapping("/webinar/{webinarId}")
    public List<Comment> getByWebinar(@PathVariable Long webinarId) {
        return commentRepository.findByWebinarId(webinarId);
    }

    // Комментарии конкретного пользователя
    @GetMapping("/user/{userId}")
    public List<Comment> getByUser(@PathVariable Long userId) {
        return commentRepository.findByUserId(userId);
    }

    // Создать новый комментарий
    @PostMapping
    public Comment create(@RequestBody Comment comment) {
        return commentRepository.save(comment);
    }

    // Обновить существующий комментарий
    @PutMapping("/{id}")
    public ResponseEntity<Comment> update(
            @PathVariable Long id,
            @RequestBody Comment commentDetails) {

        return commentRepository.findById(id)
                .map(comment -> {
                    comment.setContent(commentDetails.getContent());
                    comment.setUser(commentDetails.getUser());
                    Comment updated = commentRepository.save(comment);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Удалить комментарий
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return commentRepository.findById(id)
                .map(comment -> {
                    commentRepository.delete(comment);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}