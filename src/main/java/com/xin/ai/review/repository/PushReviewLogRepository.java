package com.xin.ai.review.repository;

import com.xin.ai.review.entity.PushReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-04-25 17:27
 * @github https://github.com/xyf527
 * @copyright
 */

@Repository
public interface PushReviewLogRepository extends JpaRepository<PushReviewLog, Long> {

    @Query("SELECT p FROM PushReviewLog p WHERE " +
            "(:authors IS NULL OR p.author IN :authors) AND " +
            "(:projectNames IS NULL OR p.projectName IN :projectNames) AND " +
            "(:updatedAtGte IS NULL OR p.updatedAt >= :updatedAtGte) AND " +
            "(:updatedAtLte IS NULL OR p.updatedAt <= :updatedAtLte) " +
            "ORDER BY p.updatedAt DESC")
    List<PushReviewLog> findByFilters(
            @Param("authors") List<String> authors,
            @Param("projectNames") List<String> projectNames,
            @Param("updatedAtGte") Long updatedAtGte,
            @Param("updatedAtLte") Long updatedAtLte
    );

    @Query("SELECT DISTINCT p.author FROM PushReviewLog p ORDER BY p.author")
    List<String> findDistinctAuthors();

    @Query("SELECT DISTINCT p.projectName FROM PushReviewLog p ORDER BY p.projectName")
    List<String> findDistinctProjectNames();
}
