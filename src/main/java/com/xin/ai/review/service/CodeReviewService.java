package com.xin.ai.review.service;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-05-09 09:28
 * @github https://github.com/xyf527
 * @copyright
 */

public interface CodeReviewService {

    /**
     * 审查代码并去除markdown格式
     * @param changesText Git diff格式的代码变更文本
     * @param commitText 提交信息文本
     * @return 去除markdown格式后的审查结果
     */
    String reviewAndStripCode(String changesText, String commitText);

    /**
     * 从diff文本中检测主要编程语言
     * @param diffsText Git diff格式的代码变更文本
     * @return 检测到的编程语言 默认返回default
     */
    String detectLanguageFromDiff(String diffsText);

    /**
     * 解析Ai返回的审查结果中的分数
     * @param reviewText AI返回的审查结果文本
     * @return 审查分数 未找到则返回0
     */
    int parseReviewScore(String reviewText);

}
