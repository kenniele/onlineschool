package com.onlineSchool.controller;

import com.onlineSchool.model.User;
import com.onlineSchool.model.Course;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.CourseService;
import com.onlineSchool.service.WebinarService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;
    private final WebinarService webinarService;

    @Autowired
    public AdminController(UserService userService, CourseService courseService, WebinarService webinarService) {
        this.userService = userService;
        this.courseService = courseService;
        this.webinarService = webinarService;
    }

    // Главная страница админки
    @GetMapping
    public String adminHomePage(Model model) {
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalCourses", courseService.count());
        model.addAttribute("totalWebinars", webinarService.count());
        return "admin";
    }

    // === УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ===
    
    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String showCreateUserForm(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "admin/user-form";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        model.addAttribute("user", user);
        return "admin/user-form";
    }

    @PostMapping("/users/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                          @RequestParam(value = "rawPassword", required = false) String rawPassword,
                          BindingResult bindingResult, Model model) {

        boolean isNewUser = user.getId() == null;

        // Валидация
        if (isNewUser) {
            if (userService.existsByUsername(user.getUsername())) {
                bindingResult.rejectValue("username", "error.user", "Пользователь с таким именем уже существует.");
            }
            if (userService.existsByEmail(user.getEmail())) {
                bindingResult.rejectValue("email", "error.user", "Пользователь с таким email уже существует.");
            }
            if (rawPassword == null || rawPassword.isBlank()) {
                bindingResult.rejectValue("password", "error.user", "Пароль не может быть пустым для нового пользователя.");
            }
        } else {
            User existingUser = userService.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Попытка сохранить изменения для несуществующего пользователя"));

            if (!existingUser.getUsername().equals(user.getUsername()) && userService.existsByUsername(user.getUsername())) {
                bindingResult.rejectValue("username", "error.user", "Пользователь с таким именем уже существует.");
            }
            if (!existingUser.getEmail().equals(user.getEmail()) && userService.existsByEmail(user.getEmail())) {
                bindingResult.rejectValue("email", "error.user", "Пользователь с таким email уже существует.");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "admin/user-form";
        }

        // Установка пароля для новых пользователей
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(rawPassword);
        } else if (isNewUser) {
            bindingResult.rejectValue("password", "error.user", "Пароль не может быть пустым для нового пользователя.");
            model.addAttribute("user", user);
            return "admin/user-form";
        }

        userService.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteById(id);
        return "redirect:/admin/users";
    }

    // === УПРАВЛЕНИЕ КУРСАМИ ===
    
    @GetMapping("/courses")
    public String manageCourses(Model model) {
        List<Course> courses = courseService.findAll();
        model.addAttribute("courses", courses);
        return "admin/courses";
    }

    @GetMapping("/courses/new")
    public String showCreateCourseForm(Model model) {
        Course course = new Course();
        model.addAttribute("course", course);
        return "admin/course-form";
    }

    @GetMapping("/courses/edit/{id}")
    public String showEditCourseForm(@PathVariable("id") Long id, Model model) {
        Course course = courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));
        model.addAttribute("course", course);
        return "admin/course-form";
    }

    @PostMapping("/courses/save")
    public String saveCourse(@Valid @ModelAttribute("course") Course course,
                           BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("course", course);
            return "admin/course-form";
        }
        courseService.save(course);
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id) {
        courseService.deleteById(id);
        return "redirect:/admin/courses";
    }

    // === УПРАВЛЕНИЕ ВЕБИНАРАМИ ===
    
    @GetMapping("/webinars")
    public String manageWebinars(Model model) {
        List<Webinar> webinars = webinarService.findAll();
        model.addAttribute("webinars", webinars);
        return "admin/webinars";
    }

    @GetMapping("/webinars/new")
    public String showCreateWebinarForm(Model model) {
        Webinar webinar = new Webinar();
        model.addAttribute("webinar", webinar);
        List<User> teachers = userService.findAllTeachers();
        model.addAttribute("teachers", teachers);
        return "admin/webinar-form";
    }

    @GetMapping("/webinars/edit/{id}")
    public String showEditWebinarForm(@PathVariable("id") Long id, Model model) {
        Webinar webinar = webinarService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вебинар не найден"));
        model.addAttribute("webinar", webinar);
        List<User> teachers = userService.findAllTeachers();
        model.addAttribute("teachers", teachers);
        return "admin/webinar-form";
    }

    @PostMapping("/webinars/save")
    public String saveWebinar(@Valid @ModelAttribute("webinar") Webinar webinar,
                            @RequestParam(value = "teacherId", required = false) Long teacherId,
                            BindingResult bindingResult, Model model) {
        
        // Устанавливаем teacher по teacherId
        if (teacherId != null) {
            User teacher = userService.findById(teacherId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Преподаватель не найден"));
            webinar.setTeacher(teacher);
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("webinar", webinar);
            List<User> teachers = userService.findAllTeachers();
            model.addAttribute("teachers", teachers);
            return "admin/webinar-form";
        }
        webinarService.save(webinar);
        return "redirect:/admin/webinars";
    }

    @PostMapping("/webinars/delete/{id}")
    public String deleteWebinar(@PathVariable("id") Long id) {
        webinarService.deleteById(id);
        return "redirect:/admin/webinars";
    }
} 