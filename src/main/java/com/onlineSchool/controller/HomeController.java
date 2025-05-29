package com.onlineSchool.controller;

import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CourseService courseService;
    private final UserService userService;
    private final WebinarService webinarService;

    @GetMapping("/")
    public String home(Model model) {
        try {
            // Статистика для главной страницы
            var allCourses = courseService.findAll();
            var allUsers = userService.findAll();
            var allWebinars = webinarService.findAll();
            
            model.addAttribute("courseCount", allCourses != null ? allCourses.size() : 0);
            model.addAttribute("studentCount", allUsers != null ? 
                allUsers.stream().mapToInt(user -> user.getRole() == Role.STUDENT ? 1 : 0).sum() : 0);
            model.addAttribute("teacherCount", allUsers != null ? 
                allUsers.stream().mapToInt(user -> user.getRole() == Role.TEACHER ? 1 : 0).sum() : 0);
            model.addAttribute("webinarCount", allWebinars != null ? allWebinars.size() : 0);
            
            // Популярные курсы (первые 6)
            if (allCourses != null && !allCourses.isEmpty()) {
                var popularCourses = allCourses.stream()
                        .filter(course -> course.isActive())
                        .limit(6)
                        .toList();
                model.addAttribute("popularCourses", popularCourses);
            } else {
                model.addAttribute("popularCourses", Collections.emptyList());
            }
            
            // Ближайшие вебинары (первые 4)
            try {
                var upcomingWebinars = webinarService.findUpcoming().stream()
                        .limit(4)
                        .toList();
                model.addAttribute("upcomingWebinars", upcomingWebinars);
            } catch (Exception e) {
                model.addAttribute("upcomingWebinars", Collections.emptyList());
            }
        } catch (Exception e) {
            // В случае ошибки устанавливаем значения по умолчанию
            model.addAttribute("courseCount", 0);
            model.addAttribute("studentCount", 0);
            model.addAttribute("teacherCount", 0);
            model.addAttribute("webinarCount", 0);
            model.addAttribute("popularCourses", Collections.emptyList());
            model.addAttribute("upcomingWebinars", Collections.emptyList());
        }
        
        return "index";
    }

    @GetMapping("/courses")
    public String courses(Model model) {
        try {
            // Получаем все курсы
            var allCourses = courseService.findAll();
            System.out.println("Found courses: " + (allCourses != null ? allCourses.size() : "null"));
            
            if (allCourses != null) {
                // Фильтруем только активные курсы
                var activeCourses = allCourses.stream()
                        .filter(course -> course != null && course.isActive())
                        .toList();
                System.out.println("Active courses: " + activeCourses.size());
                model.addAttribute("courses", activeCourses);
                model.addAttribute("totalCourses", activeCourses.size());
            } else {
                model.addAttribute("courses", Collections.emptyList());
                model.addAttribute("totalCourses", 0);
            }
        } catch (Exception e) {
            System.err.println("Error in courses handler: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("courses", Collections.emptyList());
            model.addAttribute("totalCourses", 0);
        }
        
        return "courses";
    }

    @GetMapping("/courses/{id}")
    public String courseDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var course = courseService.findByIdWithDetails(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            
            // Загружаем вебинары отдельно
            var webinars = webinarService.findByCourseId(id);
            
            // Проверяем, записан ли текущий пользователь на курс
            boolean isEnrolled = false;
            if (userDetails != null) {
                // Получаем пользователя по username
                var user = userService.findByUsername(userDetails.getUsername()).orElse(null);
                if (user != null && user.getRole() == Role.STUDENT) {
                    isEnrolled = courseService.isStudentEnrolled(id, user.getUsername());
                }
            }
            
            model.addAttribute("course", course);
            model.addAttribute("webinars", webinars != null ? webinars : Collections.emptyList());
            model.addAttribute("isEnrolled", isEnrolled);
            
            return "course-details";
        } catch (Exception e) {
            return "redirect:/courses";
        }
    }

    @GetMapping("/webinars")
    public String webinars(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Получаем все предстоящие вебинары
            var upcomingWebinars = webinarService.findUpcoming();
            System.out.println("Found upcoming webinars: " + (upcomingWebinars != null ? upcomingWebinars.size() : "null"));
            
            // Фильтруем только активные вебинары
            if (upcomingWebinars != null) {
                var activeWebinars = upcomingWebinars.stream()
                        .filter(webinar -> webinar != null && webinar.isActive())
                        .toList();
                System.out.println("Active webinars: " + activeWebinars.size());
                model.addAttribute("webinars", activeWebinars);
                model.addAttribute("totalWebinars", activeWebinars.size());
                
                // Добавляем информацию об участии текущего пользователя для каждого вебинара
                if (userDetails != null) {
                    Map<Long, Boolean> participationMap = new HashMap<>();
                    for (var webinar : activeWebinars) {
                        boolean isParticipant = webinarService.isUserParticipant(webinar.getId(), userDetails.getUsername());
                        participationMap.put(webinar.getId(), isParticipant);
                        // Принудительная загрузка для избежания lazy loading проблем
                        webinar.getParticipants().size();
                    }
                    model.addAttribute("participationMap", participationMap);
                } else {
                    model.addAttribute("participationMap", new HashMap<Long, Boolean>());
                }
            } else {
                model.addAttribute("webinars", Collections.emptyList());
                model.addAttribute("totalWebinars", 0);
                model.addAttribute("participationMap", new HashMap<Long, Boolean>());
            }
        } catch (Exception e) {
            System.err.println("Error in webinars handler: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("webinars", Collections.emptyList());
            model.addAttribute("totalWebinars", 0);
            model.addAttribute("participationMap", new HashMap<Long, Boolean>());
        }
        
        return "webinars";
    }

    @GetMapping("/webinars/{id}")
    public String webinarDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var webinar = webinarService.findByIdWithDetails(id)
                    .orElseThrow(() -> new RuntimeException("Webinar not found"));
            
            // Проверяем, участвует ли текущий пользователь в вебинаре
            boolean isParticipant = false;
            boolean hasLiked = false;
            if (userDetails != null) {
                try {
                    isParticipant = webinarService.isUserParticipant(id, userDetails.getUsername());
                } catch (Exception e) {
                    System.err.println("Error checking user participation: " + e.getMessage());
                    isParticipant = false;
                }
                
                // Проверяем, поставил ли пользователь лайк
                try {
                    var user = userService.findByUsername(userDetails.getUsername()).orElse(null);
                    if (user != null && webinar.getLikes() != null) {
                        hasLiked = webinar.getLikes().stream()
                                .anyMatch(like -> like.getUser() != null && 
                                        like.getUser().getId().equals(user.getId()));
                    }
                } catch (Exception e) {
                    System.err.println("Error checking user likes: " + e.getMessage());
                    hasLiked = false;
                }
            }
            
            model.addAttribute("webinar", webinar);
            model.addAttribute("isParticipant", isParticipant);
            model.addAttribute("hasLiked", hasLiked);
            
            return "webinar-details";
        } catch (Exception e) {
            System.err.println("Error in webinarDetails: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/webinars";
        }
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal User currentUser, 
                               @ModelAttribute User user, 
                               Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            // Обновляем только разрешенные поля
            currentUser.setFirstName(user.getFirstName());
            currentUser.setLastName(user.getLastName());
            currentUser.setEmail(user.getEmail());
            
            userService.save(currentUser);
            model.addAttribute("success", "Профиль успешно обновлен");
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при обновлении профиля");
        }
        
        model.addAttribute("user", currentUser);
        return "profile";
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