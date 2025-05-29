package com.onlineSchool.controller;

import com.onlineSchool.model.User;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.service.UserService;
import com.onlineSchool.service.WebinarService;
import com.onlineSchool.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.context.SecurityContextHolder; // Для проверки удаления самого себя
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // Импорт для ResponseStatusException
import org.springframework.http.HttpStatus; // Импорт для HttpStatus

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Доступ только для администраторов
public class AdminController {

    private final UserService userService;
    private final WebinarService webinarService;
    private final CourseService courseService;

    @Autowired
    public AdminController(UserService userService, WebinarService webinarService, CourseService courseService) {
        this.userService = userService;
        this.webinarService = webinarService;
        this.courseService = courseService;
    }

    @GetMapping
    public String adminPage(Model model) {
        // Добавляем статистику для главной страницы админа
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalCourses", courseService.count());
        model.addAttribute("totalWebinars", webinarService.count());
        model.addAttribute("activeWebinars", webinarService.countByStatus("SCHEDULED"));
        
        return "admin"; // Возвращаем главную страницу админки
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String showCreateUserForm(Model model) {
        User user = new User();
        user.setActive(true); // По умолчанию новый пользователь активен
        model.addAttribute("user", user);
        return "admin/user-form";
    }

    @PostMapping("/users/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           @RequestParam(value = "rawPassword", required = false) String rawPassword,
                           BindingResult bindingResult, Model model) {

        boolean isNewUser = user.getId() == null;

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
            // Для существующего пользователя, проверяем уникальность только если значение изменилось
            User existingUserById = userService.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Попытка сохранить изменения для несуществующего пользователя с ID: " + user.getId()));

            if (!existingUserById.getUsername().equals(user.getUsername()) && userService.existsByUsername(user.getUsername())) {
                bindingResult.rejectValue("username", "error.user", "Пользователь с таким именем уже существует.");
            }
            if (!existingUserById.getEmail().equals(user.getEmail()) && userService.existsByEmail(user.getEmail())) {
                bindingResult.rejectValue("email", "error.user", "Пользователь с таким email уже существует.");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "admin/user-form";
        }

        // Устанавливаем пароль, только если он предоставлен.
        // Для новых пользователей это обязательно (проверено выше).
        // Для существующих, если не предоставлен, пароль остается старым (логика в userService.save должна это учитывать).
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(rawPassword); // userService.save() должен будет его закодировать
        } else if (isNewUser) {
            // Эта ветка не должна достигаться из-за валидации выше, но для безопасности:
             bindingResult.rejectValue("password", "error.user", "Пароль не может быть пустым для нового пользователя.");
             model.addAttribute("user", user);
             return "admin/user-form";
        }
        // Если это существующий пользователь и rawPassword пуст, userService.save() должен сохранить старый пароль.

        userService.save(user); // Метод save должен обработать как новых, так и существующих пользователей
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с ID:" + id + " не найден"));
        model.addAttribute("user", user);
        return "admin/user-form";
    }

    @PostMapping("/users/update")
    public String updateUser(@Valid @ModelAttribute("user") User userFromForm, // Переименовано для ясности
                             @RequestParam(value = "rawPassword", required = false) String rawPassword,
                             BindingResult bindingResult, Model model) {
        
        User existingUser = userService.findById(userFromForm.getId())
            .orElseThrow(() -> new IllegalArgumentException("Попытка обновить несуществующего пользователя с ID: " + userFromForm.getId()));

        // Проверка на уникальность username, если он был изменен
        if (!existingUser.getUsername().equals(userFromForm.getUsername()) && userService.existsByUsername(userFromForm.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Пользователь с таким именем уже существует.");
        }
        // Проверка на уникальность email, если он был изменен
        if (!existingUser.getEmail().equals(userFromForm.getEmail()) && userService.existsByEmail(userFromForm.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Пользователь с таким email уже существует.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userFromForm);
            return "admin/user-form";
        }

        // Обновляем поля существующего пользователя данными из формы
        existingUser.setUsername(userFromForm.getUsername());
        existingUser.setEmail(userFromForm.getEmail());
        existingUser.setFirstName(userFromForm.getFirstName());
        existingUser.setLastName(userFromForm.getLastName());
        existingUser.setRole(userFromForm.getRole());
        existingUser.setActive(userFromForm.isActive());

        if (rawPassword != null && !rawPassword.isBlank()) {
            existingUser.setPassword(rawPassword); // userService.save() должен будет его закодировать
        }
        // Если rawPassword пуст, пароль existingUser не меняется, userService.save() сохранит старый.

        userService.save(existingUser); 
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        // Опциональная проверка: не пытается ли админ удалить сам себя
        // UserDetails currentUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // if (currentUserDetails instanceof User) {
        //     User currentUser = (User) currentUserDetails;
        //     if (currentUser.getId().equals(id)) {
        //         // TODO: Добавить сообщение об ошибке (например, через RedirectAttributes)
        //         // и предотвратить удаление
        //         return "redirect:/admin/users?error=cannotDeleteSelf";
        //     }
        // }
        userService.deleteById(id);
        return "redirect:/admin/users";
    }

    // TODO: Добавить метод для /users/delete/{id}

    @GetMapping("/webinars")
    public String manageWebinars(Model model) {
        List<Webinar> webinars = webinarService.findAll();
        model.addAttribute("webinars", webinars);
        return "admin/webinars";
    }

    @GetMapping("/webinars/new")
    public String showCreateWebinarForm(Model model) {
        model.addAttribute("webinar", new Webinar());
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("teachers", userService.findByRole("TEACHER"));
        return "admin/webinar-form";
    }

    @PostMapping("/webinars/save")
    public String saveWebinar(@Valid @ModelAttribute("webinar") Webinar webinar,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("webinar", webinar);
            model.addAttribute("courses", courseService.findAll());
            model.addAttribute("teachers", userService.findByRole("TEACHER"));
            return "admin/webinar-form";
        }

        webinarService.save(webinar);
        return "redirect:/admin/webinars";
    }

    @GetMapping("/webinars/edit/{id}")
    public String showEditWebinarForm(@PathVariable("id") Long id, Model model) {
        Webinar webinar = webinarService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вебинар с ID:" + id + " не найден"));
        model.addAttribute("webinar", webinar);
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("teachers", userService.findByRole("TEACHER"));
        return "admin/webinar-form";
    }

    @PostMapping("/webinars/update")
    public String updateWebinar(@Valid @ModelAttribute("webinar") Webinar webinar,
                               BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("webinar", webinar);
            model.addAttribute("courses", courseService.findAll());
            model.addAttribute("teachers", userService.findByRole("TEACHER"));
            return "admin/webinar-form";
        }

        webinarService.update(webinar.getId(), webinar);
        return "redirect:/admin/webinars";
    }

    @PostMapping("/webinars/delete/{id}")
    public String deleteWebinar(@PathVariable("id") Long id) {
        webinarService.deleteById(id);
        return "redirect:/admin/webinars";
    }
} 