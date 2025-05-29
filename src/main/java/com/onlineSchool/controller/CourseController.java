package com.onlineSchool.controller;

import com.onlineSchool.model.Course;
import com.onlineSchool.model.User;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.UserService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<Course>> getCoursesByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.getCoursesByStudentId(studentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Course> createCourse(
            @RequestBody Course course,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User teacher = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            course.setTeacher(teacher);
            Course savedCourse = courseService.save(course);
            return ResponseEntity.ok(savedCourse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody Course course,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Проверяем, что учитель может редактировать только свои курсы
            Course existingCourse = courseService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Админы могут редактировать любые курсы, учителя - только свои
            if (!currentUser.getRole().name().equals("ADMIN") && 
                !existingCourse.getTeacher().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            Course updatedCourse = courseService.update(id, course);
            return ResponseEntity.ok(updatedCourse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> activateCourse(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(courseService.activate(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> deactivateCourse(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(courseService.deactivate(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // НОВЫЙ ENDPOINT: Самостоятельная запись студента на курс
    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> enrollInCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            User student = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            courseService.enrollStudent(id, student);
            
            response.put("success", true);
            response.put("message", "Вы успешно записались на курс!");
            response.put("enrolled", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при записи на курс: " + e.getMessage());
            response.put("enrolled", false);
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    // НОВЫЙ ENDPOINT: Отписка студента от курса
    @DeleteMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> unenrollFromCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            User student = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            courseService.unenrollStudent(id, student);
            
            response.put("success", true);
            response.put("message", "Вы успешно отписались от курса!");
            response.put("enrolled", false);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при отписке от курса: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    // НОВЫЙ ENDPOINT: Проверка записи на курс
    @GetMapping("/{id}/enrollment-status")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getEnrollmentStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            User student = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            boolean isEnrolled = courseService.isStudentEnrolled(id, student.getUsername());
            
            response.put("enrolled", isEnrolled);
            response.put("courseId", id);
            response.put("userId", student.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("enrolled", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Административные методы для добавления/удаления студентов
    @PostMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> addStudent(
            @PathVariable Long id,
            @PathVariable Long studentId) {
        try {
            return ResponseEntity.ok(courseService.addStudent(id, studentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> removeStudent(
            @PathVariable Long id,
            @PathVariable Long studentId) {
        try {
            return ResponseEntity.ok(courseService.removeStudent(id, studentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 