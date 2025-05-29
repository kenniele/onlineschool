package com.onlineSchool.controller;

import com.onlineSchool.model.Role;
import com.onlineSchool.model.User;
import com.onlineSchool.model.Course;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.model.EntityType;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;
import com.onlineSchool.service.ProgressService;
import com.onlineSchool.service.LikeService;
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
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CourseService courseService;
    private final UserService userService;
    private final WebinarService webinarService;
    private final ProgressService progressService;
    private final LikeService likeService;

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
    public String courses(Model model, @AuthenticationPrincipal UserDetails userDetails) {
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
                
                // Если пользователь авторизован и является студентом, добавляем данные о прогрессе
                if (userDetails != null) {
                    try {
                        var user = userService.findByUsername(userDetails.getUsername()).orElse(null);
                        if (user != null && user.getRole() == Role.STUDENT) {
                            Map<Course, Integer> courseProgressMap = progressService.getCourseProgressMap(user);
                            model.addAttribute("courseProgressMap", courseProgressMap);
                            model.addAttribute("student", user);
                        }
                    } catch (Exception e) {
                        System.err.println("Error getting student progress: " + e.getMessage());
                    }
                }
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
        System.out.println("=== WEBINARS PAGE DEBUG ===");
        
        // Получаем все вебинары
        var allWebinars = webinarService.findAll();
        System.out.println("All webinars count: " + (allWebinars != null ? allWebinars.size() : "null"));
            
            // Фильтруем только активные вебинары
        var upcomingWebinars = allWebinars.stream()
                .filter(webinar -> webinar.getStartTime().isAfter(LocalDateTime.now()))
                        .toList();
        System.out.println("Upcoming webinars count: " + upcomingWebinars.size());
        
        model.addAttribute("webinars", allWebinars);
        model.addAttribute("upcomingWebinars", upcomingWebinars);
        model.addAttribute("totalWebinars", upcomingWebinars.size());
                
        // Если пользователь авторизован как студент, добавляем информацию об участии
        if (userDetails != null) {
            try {
                User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
                System.out.println("Current user: " + (currentUser != null ? currentUser.getUsername() : "null"));
                
                if (currentUser != null && currentUser.getRole() == Role.STUDENT) {
                    System.out.println("User is student, adding participation info");
                    // Создаем карту участия в вебинарах
                    Map<Long, Boolean> participationMap = new HashMap<>();
                    for (var webinar : allWebinars) {
                        boolean isParticipant = webinar.getParticipants() != null && 
                                webinar.getParticipants().contains(currentUser);
                        participationMap.put(webinar.getId(), isParticipant);
                    }
                    System.out.println("Participation map size: " + participationMap.size());
                    model.addAttribute("participationMap", participationMap);
                }
            } catch (Exception e) {
                System.err.println("Error processing user participation: " + e.getMessage());
            e.printStackTrace();
            }
        } else {
            System.out.println("User not authenticated");
        }
        
        System.out.println("=== END WEBINARS DEBUG ===");
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
            User currentUser = null;
            
            if (userDetails != null) {
                try {
                    currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
                    if (currentUser != null) {
                    isParticipant = webinarService.isUserParticipant(id, userDetails.getUsername());
                        
                        // Если пользователь - студент, добавляем данные о прогрессе
                        if (currentUser.getRole() == Role.STUDENT) {
                            Map<String, Object> progressStats = progressService.getProgressStatistics(currentUser);
                            Map<Course, Integer> courseProgressMap = progressService.getCourseProgressMap(currentUser);
                            
                            model.addAttribute("student", currentUser);
                            model.addAttribute("progressStats", progressStats);
                            model.addAttribute("courseProgressMap", courseProgressMap);
                            model.addAttribute("overallProgress", progressStats.get("overallProgress"));
                            model.addAttribute("completedWebinars", progressStats.get("completedWebinars"));
                            model.addAttribute("totalCourses", progressStats.get("totalCourses"));
                            model.addAttribute("completedCourses", progressStats.get("completedCourses"));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error checking user participation: " + e.getMessage());
                    isParticipant = false;
                }
                
                // Проверяем, поставил ли пользователь лайк
                try {
                    if (currentUser != null) {
                        hasLiked = likeService.hasLiked(currentUser.getId(), webinar.getId(), EntityType.WEBINAR);
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
        
        // Добавляем статистику в зависимости от роли пользователя
        if (user.getRole() == Role.STUDENT) {
            try {
                Map<String, Object> progressStats = progressService.getProgressStatistics(user);
                model.addAttribute("progressStats", progressStats);
                model.addAttribute("totalCourses", progressStats.get("totalCourses"));
                model.addAttribute("completedCourses", progressStats.get("completedCourses"));
                model.addAttribute("totalWebinars", progressStats.get("totalAttendedWebinars"));
                model.addAttribute("overallProgress", progressStats.get("overallProgress"));
            } catch (Exception e) {
                System.err.println("Error getting student progress: " + e.getMessage());
                // Устанавливаем значения по умолчанию при ошибке
                model.addAttribute("totalCourses", 0);
                model.addAttribute("completedCourses", 0);
                model.addAttribute("totalWebinars", 0);
                model.addAttribute("overallProgress", 0);
            }
        } else if (user.getRole() == Role.TEACHER) {
            try {
                List<Course> teacherCourses = courseService.findByTeacher(user);
                List<Webinar> teacherWebinars = webinarService.findByTeacher(user);
                
                // Подсчитываем количество студентов без lambda
                int totalStudents = 0;
                for (Course course : teacherCourses) {
                    if (course.getStudents() != null) {
                        totalStudents += course.getStudents().size();
                    }
                }
                
                model.addAttribute("teacherCoursesCount", teacherCourses.size());
                model.addAttribute("teacherWebinarsCount", teacherWebinars.size());
                model.addAttribute("teacherStudentsCount", totalStudents);
                model.addAttribute("teacherRating", 4.8); // Пока статичное значение, можно добавить систему рейтингов
            } catch (Exception e) {
                System.err.println("Error getting teacher stats: " + e.getMessage());
                model.addAttribute("teacherCoursesCount", 0);
                model.addAttribute("teacherWebinarsCount", 0);
                model.addAttribute("teacherStudentsCount", 0);
                model.addAttribute("teacherRating", 0);
            }
        } else if (user.getRole() == Role.ADMIN) {
            try {
                List<User> allUsers = userService.findAll();
                List<Course> allCourses = courseService.findAll();
                List<Webinar> upcomingWebinars = webinarService.findUpcoming();
                
                // Подсчитываем активные курсы без lambda
                int activeCourseCount = 0;
                for (Course course : allCourses) {
                    if (course.isActive()) {
                        activeCourseCount++;
                    }
                }
                
                model.addAttribute("totalUsers", allUsers.size());
                model.addAttribute("activeCourses", activeCourseCount);
                model.addAttribute("upcomingWebinarsCount", upcomingWebinars.size());
                model.addAttribute("systemUptime", 99.9); // Статичное значение для демонстрации
            } catch (Exception e) {
                System.err.println("Error getting admin stats: " + e.getMessage());
                model.addAttribute("totalUsers", 0);
                model.addAttribute("activeCourses", 0);
                model.addAttribute("upcomingWebinarsCount", 0);
                model.addAttribute("systemUptime", 100);
            }
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