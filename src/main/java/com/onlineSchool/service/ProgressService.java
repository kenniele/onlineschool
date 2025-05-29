package com.onlineSchool.service;

import com.onlineSchool.model.Course;
import com.onlineSchool.model.User;
import com.onlineSchool.model.Webinar;
import com.onlineSchool.model.WebinarStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProgressService {

    @Autowired
    private CourseService courseService;

    @Autowired
    private WebinarService webinarService;

    /**
     * Рассчитывает прогресс студента по конкретному курсу
     * Прогресс = количество пройденных вебинаров / общее количество вебинаров * 100
     */
    public int calculateCourseProgress(Course course, User student) {
        if (course == null || course.getId() == null || student == null) {
            return 0;
        }
        
        List<Webinar> courseWebinars = webinarService.findByCourseId(course.getId());
        
        if (courseWebinars == null || courseWebinars.isEmpty()) {
            return 0;
        }

        // Считаем пройденные вебинары (те, что уже завершились по времени и где студент участвует)
        long completedWebinars = courseWebinars.stream()
                .filter(webinar -> webinar != null && webinar.getStartTime() != null)
                .filter(webinar -> webinar.getStartTime().isBefore(LocalDateTime.now()))
                .filter(webinar -> webinar.getParticipants() != null && webinar.getParticipants().contains(student))
                .count();

        int totalWebinars = courseWebinars.size();
        
        return totalWebinars > 0 ? (int) Math.round((double) completedWebinars / totalWebinars * 100) : 0;
    }

    /**
     * Рассчитывает общий прогресс студента по всем курсам
     */
    public int calculateOverallProgress(User student) {
        if (student == null) {
            return 0;
        }
        
        List<Course> enrolledCourses = courseService.findByStudent(student);
        
        if (enrolledCourses == null || enrolledCourses.isEmpty()) {
            return 0;
        }

        int totalProgress = 0;
        int coursesWithWebinars = 0;

        for (Course course : enrolledCourses) {
            if (course != null) {
                List<Webinar> courseWebinars = webinarService.findByCourseId(course.getId());
                if (courseWebinars != null && !courseWebinars.isEmpty()) {
                    totalProgress += calculateCourseProgress(course, student);
                    coursesWithWebinars++;
                }
            }
        }

        return coursesWithWebinars > 0 ? totalProgress / coursesWithWebinars : 0;
    }

    /**
     * Возвращает детальную статистику прогресса студента
     */
    public Map<String, Object> getProgressStatistics(User student) {
        Map<String, Object> stats = new HashMap<>();
        
        if (student == null) {
            stats.put("totalCourses", 0);
            stats.put("totalAttendedWebinars", 0);
            stats.put("completedWebinars", 0);
            stats.put("totalWebinarsInCourses", 0);
            stats.put("overallProgress", 0);
            stats.put("completedCourses", 0);
            return stats;
        }
        
        List<Course> enrolledCourses = courseService.findByStudent(student);
        List<Webinar> attendedWebinars = webinarService.findByParticipant(student);
        
        // Защита от null
        if (enrolledCourses == null) enrolledCourses = List.of();
        if (attendedWebinars == null) attendedWebinars = List.of();
        
        // Общие статистики
        stats.put("totalCourses", enrolledCourses.size());
        stats.put("totalAttendedWebinars", attendedWebinars.size());
        
        // Считаем пройденные вебинары (те, которые уже завершились по времени)
        long completedWebinars = attendedWebinars.stream()
                .filter(webinar -> webinar != null && webinar.getStartTime() != null)
                .filter(webinar -> webinar.getStartTime().isBefore(LocalDateTime.now()))
                .count();
        
        stats.put("completedWebinars", completedWebinars);
        
        // Считаем общее количество вебинаров во всех курсах студента
        int totalWebinarsInCourses = enrolledCourses.stream()
                .filter(course -> course != null)
                .mapToInt(course -> {
                    List<Webinar> courseWebinars = webinarService.findByCourseId(course.getId());
                    return courseWebinars != null ? courseWebinars.size() : 0;
                })
                .sum();
        
        stats.put("totalWebinarsInCourses", totalWebinarsInCourses);
        
        // Общий прогресс
        int overallProgress = calculateOverallProgress(student);
        stats.put("overallProgress", overallProgress);
        
        // Завершенные курсы (курсы, где прогресс = 100%)
        long completedCourses = enrolledCourses.stream()
                .filter(course -> course != null)
                .filter(course -> calculateCourseProgress(course, student) == 100)
                .count();
        
        stats.put("completedCourses", completedCourses);
        
        return stats;
    }

    /**
     * Возвращает прогресс по всем курсам студента
     */
    public Map<Course, Integer> getCourseProgressMap(User student) {
        Map<Course, Integer> progressMap = new HashMap<>();
        List<Course> enrolledCourses = courseService.findByStudent(student);
        
        if (enrolledCourses != null) {
            for (Course course : enrolledCourses) {
                int progress = calculateCourseProgress(course, student);
                progressMap.put(course, progress);
            }
        }
        
        return progressMap;
    }

    /**
     * Проверяет, завершен ли курс студентом
     */
    public boolean isCourseCompleted(Course course, User student) {
        return calculateCourseProgress(course, student) == 100;
    }

    /**
     * Возвращает количество пройденных и общее количество вебинаров в курсе
     */
    public Map<String, Integer> getWebinarCounts(Course course, User student) {
        Map<String, Integer> counts = new HashMap<>();
        
        if (course == null || course.getId() == null) {
            counts.put("completed", 0);
            counts.put("total", 0);
            return counts;
        }
        
        List<Webinar> courseWebinars = webinarService.findByCourseId(course.getId());
        if (courseWebinars == null || courseWebinars.isEmpty()) {
            counts.put("completed", 0);
            counts.put("total", 0);
            return counts;
        }

        int totalWebinars = courseWebinars.size();
        
        long completedWebinars = courseWebinars.stream()
                .filter(webinar -> webinar.getStartTime() != null)
                .filter(webinar -> webinar.getStartTime().isBefore(LocalDateTime.now()))
                .filter(webinar -> webinar.getParticipants() != null && webinar.getParticipants().contains(student))
                .count();

        counts.put("completed", (int) completedWebinars);
        counts.put("total", totalWebinars);
        
        return counts;
    }
} 