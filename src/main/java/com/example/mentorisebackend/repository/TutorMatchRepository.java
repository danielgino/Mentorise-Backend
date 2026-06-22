package com.example.mentorisebackend.repository;

import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.repository.projection.tutor.RankedTutorRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TutorMatchRepository extends Repository<User, Long> {

    @Query(value = """
      SELECT id, MIN(tier) AS match_rank
      FROM (
          SELECT u.id,
                 CASE ts.scope_type
                     WHEN 'YEAR'   THEN 1
                     WHEN 'COURSE' THEN 2
                     ELSE 9
                 END AS tier
          FROM users u
          JOIN tutors t
            ON t.user_id = u.id AND t.is_active = 1
          LEFT JOIN tutor_scopes ts
            ON ts.tutor_id = t.id
          WHERE u.major_id = :majorId
            AND u.role = 'TUTOR'
            AND (:excludeEmpty = 1 OR u.id NOT IN (:excludeIds))
      ) ranked
      GROUP BY id
      HAVING (match_rank > :lastRank OR (match_rank = :lastRank AND id > :lastId))
      ORDER BY match_rank ASC, id ASC
      LIMIT :limit
      """, nativeQuery = true)
    List<RankedTutorRow> findMajorUnified(
            @Param("majorId") Long majorId,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("excludeEmpty") int excludeEmpty,
            @Param("lastRank") int lastRank,
            @Param("lastId") long lastId,
            @Param("limit") int limit
    );

    @Query(value = """
      SELECT id, MIN(tier) AS match_rank
      FROM (
          SELECT u.id,
                 1 AS tier
          FROM users u
          JOIN tutors t
            ON t.user_id = u.id AND t.is_active = 1
          JOIN tutor_scopes ts
            ON ts.tutor_id = t.id
          WHERE u.major_id = :majorId
            AND u.role = 'TUTOR'
            AND ts.scope_type = 'YEAR'
            AND ts.year_number IN (:years)
            AND (:excludeEmpty = 1 OR u.id NOT IN (:excludeIds))

          UNION ALL

          SELECT u.id,
                 9 AS tier
          FROM users u
          JOIN tutors t
            ON t.user_id = u.id AND t.is_active = 1
          WHERE u.major_id = :majorId
            AND u.role = 'TUTOR'
            AND (:excludeEmpty = 1 OR u.id NOT IN (:excludeIds))
      ) ranked
      GROUP BY id
      HAVING (match_rank > :lastRank OR (match_rank = :lastRank AND id > :lastId))
      ORDER BY match_rank ASC, id ASC
      LIMIT :limit
      """, nativeQuery = true)
    List<RankedTutorRow> findYearUnified(
            @Param("majorId") Long majorId,
            @Param("years") List<Integer> years,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("excludeEmpty") int excludeEmpty,
            @Param("lastRank") int lastRank,
            @Param("lastId") long lastId,
            @Param("limit") int limit
    );

    @Query(value = """
      SELECT id, MIN(tier) AS match_rank
      FROM (
          SELECT u.id,
                 1 AS tier
          FROM users u
          JOIN tutors t
            ON t.user_id = u.id AND t.is_active = 1
          JOIN tutor_scopes ts
            ON ts.tutor_id = t.id
          WHERE u.major_id = :majorId
            AND u.role = 'TUTOR'
            AND ts.scope_type = 'COURSE'
            AND ts.course_id IN (:courseIds)
            AND (:excludeEmpty = 1 OR u.id NOT IN (:excludeIds))

          UNION ALL

          SELECT u.id,
                 2 AS tier
          FROM users u
          JOIN tutors t
            ON t.user_id = u.id AND t.is_active = 1
          JOIN tutor_scopes ts
            ON ts.tutor_id = t.id
          WHERE u.major_id = :majorId
            AND u.role = 'TUTOR'
            AND :yearsEmpty = 0
            AND ts.scope_type = 'YEAR'
            AND ts.year_number IN (:years)
            AND (:excludeEmpty = 1 OR u.id NOT IN (:excludeIds))

          UNION ALL

          SELECT u.id,
                 9 AS tier
          FROM users u
          JOIN tutors t
            ON t.user_id = u.id AND t.is_active = 1
          WHERE u.major_id = :majorId
            AND u.role = 'TUTOR'
            AND (:excludeEmpty = 1 OR u.id NOT IN (:excludeIds))
      ) ranked
      GROUP BY id
      HAVING (match_rank > :lastRank OR (match_rank = :lastRank AND id > :lastId))
      ORDER BY match_rank ASC, id ASC
      LIMIT :limit
      """, nativeQuery = true)
    List<RankedTutorRow> findCourseUnified(
            @Param("majorId") Long majorId,
            @Param("courseIds") List<Long> courseIds,
            @Param("years") List<Integer> years,
            @Param("yearsEmpty") int yearsEmpty,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("excludeEmpty") int excludeEmpty,
            @Param("lastRank") int lastRank,
            @Param("lastId") long lastId,
            @Param("limit") int limit
    );
}
