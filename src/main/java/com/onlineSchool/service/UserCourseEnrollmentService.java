package com.onlineSchool.service;

import com.onlineSchool.model.Course;
import com.onlineSchool.model.EnrollmentStatus;
import com.onlineSchool.model.User;
import com.onlineSchool.model.UserCourseEnrollment;
import com.onlineSchool.repository.CourseRepository;
import com.onlineSchool.repository.UserCourseEnrollmentRepository;
import com.onlineSchool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("userCourseEnrollmentService") // Имя бина для использования в Thymeleaf
@RequiredArgsConstructor
@Transactional(readOnly = true) // Большинство методов будут для чтения
public class UserCourseEnrollmentService {

    private final UserCourseEnrollmentRepository userCourseEnrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public Optional<UserCourseEnrollment> findByUserAndCourse(User user, Course course) {
        return userCourseEnrollmentRepository.findByUserAndCourse(user, course);
    }

    public EnrollmentStatus getEnrollmentStatus(Long courseId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);

        if (user == null || course == null) {
            return null; // или выбросить исключение, или вернуть NOT_ENROLLED, если есть такой статус
        }

        return userCourseEnrollmentRepository.findByUserAndCourse(user, course)
                .map(UserCourseEnrollment::getStatus)
                .orElse(null); // Если запись не найдена, значит не записан
    }

    public EnrollmentStatus getEnrollmentStatus(Long courseId, UserDetails principal) {
        if (principal instanceof User) {
            User currentUser = (User) principal;
            return getEnrollmentStatus(courseId, currentUser.getId());
        }
        return null;
    }
    
    // Другие методы, связанные с UserCourseEnrollment, могут быть добавлены сюда
    // Например, для получения всех записей пользователя, всех записей на курс и т.д.
} 