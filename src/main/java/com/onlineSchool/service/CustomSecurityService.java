package com.onlineSchool.service;

import com.onlineSchool.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service("customSecurityService")
public class CustomSecurityService {

    public boolean isStudentHimself(Long studentId, UserDetails principal) {
        if (principal == null) {
            return false;
        }
        if (principal instanceof User) {
            User currentUser = (User) principal;
            return currentUser.getId().equals(studentId);
        }
        return false;
    }
    
    // Можно добавить другие кастомные проверки безопасности здесь
} 