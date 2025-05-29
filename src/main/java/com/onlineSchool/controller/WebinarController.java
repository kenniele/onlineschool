package com.onlineSchool.controller;

import com.onlineSchool.model.User;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webinars")
@RequiredArgsConstructor
public class WebinarController {
    private final WebinarService webinarService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Webinar>> getAllWebinars() {
        return ResponseEntity.ok(webinarService.findAll());
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Webinar>> getWebinarsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(webinarService.findByCourseId(courseId));
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<Webinar>> getWebinarsByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(webinarService.findByTeacherId(teacherId));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Webinar>> getUpcomingWebinars() {
        return ResponseEntity.ok(webinarService.findUpcoming());
    }

    @GetMapping("/past")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Webinar>> getPastWebinars() {
        return ResponseEntity.ok(webinarService.findPast());
    }

    @GetMapping("/participant/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<Webinar>> getWebinarsByParticipant(@PathVariable Long userId) {
        return ResponseEntity.ok(webinarService.findByParticipantId(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Webinar> getWebinarById(@PathVariable Long id) {
        return webinarService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Webinar> createWebinar(
            @Valid @RequestBody Webinar webinar,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User teacher = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            webinar.setTeacher(teacher);
            return ResponseEntity.ok(webinarService.save(webinar));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Webinar> updateWebinar(
            @PathVariable Long id,
            @Valid @RequestBody Webinar webinar,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Проверяем права доступа - учитель может редактировать только свои вебинары
            Webinar existingWebinar = webinarService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Webinar not found"));
            
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Админы могут редактировать любые вебинары, учителя - только свои
            if (!currentUser.getRole().name().equals("ADMIN") && 
                !existingWebinar.getTeacher().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            return ResponseEntity.ok(webinarService.update(id, webinar));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWebinar(@PathVariable Long id) {
        try {
            webinarService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Webinar> startWebinar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Проверяем права - учитель может запускать только свои вебинары
            Webinar webinar = webinarService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Webinar not found"));
            
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!currentUser.getRole().name().equals("ADMIN") && 
                !webinar.getTeacher().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            return ResponseEntity.ok(webinarService.start(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Webinar> completeWebinar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Проверяем права - учитель может завершать только свои вебинары
            Webinar webinar = webinarService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Webinar not found"));
            
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!currentUser.getRole().name().equals("ADMIN") && 
                !webinar.getTeacher().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            return ResponseEntity.ok(webinarService.complete(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Webinar> cancelWebinar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Проверяем права - учитель может отменять только свои вебинары
            Webinar webinar = webinarService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Webinar not found"));
            
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!currentUser.getRole().name().equals("ADMIN") && 
                !webinar.getTeacher().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            return ResponseEntity.ok(webinarService.cancel(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // НОВЫЙ ENDPOINT: Самостоятельная запись студента на вебинар
    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> joinWebinar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            User student = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            webinarService.addParticipant(id, student);
            
            response.put("success", true);
            response.put("message", "Вы успешно записались на вебинар!");
            response.put("joined", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при записи на вебинар: " + e.getMessage());
            response.put("joined", false);
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    // НОВЫЙ ENDPOINT: Отписка студента от вебинара
    @DeleteMapping("/{id}/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> leaveWebinar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            User student = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            webinarService.removeParticipant(id, student);
            
            response.put("success", true);
            response.put("message", "Вы успешно отписались от вебинара!");
            response.put("joined", false);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при отписке от вебинара: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    // НОВЫЙ ENDPOINT: Проверка участия в вебинаре
    @GetMapping("/{id}/participation-status")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getParticipationStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isParticipant = webinarService.isUserParticipant(id, userDetails.getUsername());
            
            response.put("joined", isParticipant);
            response.put("webinarId", id);
            response.put("username", userDetails.getUsername());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("joined", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Административные методы для управления участниками
    @PostMapping("/{id}/participants/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Webinar> addParticipant(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Проверяем права - учитель может добавлять участников только в свои вебинары
            Webinar webinar = webinarService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Webinar not found"));
            
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!currentUser.getRole().name().equals("ADMIN") && 
                !webinar.getTeacher().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            return ResponseEntity.ok(webinarService.addParticipant(id, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/participants/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Webinar> removeParticipant(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Проверяем права - учитель может удалять участников только из своих вебинаров
            Webinar webinar = webinarService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Webinar not found"));
            
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!currentUser.getRole().name().equals("ADMIN") && 
                !webinar.getTeacher().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            return ResponseEntity.ok(webinarService.removeParticipant(id, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Webinar> deactivateWebinar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(webinarService.deactivate(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 