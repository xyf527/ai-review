package com.xin.ai.review.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xin.ai.review.constant.AiReviewConstants;
import com.xin.ai.review.entity.MrReviewLog;
import com.xin.ai.review.entity.PushReviewLog;
import com.xin.ai.review.llm.LLMClient;
import com.xin.ai.review.llm.LLMFactory;
import com.xin.ai.review.service.ReportService;
import com.xin.ai.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-05-09 12:28
 * @github https://github.com/xyf527
 * @copyright
 */

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private LLMFactory llmFactory;

    @Value("${review.push-enabled:false}")
    private boolean pushReviewEnabled;

    // 定时生成日报
    @Scheduled(cron = "${scheduler.report-cron:0 0 18 * * MON-FRI}")
    public void scheduledDailyReport() {
        log.info("Starting scheduled daily report");
        generateAndSendDailyReport();
    }

    @Override
    public String generateAndSendDailyReport() {
        try {
            // 1.获取今日时间范围
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
            long startTs = startOfDay.atZone(ZoneId.systemDefault()).toEpochSecond();
            long endTs = endOfDay.atZone(ZoneId.systemDefault()).toEpochSecond();

            List<Map<String, Object>> records = new ArrayList<>();

            // 2.根据配置查询MR或Push日志
            if (pushReviewEnabled) {
                List<PushReviewLog> pushLogs = reviewService.getPushReviewLogs(null, null, startTs, endTs);
                for (PushReviewLog l : pushLogs) {
                    records.add(Map.of(
                            AiReviewConstants.JSON_FIELD_AUTHOR, l.getAuthor() != null ? l.getAuthor() : "",
                            AiReviewConstants.JSON_FIELD_PROJECT_NAME, l.getProjectName() != null ? l.getProjectName() : "",
                            AiReviewConstants.BRANCH, l.getBranch() != null ? l.getBranch() : "",
                            AiReviewConstants.JSON_FIELD_COMMIT_MESSAGES, l.getCommitMessages() != null ? l.getCommitMessages() : "",
                            AiReviewConstants.JSON_FIELD_SCORE, l.getScore() != null ? l.getScore() : AiReviewConstants.ZERO
                    ));
                }
            } else {
                List<MrReviewLog> mrLogs = reviewService.getMrReviewLogs(null, null, startTs, endTs);
                for (MrReviewLog l : mrLogs) {
                    records.add(Map.of(
                            AiReviewConstants.JSON_FIELD_AUTHOR, l.getAuthor() != null ? l.getAuthor() : "",
                            AiReviewConstants.JSON_FIELD_PROJECT_NAME, l.getProjectName() != null ? l.getProjectName() : "",
                            AiReviewConstants.SOURCE_BRANCH, l.getSourceBranch() != null ? l.getSourceBranch() : "",
                            AiReviewConstants.TARGET_BRANCH, l.getTargetBranch() != null ? l.getTargetBranch() : "",
                            AiReviewConstants.JSON_FIELD_COMMIT_MESSAGES, l.getCommitMessages() != null ? l.getCommitMessages() : "",
                            AiReviewConstants.JSON_FIELD_SCORE, l.getScore() != null ? l.getScore() : AiReviewConstants.ZERO
                    ));
                }
            }

            if (records.isEmpty()) {
                log.info("No records for daily report");
                return AiReviewConstants.MSG_NO_DAILY_RECORDS;
            }

            // 去重 基于author commit_messages组合
            Set<String> seen = new LinkedHashSet<>();
            List<Map<String, Object>> deduped = new ArrayList<>();
            for (Map<String, Object> r : records) {
                String key = r.get(AiReviewConstants.AUTHOR) + "|" + r.get(AiReviewConstants.COMMIT_MESSAGES);
                if (seen.add(key)) {
                    deduped.add(r);
                }
            }

            // 按照author排序
            deduped.sort(Comparator.comparing(r -> String.valueOf(r.get(AiReviewConstants.AUTHOR))));

            // 使用LLM生成日报
            String reportContent = generateReport(deduped);

            // TODO发送通知
            return reportContent;
        } catch (Exception e) {
            log.error("Failed to generate daily report: {}", e.getMessage(), e);
            return AiReviewConstants.MSG_GENERATE_DAILY_REPORT_FAILED_PREFIX + e.getMessage();
        }
    }

    private String generateReport(List<Map<String, Object>> records) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper.writeValueAsString(records);

            List<Map<String, String>> messages = List.of(
                    Map.of(
                            AiReviewConstants.LLM_FIELD_ROLE, AiReviewConstants.LLM_ROLE_USER,
                            AiReviewConstants.LLM_FIELD_CONTENT, AiReviewConstants.LLM_DAILY_REPORT_PROMPT + data
                    )
            );

            LLMClient client = llmFactory.getClient();
            return client.completions(messages);
        } catch (Exception e) {
            log.error("Failed to generate daily report with LLM: {}", e.getMessage(), e);
            // 生成简单的日报
            StringBuilder report = new StringBuilder(AiReviewConstants.MSG_DAILY_REPORT_FALLBACK_HEADER);
            for (Map<String, Object> r : records) {
                report.append("- **").append(r.get(AiReviewConstants.JSON_FIELD_AUTHOR)).append("** 提交到")
                        .append(r.get(AiReviewConstants.JSON_FIELD_PROJECT_NAME)).append("：")
                        .append(r.get(AiReviewConstants.JSON_FIELD_COMMIT_MESSAGES)).append("（评分：")
                        .append(r.get(AiReviewConstants.JSON_FIELD_SCORE)).append("分）\n");
            }
            return report.toString();
        }
    }

}
