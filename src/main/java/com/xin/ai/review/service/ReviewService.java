package com.xin.ai.review.service;

import com.xin.ai.review.entity.MrReviewLog;
import com.xin.ai.review.entity.PushReviewLog;

import java.util.List;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-05-09 12:08
 * @github https://github.com/xyf527
 * @copyright
 */

public interface ReviewService {

    // 插入MR审查日志
    void insertMrReviewLog(MrReviewLog entity);

    // 插入Push审查日志
    void insertPushReviewLog(PushReviewLog entity);

    // 查询MR审查日志
    List<MrReviewLog> getMrReviewLogs(List<String> authors, List<String> projectNames, Long updatedAtGte, Long updatedAtLte);

    // 查询Push审查日志
    List<PushReviewLog> getPushReviewLogs(List<String> authors, List<String> projectNames, Long updatedAtGte, Long updatedAtLte);

    // 获取MR不重复的作者列表
    List<String> getMrDistinctAuthors();

    // 获取MR不重复的项目名称列表
    List<String> getMrDistinctProjectNames();

    // 获取Push不重复的作者列表
    List<String> getPushDistinctAuthors();

    // 获取Push不重复的项目名称列表
    List<String> getPushDistinctProjectNames();

}
