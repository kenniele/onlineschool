package com.onlineSchool.repository;

import com.onlineSchool.model.Webinar;
import com.onlineSchool.model.WebinarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebinarRepository extends JpaRepository<Webinar, Long> {
    List<Webinar> findByCourseId(Long courseId);
    List<Webinar> findByTeacherId(Long teacherId);
    List<Webinar> findByStatus(WebinarStatus status);
    List<Webinar> findByStartTimeAfter(LocalDateTime dateTime);
    List<Webinar> findByStartTimeBefore(LocalDateTime dateTime);
    
    @Query("SELECT w FROM Webinar w JOIN w.participants p WHERE p.id = :userId")
    List<Webinar> findByParticipantId(@Param("userId") Long userId);
    
    @Query("SELECT w FROM Webinar w WHERE w.status = 'SCHEDULED' AND w.startTime > CURRENT_TIMESTAMP")
    List<Webinar> findUpcomingWebinars();
    
    @Query("SELECT w FROM Webinar w WHERE w.status = 'COMPLETED' OR w.startTime < CURRENT_TIMESTAMP")
    List<Webinar> findPastWebinars();
} 