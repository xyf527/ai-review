package com.xin.ai.review.service.impl;

import com.xin.ai.review.entity.MrReviewLog;
import com.xin.ai.review.entity.PushReviewLog;
import com.xin.ai.review.repository.MrReviewLogRepository;
import com.xin.ai.review.repository.PushReviewLogRepository;
import com.xin.ai.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-05-09 12:18
 * @github https://github.com/xyf527
 * @copyright
 */

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private MrReviewLogRepository mrReviewLogRepository;

    @Autowired
    private PushReviewLogRepository pushReviewLogRepository;

    @Override
    public void insertMrReviewLog(MrReviewLog entity) {
        try {
            mrReviewLogRepository.save(entity);
            log.info("MR review log saves: {} by: {}", entity.getProjectName(), entity.getAuthor());
        } catch (Exception e) {
            log.error("Error saving MR review log: {}", e.getMessage(), e);
        }
    }

    @Override
    public void insertPushReviewLog(PushReviewLog entity) {
        try {
            pushReviewLogRepository.save(entity);
            log.info("Push review log saved: {} by {}", entity.getProjectName(), entity.getAuthor());
        } catch (Exception e) {
            log.error("Error saving push review log: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<MrReviewLog> getMrReviewLogs(List<String> authors, List<String> projectNames, Long updatedAtGte, Long updatedAtLte) {
        return mrReviewLogRepository.findByFilters(
                authors != null && authors.isEmpty() ? null : authors,
                projectNames != null && projectNames.isEmpty() ? null : projectNames,
                updatedAtGte, updatedAtLte
        );
    }

    @Override
    public List<PushReviewLog> getPushReviewLogs(List<String> authors, List<String> projectNames, Long updatedAtGte, Long updatedAtLte) {
        return pushReviewLogRepository.findByFilters(
                authors != null && authors.isEmpty() ? null : authors,
                projectNames != null && projectNames.isEmpty() ? null : projectNames,
                updatedAtGte, updatedAtLte
        );
    }

    @Override
    public List<String> getMrDistinctAuthors() {
        return mrReviewLogRepository.findDistinctAuthors();
    }

    @Override
    public List<String> getMrDistinctProjectNames() {
        return mrReviewLogRepository.findDistinctProjectNames();
    }

    @Override
    public List<String> getPushDistinctAuthors() {
        return pushReviewLogRepository.findDistinctAuthors();
    }

    @Override
    public List<String> getPushDistinctProjectNames() {
        return pushReviewLogRepository.findDistinctProjectNames();
    }
}
