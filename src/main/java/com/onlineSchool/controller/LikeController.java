package com.onlineSchool.controller;

import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.Like;
import com.onlineSchool.model.User;
import com.onlineSchool.service.LikeService;
import com.onlineSchool.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<Like>> getLikesByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(likeService.getLikesByEntity(entityType, entityId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<List<Like>> getLikesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(likeService.getLikesByUser(userId));
    }

    @GetMapping("/user/{userId}/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<List<Like>> getLikesByUserAndEntity(
            @PathVariable Long userId,
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(likeService.getLikesByUserAndEntity(userId, entityType, entityId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Like> getLikeById(@PathVariable Long id) {
        try {
            Like like = likeService.getLikeById(id);
            return ResponseEntity.ok(like);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Like> createLike(@RequestBody Like like) {
        if (like.getUser() != null && like.getUser().getId() != null) {
            return ResponseEntity.ok(likeService.createLike(like));
        }

        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteLike(@PathVariable Long id) {
        likeService.deleteLike(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/{entityType}/{entityId}")
    public ResponseEntity<Long> countLikes(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(likeService.countLikes(entityId, entityType));
    }

    @GetMapping("/has-liked/{userId}/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Boolean> hasLiked(
            @PathVariable Long userId,
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(likeService.hasLiked(userId, entityId, entityType));
    }

    @PostMapping("/toggle/{entityType}/{entityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        System.out.println("=== TOGGLE LIKE DEBUG ===");
        System.out.println("EntityType: " + entityType);
        System.out.println("EntityId: " + entityId);
        System.out.println("Username: " + userDetails.getUsername());
        
        try {
            // Получаем User через UserService, а не кастим напрямую
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            System.out.println("User found: " + user.getId() + " - " + user.getUsername());
            
            boolean hasLiked = likeService.hasLiked(user.getId(), entityId, entityType);
            System.out.println("Has liked before: " + hasLiked);
            
            Map<String, Object> response = new HashMap<>();
            
            if (hasLiked) {
                // Убираем лайк
                likeService.unlike(user.getId(), entityId, entityType);
                response.put("liked", false);
                response.put("message", "Лайк убран");
                System.out.println("Like removed");
            } else {
                // Ставим лайк
                likeService.like(user.getId(), entityId, entityType);
                response.put("liked", true);
                response.put("message", "Лайк поставлен");
                System.out.println("Like added");
            }
            
            // Получаем новое количество лайков
            long likesCount = likeService.countLikes(entityId, entityType);
            System.out.println("New likes count: " + likesCount);
            
            response.put("likesCount", likesCount);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error in toggleLike: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}