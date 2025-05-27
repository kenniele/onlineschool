package com.onlineSchool.controller;

import com.onlineSchool.model.Course;
import com.onlineSchool.model.User;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final CourseService courseService;
    private final WebinarService webinarService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        List<Course> enrolledCourses = courseService.findByStudent(student);
        List<Webinar> upcomingWebinars = webinarService.findUpcoming().stream()
                .filter(webinar -> webinar.getParticipants().contains(student) || 
                        enrolledCourses.contains(webinar.getCourse()))
                .limit(5)
                .toList();
        
        model.addAttribute("student", student);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("upcomingWebinars", upcomingWebinars);
        model.addAttribute("coursesCount", enrolledCourses.size());
        model.addAttribute("webinarsCount", upcomingWebinars.size());
        
        return "student/dashboard";
    }

    @GetMapping("/courses")
    public String myCourses(Model model, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        List<Course> enrolledCourses = courseService.findByStudent(student);
        model.addAttribute("courses", enrolledCourses);
        model.addAttribute("student", student);
        
        return "student/courses";
    }

    @PostMapping("/courses/{id}/enroll")
    public String enrollInCourse(@PathVariable Long id, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        courseService.enrollStudent(id, student);
        
        return "redirect:/courses/" + id;
    }

    @PostMapping("/api/courses/{id}/enroll")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enrollInCourseAjax(@PathVariable Long id, Authentication authentication) {
        try {
            User student = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            courseService.enrollStudent(id, student);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Вы успешно записались на курс!");
            response.put("enrolled", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка при записи на курс: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/courses/{id}/unenroll")
    public String unenrollFromCourse(@PathVariable Long id, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        courseService.unenrollStudent(id, student);
        
        return "redirect:/student/courses";
    }

    @GetMapping("/webinars")
    public String myWebinars(Model model, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        List<Webinar> participatingWebinars = webinarService.findByParticipant(student);
        List<Webinar> availableWebinars = webinarService.findUpcoming().stream()
                .filter(webinar -> !webinar.getParticipants().contains(student))
                .limit(10)
                .toList();
        
        model.addAttribute("participatingWebinars", participatingWebinars);
        model.addAttribute("availableWebinars", availableWebinars);
        model.addAttribute("student", student);
        
        return "student/webinars";
    }

    @PostMapping("/webinars/{id}/join")
    public String joinWebinar(@PathVariable Long id, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        webinarService.addParticipant(id, student);
        
        return "redirect:/webinars/" + id;
    }

    @PostMapping("/webinars/{id}/leave")
    public String leaveWebinar(@PathVariable Long id, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        webinarService.removeParticipant(id, student);
        
        return "redirect:/student/webinars";
    }

    @GetMapping("/progress")
    public String progress(Model model, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        List<Course> enrolledCourses = courseService.findByStudent(student);
        List<Webinar> attendedWebinars = webinarService.findByParticipant(student);
        
        // Подсчет статистики
        int totalCourses = enrolledCourses.size();
        int completedCourses = (int) enrolledCourses.stream()
                .filter(course -> course.getEndDate().isBefore(java.time.LocalDateTime.now()))
                .count();
        int totalWebinars = attendedWebinars.size();
        
        model.addAttribute("student", student);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("attendedWebinars", attendedWebinars);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("completedCourses", completedCourses);
        model.addAttribute("totalWebinars", totalWebinars);
        model.addAttribute("progressPercentage", totalCourses > 0 ? (completedCourses * 100 / totalCourses) : 0);
        
        return "student/progress";
    }
} 