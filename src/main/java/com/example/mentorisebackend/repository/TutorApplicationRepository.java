package com.example.mentorisebackend.repository;


import com.example.mentorisebackend.api.entity.TutorApplication;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorApplicationRepository extends JpaRepository<TutorApplication, Long> {


    @EntityGraph(attributePaths = {"user", "user.major"})
    Page<TutorApplication> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.major"})
    Page<TutorApplication> findByStatus(Status status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.major"})
    Page<TutorApplication> findByUser_NationalId(String nationalId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.major"})
    Page<TutorApplication> findByStatusAndUser_NationalId(Status status, String nationalId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.major"})
    Page<TutorApplication> findByUser_NationalIdStartsWith(String nationalIdPrefix, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.major"})
    Page<TutorApplication> findByStatusAndUser_NationalIdStartsWith(Status status, String nationalIdPrefix, Pageable pageable);


    @Query("""
    SELECT DISTINCT a FROM TutorApplication a
    LEFT JOIN FETCH a.scopes s
    LEFT JOIN FETCH s.course c
    LEFT JOIN FETCH a.user u
    LEFT JOIN FETCH a.reviewedBy r
    WHERE a.id = :id
""")
    Optional<TutorApplication> findWithScopesById(@Param("id") Long id);

    boolean existsByUserAndStatus(User user, Status status);

    Optional<TutorApplication> findFirstByUserAndStatus(User user, Status status);

    Optional<TutorApplication> findFirstByUserOrderByCreatedAtDesc(User user);
}