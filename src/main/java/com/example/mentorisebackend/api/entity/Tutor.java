package com.example.mentorisebackend.api.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Tutors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 300)
    private String bio;

    @Column(name = "rating_avg")
    private Double ratingAvg;

    @Column(name = "total_reviews")
    private Integer totalReviews;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "tutor_image_url", length = 500)
    private String tutorImageUrl;

    @Column(name = "tutor_image_public_id", length = 255)
    private String tutorImagePublicId;
}
