package com.onlineSchool.service;

import com.onlineSchool.exception.WebinarException;
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
import java.util.Collections;

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
    public Optional<Webinar> findByIdWithDetails(Long id) {
        try {
            Optional<Webinar> webinarOpt = webinarRepository.findById(id);
            if (webinarOpt.isPresent()) {
                Webinar webinar = webinarOpt.get();
                // Принудительная загрузка связанных сущностей
                if (webinar.getTeacher() != null) {
                    webinar.getTeacher().getFirstName(); // Загружаем учителя
                }
                if (webinar.getCourse() != null) {
                    webinar.getCourse().getTitle(); // Загружаем курс
                }
                if (webinar.getParticipants() != null) {
                    webinar.getParticipants().size(); // Загружаем участников
                }
                if (webinar.getComments() != null) {
                    webinar.getComments().size(); // Загружаем комментарии
                }
                if (webinar.getLikes() != null) {
                    webinar.getLikes().size(); // Загружаем лайки
                }
                if (webinar.getMaterials() != null) {
                    webinar.getMaterials().size(); // Загружаем материалы
                }
            }
            return webinarOpt;
        } catch (Exception e) {
            System.err.println("Error loading webinar with details: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public List<Webinar> findByTeacher(User teacher) {
        return webinarRepository.findByTeacher(teacher);
    }

    public List<Webinar> findByParticipant(User participant) {
        return webinarRepository.findByParticipantsContaining(participant);
    }

    public List<Webinar> findUpcomingByTeacher(User teacher) {
        return webinarRepository.findByTeacherAndStartTimeAfter(teacher, LocalDateTime.now());
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
        try {
            return webinarRepository.findByStartTimeAfter(LocalDateTime.now())
                    .stream()
                    .filter(webinar -> webinar != null && webinar.isActive())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error finding upcoming webinars: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public List<Webinar> findPast() {
        try {
            LocalDateTime now = LocalDateTime.now();
            return webinarRepository.findAll().stream()
                    .filter(webinar -> webinar != null && 
                                     webinar.getEndTime() != null && 
                                     webinar.getEndTime().isBefore(now))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error finding past webinars: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public List<Webinar> findByParticipantId(Long userId) {
        try {
            return webinarRepository.findByParticipantId(userId);
        } catch (Exception e) {
            System.err.println("Error finding webinars by participant: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Transactional
    public Webinar save(Webinar webinar) {
        return webinarRepository.save(webinar);
    }

    @Transactional
    public Webinar update(Long id, Webinar webinar) {
        Webinar existingWebinar = webinarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Вебинар с ID " + id + " не найден"));
        
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
        if (!webinarRepository.existsById(id)) {
            throw new EntityNotFoundException("Вебинар с ID " + id + " не найден");
        }
        webinarRepository.deleteById(id);
    }

    @Transactional
    public Webinar start(Long id) {
        Webinar webinar = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Вебинар с ID " + id + " не найден"));
        
        if (webinar.getStatus() != WebinarStatus.SCHEDULED) {
            throw new WebinarException("Вебинар не может быть запущен. Текущий статус: " + webinar.getStatus());
        }
        
        webinar.setStatus(WebinarStatus.IN_PROGRESS);
        return webinarRepository.save(webinar);
    }

    @Transactional
    public Webinar complete(Long id) {
        Webinar webinar = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Вебинар с ID " + id + " не найден"));
        
        if (webinar.getStatus() != WebinarStatus.IN_PROGRESS) {
            throw new WebinarException("Вебинар не может быть завершен. Текущий статус: " + webinar.getStatus());
        }
        
        webinar.setStatus(WebinarStatus.COMPLETED);
        return webinarRepository.save(webinar);
    }

    @Transactional
    public Webinar cancel(Long id) {
        Webinar webinar = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Вебинар с ID " + id + " не найден"));
        
        if (webinar.getStatus() != WebinarStatus.SCHEDULED) {
            throw new WebinarException("Вебинар не может быть отменен. Текущий статус: " + webinar.getStatus());
        }
        
        webinar.setStatus(WebinarStatus.CANCELLED);
        return webinarRepository.save(webinar);
    }

    @Transactional
    public Webinar addParticipant(Long webinarId, User user) {
        Webinar webinar = findById(webinarId)
                .orElseThrow(() -> new EntityNotFoundException("Вебинар с ID " + webinarId + " не найден"));
        
        // Проверяем статус вебинара
        if (webinar.getStatus() != WebinarStatus.SCHEDULED) {
            throw new WebinarException("Нельзя записаться на вебинар. Статус: " + webinar.getStatus());
        }
        
        // Проверяем активность
        if (!webinar.isActive()) {
            throw new WebinarException("Вебинар неактивен");
        }
        
        // Проверяем лимит участников
        if (webinar.getMaxParticipants() != null && 
            webinar.getParticipants().size() >= webinar.getMaxParticipants()) {
            throw new WebinarException("Достигнут лимит участников вебинара");
        }
        
        // Проверяем, что пользователь еще не записан
        if (webinar.getParticipants().contains(user)) {
            throw new WebinarException("Вы уже записаны на этот вебинар");
        }
        
        webinar.getParticipants().add(user);
        return webinarRepository.save(webinar);
    }

    @Transactional
    public Webinar removeParticipant(Long webinarId, User user) {
        Webinar webinar = findById(webinarId)
                .orElseThrow(() -> new EntityNotFoundException("Вебинар с ID " + webinarId + " не найден"));
        
        if (!webinar.getParticipants().contains(user)) {
            throw new WebinarException("Вы не записаны на этот вебинар");
        }
        
        webinar.getParticipants().remove(user);
        return webinarRepository.save(webinar);
    }

    @Transactional
    public Webinar addParticipant(Long webinarId, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));
        
        return addParticipant(webinarId, user);
    }

    @Transactional
    public Webinar removeParticipant(Long webinarId, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));
        
        return removeParticipant(webinarId, user);
    }

    @Transactional
    public Webinar deactivate(Long id) {
        Webinar webinar = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Вебинар с ID " + id + " не найден"));
        
        webinar.setActive(false);
        webinar.setStatus(WebinarStatus.CANCELLED);
        return webinarRepository.save(webinar);
    }

    @Transactional(readOnly = true)
    public boolean isUserParticipant(Long webinarId, String username) {
        try {
            Optional<Webinar> webinarOpt = webinarRepository.findById(webinarId);
            if (webinarOpt.isEmpty()) {
                return false;
            }
            
            Webinar webinar = webinarOpt.get();
            return webinar.getParticipants() != null && 
                   webinar.getParticipants().stream()
                           .anyMatch(participant -> participant.getUsername().equals(username));
        } catch (Exception e) {
            System.err.println("Error checking user participation: " + e.getMessage());
            return false;
        }
    }
} 