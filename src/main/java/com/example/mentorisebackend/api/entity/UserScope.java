package com.example.mentorisebackend.api.entity;
import com.example.mentorisebackend.enums.ScopeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "user_scopes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_year", columnNames = {"user_id", "scope_type", "year_number"}),
                @UniqueConstraint(name = "uq_user_course", columnNames = {"user_id", "scope_type", "course_id"})
        },
        indexes = {
                @Index(name = "idx_user_scopes_user", columnList = "user_id"),
                @Index(name = "idx_user_scopes_type", columnList = "scope_type")
        }
)
@Check(name = "chk_user_scopes_type_fields", constraints =
        "(scope_type = 'YEAR' AND year_number IS NOT NULL AND course_id IS NULL) OR " +
                "(scope_type = 'COURSE' AND course_id IS NOT NULL AND year_number IS NULL)"
)
@Check(name = "chk_user_scopes_year_range", constraints =
        "year_number IS NULL OR (year_number BETWEEN 1 AND 6)"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 10)
    private ScopeType scopeType;

    @Min(1)
    @Max(6)
    @Column(name = "year_number")
    private Integer yearNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    @PreUpdate
    private void validate() {
        if (scopeType == null) {
            throw new IllegalStateException("scopeType is required");
        }

        if (scopeType == ScopeType.YEAR) {
            if (yearNumber == null) throw new IllegalStateException("YEAR scope חייב yearNumber");
            if (yearNumber < 1 || yearNumber > 6) throw new IllegalStateException("yearNumber חייב להיות בין 1 ל-6");
            if (course != null) throw new IllegalStateException("YEAR scope לא יכול להכיל course");
        }

        if (scopeType == ScopeType.COURSE) {
            if (course == null) throw new IllegalStateException("COURSE scope חייב course");
            if (yearNumber != null) throw new IllegalStateException("COURSE scope לא יכול להכיל yearNumber");
        }
    }


}
