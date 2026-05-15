package com.xin.ai.review.controller;

import com.xin.ai.review.constant.AiReviewConstants;
import com.xin.ai.review.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-05-14 10:21
 * @github https://github.com/xyf527
 * @copyright
 */

@RestController
@Slf4j
@RequestMapping("/review")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping(value = "/daily_report", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> dailyReport() {
        try {
            String reportContent = reportService.generateAndSendDailyReport();
            return ResponseEntity.ok(reportContent);
        } catch (Exception e) {
            log.error("Failed to generate daily report: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(AiReviewConstants.MSG_GENERATE_DAILY_REPORT_FAILED_PREFIX + e.getMessage());
        }
    }

}
