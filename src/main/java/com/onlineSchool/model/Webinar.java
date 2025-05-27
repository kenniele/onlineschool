package com.onlineSchool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id", "title"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "webinars")
public class Webinar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "meeting_url")
    private String meetingUrl;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Builder.Default
    @Column(name = "active")
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WebinarStatus status;

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "entity_id", referencedColumnName = "id")
    @SQLRestriction("entity_type = 'WEBINAR'")
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "webinar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WebinarMaterial> materials = new ArrayList<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "webinar_participants",
            joinColumns = @JoinColumn(name = "webinar_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "entity_id", referencedColumnName = "id")
    @SQLRestriction("entity_type = 'WEBINAR'")
    private List<Like> likes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = WebinarStatus.SCHEDULED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @AssertTrue(message = "End time must be after start time")
    private boolean isEndTimeValid() {
        LocalDateTime endTime = getEndTime();
        return endTime == null || startTime == null || endTime.isAfter(startTime);
    }

    public LocalDateTime getEndTime() {
        return startTime != null && duration != null ? startTime.plusMinutes(duration) : null;
    }
}