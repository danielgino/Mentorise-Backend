package com.example.mentorisebackend.api.entity;

import com.example.mentorisebackend.enums.ScopeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tutor_scopes",
        indexes = {
                @Index(name = "idx_tutor_scopes_tutor_course", columnList = "tutor_id, scope_type, course_id"),
                @Index(name = "idx_tutor_scopes_tutor_year", columnList = "tutor_id, scope_type, year_number")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uq_tutor_scopes_year", columnNames={"tutor_id","scope_type","year_number"}),
                @UniqueConstraint(name="uq_tutor_scopes_course", columnNames={"tutor_id","scope_type","course_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorScope {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    private ScopeType scopeType;

    @Column(name = "year_number")
    private Integer yearNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


}
