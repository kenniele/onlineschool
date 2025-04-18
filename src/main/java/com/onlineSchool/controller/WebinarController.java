package com.onlineSchool.controller;

import com.onlineSchool.model.Webinar;
import com.onlineSchool.service.WebinarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/webinars")
@RequiredArgsConstructor
public class WebinarController {
    private final WebinarService webinarService;

    @GetMapping
    public ResponseEntity<List<Webinar>> getAllWebinars() {
        return ResponseEntity.ok(webinarService.findAll());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Webinar>> getWebinarsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(webinarService.findByCourseId(courseId));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Webinar>> getWebinarsByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(webinarService.findByTeacherId(teacherId));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Webinar>> getUpcomingWebinars() {
        return ResponseEntity.ok(webinarService.findUpcoming());
    }

    @GetMapping("/past")
    public ResponseEntity<List<Webinar>> getPastWebinars() {
        return ResponseEntity.ok(webinarService.findPast());
    }

    @GetMapping("/participant/{userId}")
    public ResponseEntity<List<Webinar>> getWebinarsByParticipant(@PathVariable Long userId) {
        return ResponseEntity.ok(webinarService.findByParticipantId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Webinar> getWebinarById(@PathVariable Long id) {
        return webinarService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Webinar> createWebinar(@RequestBody Webinar webinar) {
        return ResponseEntity.ok(webinarService.save(webinar));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Webinar> updateWebinar(
            @PathVariable Long id,
            @RequestBody Webinar webinar) {
        return ResponseEntity.ok(webinarService.update(id, webinar));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebinar(@PathVariable Long id) {
        webinarService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Webinar> startWebinar(@PathVariable Long id) {
        return ResponseEntity.ok(webinarService.start(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Webinar> completeWebinar(@PathVariable Long id) {
        return ResponseEntity.ok(webinarService.complete(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Webinar> cancelWebinar(@PathVariable Long id) {
        return ResponseEntity.ok(webinarService.cancel(id));
    }

    @PostMapping("/{id}/participants/{userId}")
    public ResponseEntity<Webinar> addParticipant(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return ResponseEntity.ok(webinarService.addParticipant(id, userId));
    }

    @DeleteMapping("/{id}/participants/{userId}")
    public ResponseEntity<Webinar> removeParticipant(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return ResponseEntity.ok(webinarService.removeParticipant(id, userId));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateWebinar(@PathVariable Long id) {
        webinarService.deactivate(id);
        return ResponseEntity.ok().build();
    }
} 