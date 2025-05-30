package com.onlineSchool.controller;

import com.onlineSchool.model.Course;
import com.onlineSchool.model.User;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;
import com.onlineSchool.service.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private WebinarService webinarService;

    @Autowired
    private ProgressService progressService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        log.info("=== DASHBOARD PAGE DEBUG ===");
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        log.info("Student found: " + student.getUsername() + ", Role: " + student.getRole());
        
        // Получаем курсы студента
        List<Course> enrolledCourses = courseService.findByStudent(student);
        List<Webinar> upcomingWebinars = webinarService.findUpcoming();
        List<Webinar> attendedWebinars = webinarService.findByParticipant(student);

        log.info("Enrolled courses count: " + (enrolledCourses != null ? enrolledCourses.size() : "null"));
        log.info("Upcoming webinars count: " + (upcomingWebinars != null ? upcomingWebinars.size() : "null"));
        log.info("Attended webinars count: " + (attendedWebinars != null ? attendedWebinars.size() : "null"));

        // Получаем статистику прогресса
        Map<String, Object> progressStats = progressService.getProgressStatistics(student);
        log.info("Progress stats: " + progressStats);
        
        // Получаем прогресс по курсам
        Map<Course, Integer> courseProgressMap = progressService.getCourseProgressMap(student);
        log.info("Course progress map size: " + (courseProgressMap != null ? courseProgressMap.size() : "null"));
        
        model.addAttribute("student", student);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("upcomingWebinars", upcomingWebinars);
        model.addAttribute("attendedWebinars", attendedWebinars);
        model.addAttribute("courseProgressMap", courseProgressMap);
        model.addAttribute("coursesCount", enrolledCourses != null ? enrolledCourses.size() : 0);
        model.addAttribute("webinarsCount", attendedWebinars != null ? attendedWebinars.size() : 0);
        model.addAttribute("completedWebinars", progressStats.get("completedWebinars"));
        model.addAttribute("overallProgress", progressStats.get("overallProgress"));
        
        return "student/dashboard";
    }

    @GetMapping("/courses")
    public String courses(Model model, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        List<Course> enrolledCourses = courseService.findByStudent(student);
        Map<Course, Integer> courseProgressMap = progressService.getCourseProgressMap(student);

        model.addAttribute("student", student);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("courseProgressMap", courseProgressMap);
        
        return "student/courses";
    }

    @GetMapping("/webinars")
    public String webinars(Model model, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        List<Webinar> upcomingWebinars = webinarService.findUpcoming();
        List<Webinar> attendedWebinars = webinarService.findByParticipant(student);
        
        // Разделяем вебинары на те, в которых студент участвует, и доступные для записи
        List<Webinar> participatingWebinars = upcomingWebinars.stream()
                .filter(webinar -> webinar.getParticipants() != null && webinar.getParticipants().contains(student))
                .toList();
                
        List<Webinar> availableWebinars = upcomingWebinars.stream()
                .filter(webinar -> webinar.getParticipants() == null || !webinar.getParticipants().contains(student))
                .toList();

        model.addAttribute("student", student);
        model.addAttribute("upcomingWebinars", upcomingWebinars);
        model.addAttribute("attendedWebinars", attendedWebinars);
        model.addAttribute("participatingWebinars", participatingWebinars);
        model.addAttribute("availableWebinars", availableWebinars);
            
        return "student/webinars";
    }

    @PostMapping("/courses/{id}/enroll")
    public String enrollInCourse(@PathVariable Long id, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Course course = courseService.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        courseService.enrollStudent(course.getId(), student);
        
        return "redirect:/student/courses";
    }

    @PostMapping("/courses/{id}/leave")
    public String leaveCourse(@PathVariable Long id, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Course course = courseService.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        courseService.unenrollStudent(course.getId(), student);
        
        return "redirect:/student/courses";
    }

    @PostMapping("/webinars/{id}/join")
    public String joinWebinar(@PathVariable Long id, Authentication authentication) {
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        webinarService.addParticipant(id, student);
        
        return "redirect:/student/webinars";
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
        log.info("=== PROGRESS PAGE DEBUG ===");
        User student = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        log.info("Student found: " + student.getUsername() + ", Role: " + student.getRole());
        
        List<Course> enrolledCourses = courseService.findByStudent(student);
        log.info("Enrolled courses count: " + (enrolledCourses != null ? enrolledCourses.size() : "null"));
        
        List<Webinar> attendedWebinars = webinarService.findByParticipant(student);
        log.info("Attended webinars count: " + (attendedWebinars != null ? attendedWebinars.size() : "null"));
        
        // Получаем детальную статистику прогресса
        Map<String, Object> progressStats = progressService.getProgressStatistics(student);
        log.info("Progress stats: " + progressStats);
        
        // Получаем прогресс по курсам
        Map<Course, Integer> courseProgressMap = progressService.getCourseProgressMap(student);
        log.info("Course progress map size: " + (courseProgressMap != null ? courseProgressMap.size() : "null"));
        
        model.addAttribute("student", student);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("attendedWebinars", attendedWebinars);
        model.addAttribute("courseProgressMap", courseProgressMap);
        
        // Добавляем статистику
        model.addAttribute("totalCourses", progressStats.get("totalCourses"));
        model.addAttribute("totalWebinars", progressStats.get("totalAttendedWebinars"));
        model.addAttribute("completedWebinars", progressStats.get("completedWebinars"));
        model.addAttribute("overallProgress", progressStats.get("overallProgress"));
        model.addAttribute("totalWebinarsInCourses", progressStats.get("totalWebinarsInCourses"));
        model.addAttribute("completedCourses", progressStats.get("completedCourses"));
        
        log.info("Model attributes added successfully");
        log.info("=== END PROGRESS PAGE DEBUG ===");
        
        return "student/progress";
    }
} 