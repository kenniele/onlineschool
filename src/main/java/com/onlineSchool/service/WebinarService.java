package com.onlineSchool.service;

import com.onlineSchool.model.Webinar;
import com.onlineSchool.model.WebinarStatus;
import com.onlineSchool.model.User;
import com.onlineSchool.repository.WebinarRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebinarService {
    private final WebinarRepository webinarRepository;
    private final UserService userService;
    private final CourseService courseService;

    public List<Webinar> findAll() {
        return webinarRepository.findAll();
    }

    public Optional<Webinar> findById(Long id) {
        return webinarRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Webinar> findByCourseId(Long courseId) {
        return webinarRepository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<Webinar> findByTeacherId(Long teacherId) {
        return webinarRepository.findByTeacherId(teacherId);
    }

    @Transactional(readOnly = true)
    public List<Webinar> findUpcoming() {
        return webinarRepository.findByStartTimeAfter(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Webinar> findPast() {
        LocalDateTime now = LocalDateTime.now();
        return webinarRepository.findAll().stream()
                .filter(webinar -> webinar.getEndTime() != null && webinar.getEndTime().isBefore(now))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Webinar> findByParticipantId(Long userId) {
        return webinarRepository.findByParticipantId(userId);
    }

    @Transactional
    public Webinar save(Webinar webinar) {
        return webinarRepository.save(webinar);
    }

    @Transactional
    public Webinar update(Long id, Webinar webinar) {
        Webinar existingWebinar = webinarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Webinar not found"));
        
        existingWebinar.setTitle(webinar.getTitle());
        existingWebinar.setDescription(webinar.getDescription());
        existingWebinar.setStartTime(webinar.getStartTime());
        existingWebinar.setDuration(webinar.getDuration());
        existingWebinar.setMaxParticipants(webinar.getMaxParticipants());
        existingWebinar.setCourse(webinar.getCourse());
        existingWebinar.setTeacher(webinar.getTeacher());
        existingWebinar.setActive(webinar.isActive());
        existingWebinar.setStatus(webinar.getStatus());
        
        return webinarRepository.save(existingWebinar);
    }

    @Transactional
    public void deleteById(Long id) {
        webinarRepository.deleteById(id);
    }

    @Transactional
    public Webinar start(Long id) {
        Webinar webinar = findById(id)
                .orElseThrow(() -> new RuntimeException("Webinar not found"));
        
        if (webinar.getStatus() != WebinarStatus.SCHEDULED) {
            throw new RuntimeException("Webinar cannot be started");
        }
        
        webinar.setStatus(WebinarStatus.IN_PROGRESS);
        return webinarRepository.save(webinar);
    }

    @Transactional
    public Webinar complete(Long id) {
        Webinar webinar = findById(id)
                .orElseThrow(() -> new RuntimeException("Webinar not found"));
        
        if (webinar.getStatus() != WebinarStatus.IN_PROGRESS) {
            throw new RuntimeException("Webinar cannot be completed");
        }
        
        webinar.setStatus(WebinarStatus.COMPLETED);
        return webinarRepository.save(webinar);
    }

    @Transactional
    public Webinar cancel(Long id) {
        Webinar webinar = findById(id)
                .orElseThrow(() -> new RuntimeException("Webinar not found"));
        
        if (webinar.getStatus() != WebinarStatus.SCHEDULED) {
            throw new RuntimeException("Webinar cannot be cancelled");
        }
        
        webinar.setStatus(WebinarStatus.CANCELLED);
        return webinarRepository.save(webinar);
    }

    @Transactional
    public Webinar addParticipant(Long webinarId, Long userId) {
        Webinar webinar = findById(webinarId)
                .orElseThrow(() -> new RuntimeException("Webinar not found"));
        
        if (webinar.getParticipants().size() >= 100) {
            throw new RuntimeException("Webinar is full");
        }
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        webinar.getParticipants().add(user);
        return webinarRepository.save(webinar);
    }

    @Transactional
    public Webinar removeParticipant(Long webinarId, Long userId) {
        Webinar webinar = findById(webinarId)
                .orElseThrow(() -> new RuntimeException("Webinar not found"));
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        webinar.getParticipants().remove(user);
        return webinarRepository.save(webinar);
    }

    @Transactional
    public void deactivate(Long id) {
        Webinar webinar = findById(id)
                .orElseThrow(() -> new RuntimeException("Webinar not found"));
        
        webinar.setStatus(WebinarStatus.CANCELLED);
        webinarRepository.save(webinar);
    }
} 