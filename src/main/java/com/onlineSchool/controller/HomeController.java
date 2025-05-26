package com.onlineSchool.controller;

import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CourseService courseService;
    private final UserService userService;
    private final WebinarService webinarService;

    @GetMapping("/")
    public String home(Model model) {
        // Статистика для главной страницы
        model.addAttribute("courseCount", courseService.findAll().size());
        model.addAttribute("studentCount", userService.findAll().stream()
                .mapToInt(user -> user.getRole() == Role.STUDENT ? 1 : 0).sum());
        model.addAttribute("teacherCount", userService.findAll().stream()
                .mapToInt(user -> user.getRole() == Role.TEACHER ? 1 : 0).sum());
        model.addAttribute("webinarCount", webinarService.findAll().size());
        
        // Популярные курсы (первые 6)
        var allCourses = courseService.findAll();
        var popularCourses = allCourses.stream()
                .filter(course -> course.isActive())
                .limit(6)
                .toList();
        model.addAttribute("popularCourses", popularCourses);
        
        // Ближайшие вебинары (первые 4)
        var upcomingWebinars = webinarService.findUpcoming().stream()
                .limit(4)
                .toList();
        model.addAttribute("upcomingWebinars", upcomingWebinars);
        
        return "index";
    }

    @GetMapping("/courses")
    public String courses(Model model) {
        // Получаем все активные курсы
        var activeCourses = courseService.getAllActiveCourses();
        model.addAttribute("courses", activeCourses);
        
        // Статистика
        model.addAttribute("totalCourses", activeCourses.size());
        
        return "courses";
    }

    @GetMapping("/courses/{id}")
    public String courseDetails(@PathVariable Long id, Model model) {
        var course = courseService.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        model.addAttribute("course", course);
        model.addAttribute("webinars", course.getWebinars());
        
        return "course-details";
    }

    @GetMapping("/webinars")
    public String webinars(Model model) {
        // Получаем все предстоящие вебинары
        var upcomingWebinars = webinarService.findUpcoming();
        model.addAttribute("webinars", upcomingWebinars);
        
        // Статистика
        model.addAttribute("totalWebinars", upcomingWebinars.size());
        
        return "webinars";
    }

    @GetMapping("/webinars/{id}")
    public String webinarDetails(@PathVariable Long id, Model model) {
        var webinar = webinarService.findById(id)
                .orElseThrow(() -> new RuntimeException("Webinar not found"));
        
        model.addAttribute("webinar", webinar);
        
        return "webinar-details";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/login";
        }
        
        // Перенаправляем пользователей на соответствующие дашборды в зависимости от роли
        switch (user.getRole()) {
            case ADMIN:
                return "redirect:/admin";
            case TEACHER:
                return "redirect:/teacher/dashboard";
            case STUDENT:
                return "redirect:/student/dashboard";
            default:
                return "redirect:/";
        }
    }
} 