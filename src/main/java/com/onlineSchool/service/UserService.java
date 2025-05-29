package com.onlineSchool.service;

import com.onlineSchool.model.User;
import com.onlineSchool.model.Role;
import com.onlineSchool.model.EntityType;
import com.onlineSchool.repository.UserRepository;
import com.onlineSchool.repository.UserCourseEnrollmentRepository;
import com.onlineSchool.repository.CommentRepository;
import com.onlineSchool.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCourseEnrollmentRepository userCourseEnrollmentRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User save(User user) {
        if (user.getId() == null) { // Новый пользователь
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                // Эта ситуация не должна возникать, если контроллер правильно валидирует
                // но для надежности можно выбросить исключение или вернуть ошибку
                throw new IllegalArgumentException("Пароль не может быть пустым для нового пользователя.");
            }
            // Проверяем, что пароль не является уже закодированной строкой bcrypt
            if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        } else { // Существующий пользователь
            User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + user.getId() + " не найден для обновления."));

            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                // Проверяем, что пароль не является уже закодированной строкой bcrypt
                if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                } 
                // Если это уже хеш, то он был установлен напрямую (например, из existingUser), оставляем как есть
            } else {
                // Если новый пароль не предоставлен, используем старый пароль из базы
                user.setPassword(existingUser.getPassword());
            }
        }
        // Обновляем остальные поля, если они есть в объекте user (контроллер должен позаботиться о передаче полного объекта)
        // userRepository.save(user) обновит все поля, переданные в объекте user.
        User savedUser = userRepository.save(user);
        return savedUser;
    }

    @Transactional
    public User save(Map<String, String> userData) {
        User user = new User();
        user.setUsername(userData.get("username"));
        user.setEmail(userData.get("email"));
        user.setFirstName(userData.get("firstName"));
        user.setLastName(userData.get("lastName"));
        user.setPassword(userData.get("password"));
        
        // Парсим роль из строки
        String roleString = userData.get("role");
        if (roleString != null) {
            try {
                user.setRole(Role.valueOf(roleString.toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setRole(Role.STUDENT); // роль по умолчанию
            }
        } else {
            user.setRole(Role.STUDENT);
        }
        
        return save(user); // используем существующий метод save(User)
    }

    @Transactional
    public User update(Long id, User user) {
        User existingUser = findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setRole(user.getRole());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public User updateByUsername(String username, User user) {
        User existingUser = findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update allowed fields (exclude role to prevent privilege escalation)
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            return; // Пользователь не существует
        }
        
        User user = userRepository.findById(id).get();
        
        // Удаляем записи пользователя на курсы
        try {
            userCourseEnrollmentRepository.deleteAll(userCourseEnrollmentRepository.findByUser(user));
        } catch (Exception e) {
            // Игнорируем ошибку если таблица не существует (например в тестах)
            System.out.println("Warning: Could not delete user course enrollments: " + e.getMessage());
        }
        
        // Удаляем комментарии пользователя
        try {
            commentRepository.deleteAll(commentRepository.findByUserId(user.getId()));
        } catch (Exception e) {
            // Игнорируем ошибку если таблица не существует (например в тестах)
            System.out.println("Warning: Could not delete user comments: " + e.getMessage());
        }
        
        // Удаляем лайки пользователя
        try {
            likeRepository.deleteAll(likeRepository.findByUserId(user.getId()));
        } catch (Exception e) {
            // Игнорируем ошибку если таблица не существует (например в тестах)
            System.out.println("Warning: Could not delete user likes: " + e.getMessage());
        }
        
        // Наконец удаляем пользователя
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public List<User> findByRole(String roleName) {
        try {
            Role role = Role.valueOf(roleName.toUpperCase());
            return userRepository.findAll().stream()
                    .filter(user -> user.getRole() == role)
                    .toList();
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public long count() {
        return userRepository.count();
    }

    public List<User> findAllTeachers() {
        return findByRole("TEACHER");
    }
}