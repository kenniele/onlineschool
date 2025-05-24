package com.onlineSchool.service;

import com.onlineSchool.model.User;
import com.onlineSchool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}