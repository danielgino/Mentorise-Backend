package com.example.mentorisebackend.repository;

import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.enums.Role;
import com.example.mentorisebackend.repository.projection.stats.TopMajorProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> , JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);
    boolean existsByNationalId(String nationalId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmailAndIdNot(String email, Long id);
    List<User> findAllByRole(Role role);
    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    @Query("""
           SELECT m.name AS majorName, COUNT(u.id) AS count
           FROM User u
           JOIN u.major m
           WHERE u.role = :role
           GROUP BY m.id, m.name
           ORDER BY COUNT(u.id) DESC
           """)
    List<TopMajorProjection> findTopMajorsByRole(Role role, Pageable pageable);

    @Query("""
       SELECT m.name AS majorName, COUNT(u.id) AS count
       FROM User u
       JOIN u.major m
       WHERE u.role IN :roles
       GROUP BY m.id, m.name
       ORDER BY COUNT(u.id) DESC
       """)
    List<TopMajorProjection> findTopMajorsByRoles(List<Role> roles, Pageable pageable);

    @Query("SELECT u.major.id FROM User u WHERE u.id = :userId")
    Long findMajorIdByUserId(@Param("userId") Long userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.major WHERE u.id = :id")
    Optional<User> findByIdWithMajor(@Param("id") Long id);

    @EntityGraph(attributePaths = {"major"})
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @Query("SELECT u.email FROM User u WHERE u.id = :id")
    Optional<String> findEmailById(@Param("id") Long id);

}