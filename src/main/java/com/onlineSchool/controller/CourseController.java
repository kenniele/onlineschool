package com.onlineSchool.controller;

import com.onlineSchool.model.Course;
import com.onlineSchool.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Course>> getAllActiveCourses() {
        return ResponseEntity.ok(courseService.getAllActiveCourses());
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Course>> getCoursesByTeacherId(@PathVariable Long teacherId) {
        return ResponseEntity.ok(courseService.getCoursesByTeacherId(teacherId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Course>> getCoursesByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.getCoursesByStudentId(studentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.ok(courseService.save(course));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody Course course) {
        return ResponseEntity.ok(courseService.update(id, course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Course> activateCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.activate(id));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Course> deactivateCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.deactivate(id));
    }

    @PostMapping("/{id}/students/{studentId}")
    public ResponseEntity<Course> addStudent(
            @PathVariable Long id,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.addStudent(id, studentId));
    }

    @DeleteMapping("/{id}/students/{studentId}")
    public ResponseEntity<Course> removeStudent(
            @PathVariable Long id,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.removeStudent(id, studentId));
    }
} 