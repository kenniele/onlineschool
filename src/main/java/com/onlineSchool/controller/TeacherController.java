package com.onlineSchool.controller;

import com.onlineSchool.model.Course;
import com.onlineSchool.model.User;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    private final CourseService courseService;
    private final WebinarService webinarService;
    private final UserService userService;

    @GetMapping("/courses")
    public String myCourses(Model model, Authentication authentication) {
        User teacher = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        List<Course> teacherCourses = courseService.findByTeacher(teacher);
        
        // Вычисляем общее количество студентов
        int totalStudents = teacherCourses.stream()
                .mapToInt(course -> course.getStudents().size())
                .sum();
        
        model.addAttribute("courses", teacherCourses);
        model.addAttribute("teacher", teacher);
        model.addAttribute("totalStudents", totalStudents);
        
        return "teacher/courses";
    }

    @GetMapping("/courses/new")
    public String newCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "teacher/course-form";
    }

    @PostMapping("/courses")
    public String createCourse(@ModelAttribute Course course, Authentication authentication) {
        User teacher = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        course.setTeacher(teacher);
        courseService.save(course);
        
        return "redirect:/teacher/courses";
    }

    @GetMapping("/courses/{id}/edit")
    public String editCourseForm(@PathVariable Long id, Model model, Authentication authentication) {
        Course course = courseService.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Проверяем, что курс принадлежит текущему учителю
        User teacher = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        model.addAttribute("course", course);
        return "teacher/course-form";
    }

    @PostMapping("/courses/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course course, Authentication authentication) {
        User teacher = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        course.setId(id);
        course.setTeacher(teacher);
        courseService.update(id, course);
        
        return "redirect:/teacher/courses";
    }

    @GetMapping("/webinars")
    public String myWebinars(Model model, Authentication authentication) {
        User teacher = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        List<Webinar> teacherWebinars = webinarService.findByTeacher(teacher);
        
        // Вычисляем статистику
        long upcomingCount = teacherWebinars.stream()
                .filter(w -> w.getStartTime().isAfter(java.time.LocalDateTime.now()))
                .count();
        
        int totalParticipants = teacherWebinars.stream()
                .mapToInt(w -> w.getParticipants().size())
                .sum();
        
        model.addAttribute("webinars", teacherWebinars);
        model.addAttribute("teacher", teacher);
        model.addAttribute("upcomingCount", upcomingCount);
        model.addAttribute("totalParticipants", totalParticipants);
        
        return "teacher/webinars";
    }

    @GetMapping("/webinars/new")
    public String newWebinarForm(Model model, Authentication authentication) {
        User teacher = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        List<Course> teacherCourses = courseService.findByTeacher(teacher);
        
        model.addAttribute("webinar", new Webinar());
        model.addAttribute("courses", teacherCourses);
        
        return "teacher/webinar-form";
    }

    @PostMapping("/webinars")
    public String createWebinar(@ModelAttribute Webinar webinar, Authentication authentication) {
        User teacher = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        webinar.setTeacher(teacher);
        webinarService.save(webinar);
        
        return "redirect:/teacher/webinars";
    }

    @GetMapping("/webinars/{id}/edit")
    public String editWebinarForm(@PathVariable Long id, Model model, Authentication authentication) {
        Webinar webinar = webinarService.findById(id)
                .orElseThrow(() -> new RuntimeException("Webinar not found"));
        
        // Проверяем, что вебинар принадлежит текущему учителю
        User teacher = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        if (!webinar.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        List<Course> teacherCourses = courseService.findByTeacher(teacher);
        
        model.addAttribute("webinar", webinar);
        model.addAttribute("courses", teacherCourses);
        
        return "teacher/webinar-form";
    }

    @PostMapping("/webinars/{id}")
    public String updateWebinar(@PathVariable Long id, @ModelAttribute Webinar webinar, Authentication authentication) {
        User teacher = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        webinar.setId(id);
        webinar.setTeacher(teacher);
        webinarService.update(id, webinar);
        
        return "redirect:/teacher/webinars";
    }

    @PostMapping("/webinars/{id}/cancel")
    public String cancelWebinar(@PathVariable Long id, Authentication authentication) {
        Webinar webinar = webinarService.findById(id)
                .orElseThrow(() -> new RuntimeException("Webinar not found"));
        
        // Проверяем, что вебинар принадлежит текущему учителю
        User teacher = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        if (!webinar.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        // Отменяем вебинар
        webinarService.cancel(id);
        
        return "redirect:/teacher/webinars";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User teacher = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        List<Course> teacherCourses = courseService.findByTeacher(teacher);
        List<Webinar> teacherWebinars = webinarService.findByTeacher(teacher);
        List<Webinar> upcomingWebinars = webinarService.findUpcomingByTeacher(teacher);
        
        // Статистика
        int totalStudents = teacherCourses.stream()
                .mapToInt(course -> course.getStudents().size())
                .sum();
        
        model.addAttribute("teacher", teacher);
        model.addAttribute("coursesCount", teacherCourses.size());
        model.addAttribute("webinarsCount", teacherWebinars.size());
        model.addAttribute("studentsCount", totalStudents);
        model.addAttribute("upcomingWebinars", upcomingWebinars);
        model.addAttribute("recentCourses", teacherCourses.stream().limit(3).toList());
        
        return "teacher/dashboard";
    }
} 