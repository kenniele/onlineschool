package com.onlineSchool.service;

import com.onlineSchool.exception.CourseEnrollmentException;
import com.onlineSchool.model.Course;
import com.onlineSchool.model.User;
import com.onlineSchool.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserService userService;

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getAllActiveCourses() {
        return courseRepository.findByActiveTrue();
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    public Optional<Course> findByIdWithDetails(Long id) {
        return courseRepository.findByIdWithDetails(id);
    }

    public List<Course> findByTeacher(User teacher) {
        return courseRepository.findByTeacher(teacher);
    }

    public List<Course> findByStudent(User student) {
        return courseRepository.findByStudentsContaining(student);
    }

    public List<Course> getCoursesByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    public List<Course> getCoursesByStudentId(Long studentId) {
        return courseRepository.findByStudentsId(studentId);
    }

    @Transactional
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    @Transactional
    public Course update(Long id, Course course) {
        Course existingCourse = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Курс с ID " + id + " не найден"));
        
        existingCourse.setTitle(course.getTitle());
        existingCourse.setDescription(course.getDescription());
        existingCourse.setTeacher(course.getTeacher());
        existingCourse.setStartDate(course.getStartDate());
        existingCourse.setEndDate(course.getEndDate());
        existingCourse.setActive(course.isActive());
        
        return courseRepository.save(existingCourse);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new EntityNotFoundException("Курс с ID " + id + " не найден");
        }
        courseRepository.deleteById(id);
    }

    @Transactional
    public Course activate(Long id) {
        Course course = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Курс с ID " + id + " не найден"));
        course.setActive(true);
        return courseRepository.save(course);
    }

    @Transactional
    public Course deactivate(Long id) {
        Course course = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Курс с ID " + id + " не найден"));
        course.setActive(false);
        return courseRepository.save(course);
    }

    @Transactional
    public Course enrollStudent(Long courseId, User student) {
        Course course = findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Курс с ID " + courseId + " не найден"));
        
        // Проверяем, что курс активен
        if (!course.isActive()) {
            throw new CourseEnrollmentException("Курс неактивен и недоступен для записи");
        }
        
        // Проверяем, что студент еще не записан
        if (course.getStudents().contains(student)) {
            throw new CourseEnrollmentException("Вы уже записаны на этот курс");
        }
        
        course.getStudents().add(student);
        return courseRepository.save(course);
    }

    @Transactional
    public Course unenrollStudent(Long courseId, User student) {
        Course course = findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Курс с ID " + courseId + " не найден"));
        
        if (!course.getStudents().contains(student)) {
            throw new CourseEnrollmentException("Вы не записаны на этот курс");
        }
        
        course.getStudents().remove(student);
        return courseRepository.save(course);
    }

    @Transactional
    public Course addStudent(Long courseId, Long studentId) {
        Course course = findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Курс с ID " + courseId + " не найден"));
        User student = userService.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + studentId + " не найден"));
        
        if (course.getStudents().contains(student)) {
            throw new CourseEnrollmentException("Студент уже записан на этот курс");
        }
        
        course.getStudents().add(student);
        return courseRepository.save(course);
    }

    @Transactional
    public Course removeStudent(Long courseId, Long studentId) {
        Course course = findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Курс с ID " + courseId + " не найден"));
        User student = userService.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + studentId + " не найден"));
        
        if (!course.getStudents().contains(student)) {
            throw new CourseEnrollmentException("Студент не записан на этот курс");
        }
        
        course.getStudents().remove(student);
        return courseRepository.save(course);
    }

    public boolean existsByTitleAndTeacher(String title, User teacher) {
        return courseRepository.existsByTitleAndTeacher(title, teacher);
    }

    public boolean isStudentEnrolled(Long courseId, String username) {
        Optional<Course> course = findByIdWithDetails(courseId);
        if (course.isEmpty()) {
            return false;
        }
        
        return course.get().getStudents().stream()
                .anyMatch(student -> student.getUsername().equals(username));
    }
} 