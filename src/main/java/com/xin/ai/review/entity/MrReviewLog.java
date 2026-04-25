package com.xin.ai.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-04-22 21:05
 * @github https://github.com/xyf527
 * @copyright
 */

@Data
@Entity
@Table(name = "mr_review_log")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MrReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name", length = 255)
    private String projectName;

    @Column(name = "author", length = 255)
    private String author;

    @Column(name = "source_branch", length = 255)
    private String sourceBranch;

    @Column(name = "target_branch", length = 255)
    private String targetBranch;

    @Column(name = "update_at")
    private Long updatedAt;

    @Column(name = "commit_messages", columnDefinition = "TEXT")
    private String commitMessages;

    @Column(name = "score")
    private Integer score;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;

    @Column(name = "review_result", columnDefinition = "TEXT")
    private String reviewResult;

    @Column(name = "additions")
    @Builder.Default
    private Integer additions = 0;

    @Column(name = "deletions")
    @Builder.Default
    private Integer deletions = 0;

}
