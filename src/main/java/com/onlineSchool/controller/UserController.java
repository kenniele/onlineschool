package com.onlineSchool.controller;

import com.onlineSchool.model.User;
import com.onlineSchool.model.Role;
import com.onlineSchool.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> userData) {
        if (!hasAdminRole()) {
            return ResponseEntity.status(403).build();
        }
        
        if (userData.get("username") == null || userData.get("username").isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (userData.get("email") == null || userData.get("email").isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (userData.get("firstName") == null || userData.get("firstName").isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (userData.get("lastName") == null || userData.get("lastName").isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (userData.get("password") == null || userData.get("password").isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        if (userService.existsByEmail(userData.get("email"))) {
            return ResponseEntity.badRequest().body(null);
        }
        User savedUser = userService.save(userData);
        return ResponseEntity.created(URI.create("/api/users/" + savedUser.getId())).body(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!hasAdminRole()) {
            return ResponseEntity.status(403).build();
        }
        
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            User updatedUser = userService.update(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    userService.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateOwnProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody User user) {
        try {
            User updatedUser = userService.updateByUsername(userDetails.getUsername(), user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean hasAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
}