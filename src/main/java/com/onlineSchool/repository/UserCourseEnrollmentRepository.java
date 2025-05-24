package com.onlineSchool.repository;

import com.onlineSchool.model.Course;
import com.onlineSchool.model.User;
import com.onlineSchool.model.UserCourseEnrollment;
import com.onlineSchool.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseEnrollmentRepository extends JpaRepository<UserCourseEnrollment, Long> {
    List<UserCourseEnrollment> findByUser(User user);
    List<UserCourseEnrollment> findByCourse(Course course);
    Optional<UserCourseEnrollment> findByUserAndCourse(User user, Course course);
    boolean existsByUserAndCourse(User user, Course course);
    List<UserCourseEnrollment> findByUserAndStatus(User user, EnrollmentStatus status);
    List<UserCourseEnrollment> findByCourseAndStatus(Course course, EnrollmentStatus status);
} 