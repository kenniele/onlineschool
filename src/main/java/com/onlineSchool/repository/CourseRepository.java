package com.onlineSchool.repository;

import com.onlineSchool.model.Course;
import com.onlineSchool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByActiveTrue();
    List<Course> findByTeacher(User teacher);
    List<Course> findByTeacherId(Long teacherId);
    List<Course> findByStudentsId(Long studentId);
    List<Course> findByStudentsContaining(User student);
    boolean existsByTitleAndTeacher(String title, User teacher);
    
    @Query("SELECT c FROM Course c " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH c.students " +
           "WHERE c.id = :id")
    Optional<Course> findByIdWithDetails(@Param("id") Long id);
} 