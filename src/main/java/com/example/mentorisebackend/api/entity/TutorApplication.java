package com.example.mentorisebackend.api.entity;

import com.example.mentorisebackend.enums.ApplicationType;
import com.example.mentorisebackend.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tutor_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorApplication {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_app_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.PENDING;

    @Column(name = "request_text", length = 500)
    private String requestText;

    @Column(name = "transcript_url", length = 255)
    private String transcriptUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by",
            foreignKey = @ForeignKey(name = "fk_app_reviewer"))
    private User reviewedBy;

    @Column(name = "admin_comment", length = 500)
    private String adminComment;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false, length = 10)
    private ApplicationType applicationType = ApplicationType.INITIAL;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TutorApplicationScope> scopes;
}
