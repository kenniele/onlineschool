package com.onlineSchool.controller;

import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.Like;
import com.onlineSchool.service.LikeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<Like>> getLikesByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(likeService.getLikesByEntity(entityType, entityId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #userId")
    public ResponseEntity<List<Like>> getLikesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(likeService.getLikesByUser(userId));
    }

    @GetMapping("/user/{userId}/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #userId")
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
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #userId")
    public ResponseEntity<Boolean> hasLiked(
            @PathVariable Long userId,
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(likeService.hasLiked(userId, entityId, entityType));
    }
}