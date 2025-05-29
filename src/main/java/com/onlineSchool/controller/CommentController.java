package com.onlineSchool.controller;

import com.onlineSchool.model.Comment;
import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.User;
import com.onlineSchool.service.CommentService;
import com.onlineSchool.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

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

    // Получить все комментарии - доступно всем
    @GetMapping
    public List<Comment> getAll() {
        return commentService.findAll();
    }

    // Получить комментарий по ID - доступно всем
    @GetMapping("/{id}")
    public ResponseEntity<Comment> getById(@PathVariable Long id) {
        Optional<Comment> opt = commentService.getCommentById(id);
        return opt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Комментарии к конкретному вебинару - доступно всем
    @GetMapping("/webinar/{webinarId}")
    public List<Comment> getByWebinar(@PathVariable Long webinarId) {
        return commentService.getCommentsByEntity(EntityType.WEBINAR, webinarId);
    }

    // Комментарии конкретного пользователя - доступно всем
    @GetMapping("/user/{userId}")
    public List<Comment> getByUser(@PathVariable Long userId) {
        return commentService.getCommentsByUser(userId);
    }

    // Создать новый комментарий - только авторизованные пользователи
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Comment comment, Authentication authentication) {
        System.out.println("=== CREATE COMMENT DEBUG ===");
        System.out.println("Comment content: " + comment.getContent());
        System.out.println("Entity type: " + comment.getEntityType());
        System.out.println("Entity ID: " + comment.getEntityId());
        System.out.println("Username: " + authentication.getName());
        
        try {
            // Устанавливаем текущего пользователя как автора комментария
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
            comment.setUser(currentUser);
            
            System.out.println("User found: " + currentUser.getId() + " - " + currentUser.getUsername());
            
            Comment savedComment = commentService.create(comment);
            System.out.println("Comment saved with ID: " + savedComment.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Комментарий успешно добавлен");
            response.put("comment", savedComment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error in create comment: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка при добавлении комментария: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Обновить существующий комментарий - только автор или админ
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @commentService.isCommentAuthor(#id, authentication.name))")
    public ResponseEntity<Comment> update(
            @PathVariable Long id,
            @RequestBody Comment commentDetails,
            Authentication authentication) {

        try {
            // Не меняем автора комментария при обновлении
            Comment existingComment = commentService.getCommentById(id)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));
            commentDetails.setUser(existingComment.getUser());
            
            Comment updated = commentService.updateComment(id, commentDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Удалить комментарий - только автор или админ
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @commentService.isCommentAuthor(#id, authentication.name))")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}