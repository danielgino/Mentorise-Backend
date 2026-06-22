package com.example.mentorisebackend.repository;


import com.example.mentorisebackend.api.entity.Tutor;
import com.example.mentorisebackend.repository.projection.stats.TopMajorProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
    Optional<Tutor> findByUser_Id(Long userId);
    List<Tutor> findByUserIdIn(List<Long> userIds);
    boolean existsByUser_IdAndIsActiveTrue(Long userId);

    @Query("""
           SELECT m.name AS majorName, COUNT(t.id) AS count
           FROM Tutor t
           JOIN t.user u
           JOIN u.major m
           GROUP BY m.id, m.name
           ORDER BY COUNT(t.id) DESC
           """)
    List<TopMajorProjection> findTopTutorMajors(Pageable pageable);
}
