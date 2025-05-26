package com.onlineSchool.controller;

import com.onlineSchool.model.User;
import com.onlineSchool.model.Role;
import com.onlineSchool.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String getRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               @RequestParam(required = false) String firstName,
                               @RequestParam(required = false) String lastName,
                               @RequestParam(defaultValue = "STUDENT") String role,
                               Model model) {
        
        // Проверка, что пароли совпадают
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            return "register";
        }
        
        // Проверка длины пароля
        if (password.length() < 6) {
            model.addAttribute("error", "Пароль должен содержать минимум 6 символов");
            return "register";
        }
        
        // Проверка, что пользователь с таким email не существует
        if (userService.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            return "register";
        }
        
        // Проверка, что пользователь с таким username не существует
        if (userService.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Пользователь с таким именем уже существует");
            return "register";
        }
        
        // Создание нового пользователя
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName != null && !firstName.trim().isEmpty() ? firstName.trim() : null);
        user.setLastName(lastName != null && !lastName.trim().isEmpty() ? lastName.trim() : null);
        
        // Установка роли
        try {
            user.setRole(Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            user.setRole(Role.STUDENT); // По умолчанию студент
        }
        
        user.setActive(true);
        
        userService.save(user);
        
        // Перенаправление на страницу входа
        return "redirect:/login?registered";
    }
} 