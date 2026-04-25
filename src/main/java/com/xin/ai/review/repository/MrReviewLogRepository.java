package com.xin.ai.review.repository;

import com.xin.ai.review.entity.MrReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-04-22 21:42
 * @github https://github.com/xyf527
 * @copyright
 */

@Repository
public interface MrReviewLogRepository extends JpaRepository<MrReviewLog, Long> {

    @Query("SELECT m FROM MrReviewLog m WHERE " +
            "(:authors IS NULL OR m.author IN :authors) AND " +
            "(:projectNames IS NULL OR m.projectName IN :projectNames) AND " +
            "(:updatedAtGte IS NULL OR m.updatedAt >= :updatedAtGte) AND " +
            "(:updatedAtLte IS NULL OR m.updatedAt <= :updatedAtLte) " +
            "ORDER BY m.updatedAt DESC")
    List<MrReviewLog> findByFilters(
            @Param("authors") List<String> authors,
            @Param("projectNames") List<String> projectNames,
            @Param("updatedAtGte") Long updatedAtGte,
            @Param("updatedAtLte") Long updatedAtLte
    );

    @Query("SELECT DISTINCT m.author FROM MrReviewLog m ORDER BY m.author")
    List<String> findDistinctAuthors();

    @Query("SELECT DISTINCT m.projectName FROM MrReviewLog m ORDER BY m.projectName")
    List<String> findDistinctProjectNames();

}
