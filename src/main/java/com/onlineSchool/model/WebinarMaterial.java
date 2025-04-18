package com.onlineSchool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = {"id", "filePath"})
@NoArgsConstructor
@Entity
@Table(name = "webinar_materials", uniqueConstraints = @UniqueConstraint(columnNames = {"webinar_id", "filePath"}))
public class WebinarMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String filePath;

    @NotBlank
    @Column(nullable = false)
    private String fileType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webinar_id", nullable = false)
    private Webinar webinar;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}