package com.example.mentorisebackend.api.entity;

import com.example.mentorisebackend.enums.ScopeType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tutor_application_scopes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorApplicationScope {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_scope_application"))
    private TutorApplication application;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 10)
    private ScopeType scopeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", foreignKey = @ForeignKey(name = "fk_scope_course"))
    private Course course;

    @Column(name = "year_number")
    private Integer yearNumber;
}
