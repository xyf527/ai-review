package com.xin.ai.review.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xin.ai.review.constant.AiReviewConstants;
import com.xin.ai.review.entity.MrReviewLog;
import com.xin.ai.review.entity.PushReviewLog;
import com.xin.ai.review.messaging.NotificationService;
import com.xin.ai.review.service.CodeReviewService;
import com.xin.ai.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-05-12 10:09
 * @github https://github.com/xyf527
 * @copyright
 */

@Slf4j
@Component
public class GitLabWebhookHandler {

    @Autowired
    private CodeReviewService codeReviewService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private NotificationService notificationService;

    @Value("${review.supported-extensions:.java,.py,.php,.yml,.vue,.go,.c,.cpp,.h,.js,.css,.md,.sql,.ts,.tsx,.jsx}")
    private String supportedExtensions;

    @Value("${review.push-enabled:false}")
    private boolean pushReviewEnabled;

    @Value("${review.only-protected-branches:false}")
    private boolean onlyProtectedBranches;

    @Value("${review.only-branch-name:}")
    private String onlyBranchName;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GitLabWebhookHandler() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }

    @Async
    public void handleMergeRequest(JsonNode payload, String gitlabToken, String gitlabUrl) {
        try {
            log.info("Processing GitLab MR webhook");

            JsonNode objectAttrs = payload.path(AiReviewConstants.OBJECT_ATTRIBUTES);
            String action = objectAttrs.path(AiReviewConstants.ACTION).asText();

            if (!List.of(AiReviewConstants.ACTION_OPEN, AiReviewConstants.ACTION_UPDATE, AiReviewConstants.ACTION_REOPEN, AiReviewConstants.ACTION_MERGE).contains(action)) {
                log.info("GitLab MR action '{}' ignored", action);
                return;
            }

            String projectName = payload.path(AiReviewConstants.PROJECT).path(AiReviewConstants.NAME).asText();
            String author = payload.path(AiReviewConstants.USER).path(AiReviewConstants.NAME).asText();
            if (author == null || author.isBlank()) {
                author = payload.path(AiReviewConstants.USER).path(AiReviewConstants.USERNAME).asText();
            }
            String sourceBranch = objectAttrs.path(AiReviewConstants.SOURCE_BRANCH).asText();
            String targetBranch = objectAttrs.path(AiReviewConstants.TARGET_BRANCH).asText();
            long updatedAt = System.currentTimeMillis() / 1000;
            String mrUrl = normalizeUrl(objectAttrs.path(AiReviewConstants.URL).asText(), gitlabUrl);
            int projectId = payload.path(AiReviewConstants.PROJECT).path(AiReviewConstants.ID).asInt();
            int mrIid = objectAttrs.path(AiReviewConstants.IID).asInt();

            // 过滤目标分值
            if (onlyBranchName != null && !onlyBranchName.isBlank()) {
                List<String> allowedBranches = Arrays.stream(onlyBranchName.split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                if (!allowedBranches.isEmpty() && !allowedBranches.contains(targetBranch)) {
                    log.info("GitLab MR target branch '{}' not in allowed branches {}, skipping", targetBranch, allowedBranches);
                    return;
                }
            }

            // 过滤受保护的分支
            if (onlyProtectedBranches && !isTargetBranchProtected(gitlabUrl, gitlabToken, projectId, targetBranch)) {
                log.info("GitLab MR target branch '{}' not in protected branches, skipping", targetBranch);
                return;
            }

            // 获取MR的代码变更
            String changesText = fetchMrChanges(gitlabUrl, gitlabToken, projectId, mrIid);
            if (changesText == null || changesText.isBlank()) {
                log.info("No supported changes found in MR");
                return;
            }

            // 获取提交信息
            String commitsText = fetchMrCommits(gitlabUrl, gitlabToken, projectId, mrIid);

            // 执行AI代码审查
            log.info("Starting AI review for GitLab MR: {}/{}", projectName, mrIid);
            String reviewResult = codeReviewService.reviewAndStripCode(changesText, commitsText);
            int score = codeReviewService.parseReviewScore(reviewResult);

            // 在MR上发布评论
            postMrComment(gitlabUrl, gitlabToken, projectId, mrIid, reviewResult);

            // 计算代码行数
            int[] additions = {0};
            int[] deletions = {0};
            calculateChanges(changesText, additions, deletions);

            // 保存到数据库
            MrReviewLog log1 = MrReviewLog.builder()
                    .projectName(projectName)
                    .author(author)
                    .sourceBranch(sourceBranch)
                    .targetBranch(targetBranch)
                    .updatedAt(updatedAt)
                    .commitMessages(commitsText)
                    .score(score)
                    .url(mrUrl)
                    .reviewResult(reviewResult)
                    .additions(additions[0])
                    .deletions(deletions[0])
                    .build();
            reviewService.insertMrReviewLog(log1);

            // 发送通知
            notificationService.sendReviewNotification(
                    projectName, author, AiReviewConstants.REVIEW_TYPE_MERGE_REQUEST, sourceBranch, targetBranch, reviewResult, score, mrUrl,
                    slugifyUrl(gitlabUrl), null);

        } catch (Exception e) {
            log.error("Error handling GitLab MR webhook: {}", e.getMessage(), e);
            notificationService.sendErrorNotification(AiReviewConstants.MSG_ERROR_SERVICE_PREFIX + e.getMessage());
        }
    }

    @Async
    public void handlePush(JsonNode payload, String gitlabToken, String gitlabUrl) {
        try {
            log.info("Processing GitLab Push webhook");
            String projectName = payload.path(AiReviewConstants.PROJECT).path(AiReviewConstants.NAME).asText();
            String author = payload.path(AiReviewConstants.USER_NAME).asText();
            String branch = payload.path(AiReviewConstants.REF).asText().replace(AiReviewConstants.GIT_REFS_HEADS_PREFIX, "");
            long updatedAt = System.currentTimeMillis() / 1000;
            int projectId = payload.path(AiReviewConstants.PROJECT_ID).asInt();
            JsonNode commits = payload.path(AiReviewConstants.COMMITS);

            // 提取push连接 优先使用第一个commit的URL fallback到project.web_url
            String pushUrl = null;
            if (commits.isArray() && commits.size() > 0) {
                pushUrl = commits.get(0).path(AiReviewConstants.URL).asText(null);
            }
            if (pushUrl == null || pushUrl.isBlank()) {
                pushUrl = payload.path(AiReviewConstants.PROJECT).path(AiReviewConstants.WEB_URL).asText(null);
            }
            pushUrl = normalizeUrl(pushUrl, gitlabUrl);

            // 获取提交信息
            StringBuilder commitsBuilder = new StringBuilder();
            for (JsonNode commit : commits) {
                commitsBuilder.append(commit.path(AiReviewConstants.MESSAGE).asText()).append("\n");
            }
            String commitsText = commitsBuilder.toString();

            String reviewResult = null;
            int score = 0;
            int[] additions = {0};
            int[] deletions = {0};

            if (pushReviewEnabled) {
                // 获取代码变更
                StringBuilder changesBuilder = new StringBuilder();
                for (JsonNode commit : commits) {
                    String commitId = commit.path(AiReviewConstants.ID).asText();
                    String commitChanges = fetchCommitChanges(gitlabUrl, gitlabToken, projectId, commitId);
                    if (commitChanges != null) {
                        changesBuilder.append(commitChanges).append("\n");
                    }
                }

                String changesText = changesBuilder.toString();
                if (changesText.isBlank()) {
                    log.info("No supported changes found in push");
                    reviewResult = AiReviewConstants.MSG_NO_WATCHED_FILES_CHANGED;
                } else {
                    // 执行AI代码审查
                    log.info("Starting AI review for GitLab push: {}/{}", projectName, branch);
                    reviewResult = codeReviewService.reviewAndStripCode(changesText, commitsText);
                    score = codeReviewService.parseReviewScore(reviewResult);

                    // 发布commit评论
                    for (JsonNode commit : commits) {
                        String commitId = commit.path("id").asText();
                        postCommitComment(gitlabUrl, gitlabToken, projectId, commitId, reviewResult);
                        break; // 只发布第一个commit的评论
                    }

                    calculateChanges(changesText, additions, deletions);
                }
            }
            // 保存到数据库
            PushReviewLog pushLog = PushReviewLog.builder()
                    .projectName(projectName)
                    .author(author)
                    .branch(branch)
                    .updatedAt(updatedAt)
                    .commitMessages(commitsText)
                    .score(score)
                    .url(pushUrl)
                    .reviewResult(reviewResult)
                    .additions(additions[0])
                    .deletions(deletions[0])
                    .build();
            reviewService.insertPushReviewLog(pushLog);
            // 发送通知 仅当有审查结果时
            if (reviewResult != null && !reviewResult.isBlank()) {
                notificationService.sendReviewNotification(
                        projectName, author, AiReviewConstants.REVIEW_TYPE_PUSH, branch, null, reviewResult, score, null,
                        slugifyUrl(gitlabUrl), null);
            }
        } catch (Exception e) {
            log.error("Error handling GitLab push webhook: {}", e.getMessage(), e);
            notificationService.sendErrorNotification(AiReviewConstants.MSG_ERROR_SERVICE_PREFIX + e.getMessage());
        }
    }

    private String fetchCommitChanges(String gitlabUrl, String token, int projectId, String commitId) throws IOException {
        String url = gitlabUrl.replaceAll("/+$", "") + AiReviewConstants.GITLAB_API_PROJECTS_PATH + projectId + "/repository/commits/" + commitId + "/diff";
        Request request = new Request.Builder()
                .url(url)
                .addHeader(AiReviewConstants.HEADER_GITLAB_PRIVATE_TOKEN, token)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            String body = response.body() != null ? response.body().string() : "";
            JsonNode diffs = objectMapper.readTree(body);
            return extractDiffs(diffs);
        }
    }

    private String slugifyUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String s = url.replaceAll("^https?://", "");
        s = s.replaceAll("[^a-zA-Z0-9]", "_");
        s = s.replaceAll("_+$", "");
        return s;
    }

    private void calculateChanges(String diff, int[] additions, int[] deletions) {
        for (String line : diff.split("\n")) {
            if (line.startsWith("+") && !line.startsWith("+++")) {
                additions[0]++;
            } else if (line.startsWith("-") && !line.startsWith("---")) {
                deletions[0]++;
            }
        }
    }

    private void postMrComment(String gitlabUrl, String token, int projectId, int mrIid, String comment) {
        try {
            String url = gitlabUrl.replaceAll("/+$", "") + AiReviewConstants.GITLAB_API_PROJECTS_PATH + projectId + "/merge_requests/" + mrIid + "/notes";
            ObjectNode body = objectMapper.createObjectNode();
            body.put(AiReviewConstants.BODY, comment);

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_GITLAB_PRIVATE_TOKEN, token)
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                log.info("Posted MR comment: {}", response.code());
            }
        } catch (Exception e) {
            log.error("Failed to post MR comment: {}", e.getMessage(), e);
        }
    }

    private void postCommitComment(String gitlabUrl, String token, int projectId, String commitId, String comment) {
        try {
            String url = gitlabUrl.replaceAll("/+$", "") + AiReviewConstants.GITLAB_API_PROJECTS_PATH + projectId + "/repository/commits/" + commitId + "/comments";
            ObjectNode body = objectMapper.createObjectNode();
            body.put(AiReviewConstants.NOTE, comment);

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_GITLAB_PRIVATE_TOKEN, token)
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                log.info("Posted commit comment: {}", response.code());
            }
        } catch (Exception e) {
            log.error("Failed to post commit comment: {}", e.getMessage(), e);
        }
    }

    private String fetchMrCommits(String gitlabUrl, String token, int projectId, int mrIid) throws IOException {
        String url = gitlabUrl.replaceAll("/+$", "") + AiReviewConstants.GITLAB_API_PROJECTS_PATH + projectId + "/merge_requests/" + mrIid + "/commits";
        Request request = new Request.Builder()
                .url(url)
                .addHeader(AiReviewConstants.HEADER_GITLAB_PRIVATE_TOKEN, token)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "";
            }
            String body = response.body() != null ? response.body().string() : "";
            JsonNode commits = objectMapper.readTree(body);
            StringBuilder sb = new StringBuilder();
            for (JsonNode commit : commits) {
                sb.append(commit.path("message").asText()).append("\n");
            }
            return sb.toString();
        }
    }

    private String fetchMrChanges(String gitlabUrl, String token, int projectId, int mrIid) throws IOException {
        String url = gitlabUrl.replaceAll("/+$", "") + AiReviewConstants.GITLAB_API_PROJECTS_PATH + projectId + "/merge_requests/" + mrIid + "/changes";
        Request request = new Request.Builder()
                .url(url)
                .addHeader(AiReviewConstants.HEADER_GITLAB_PRIVATE_TOKEN, token)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Failed to fetch MR changes: {}", response.code());
                return null;
            }
            String body = response.body() != null ? response.body().string() : "";
            JsonNode json = objectMapper.readTree(body);
            return extractDiffs(json.path(AiReviewConstants.CHANGES));
        }
    }

    private String extractDiffs(JsonNode changesNode) {
        if (changesNode == null || !changesNode.isArray()) {
            return "";
        }
        Set<String> supported = new HashSet<>(Arrays.asList(supportedExtensions.split(",")));
        StringBuilder sb = new StringBuilder();
        for (JsonNode change : changesNode) {
            String newPath = change.path(AiReviewConstants.NEW_PATH).asText();
            String ext = getExtension(newPath);
            if (!supported.contains(ext)) {
                continue;
            }
            String diff = change.path("diff").asText();
            if (!diff.isBlank()) {
                sb.append("diff --git a/").append(newPath).append(" b/").append(newPath).append("\n");
                sb.append("--- a/").append(newPath).append("\n");
                sb.append("+++ b/").append(newPath).append("\n");
                sb.append(diff).append("\n");
            }
        }
        return sb.toString();
    }

    private String getExtension(String filePath) {
        if (filePath == null) {
            return "";
        }
        int lastDot = filePath.lastIndexOf(".");
        if (lastDot < 0) {
            return "";
        }
        return filePath.substring(lastDot);
    }

    private boolean isTargetBranchProtected(String gitlabUrl, String token, int projectId, String targetBranch) {
        try {
            String url = gitlabUrl.replaceAll("/+$", "") + AiReviewConstants.GITLAB_API_PROJECTS_PATH + projectId + "/protected_branches";
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_GITLAB_PRIVATE_TOKEN, token)
                    .get()
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return false;
                }
                String body = response.body() != null ? response.body().string() : "[]";
                JsonNode branches = objectMapper.readTree(body);
                for (JsonNode branch : branches) {
                    String name = branch.path(AiReviewConstants.NAME).asText();
                    if (fnmatch(targetBranch, name)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check GitLab protected branches: {}", e.getMessage());
        }
        return false;
    }

    private boolean fnmatch(String name, String pattern) {
        String regex = pattern.replace(".", "\\.").replace("*", ".*").replace("?", ".");
        return name.matches(regex);
    }

    private String normalizeUrl(String payloadUrl, String platformUrl) {
        if (payloadUrl == null || platformUrl == null || payloadUrl.isBlank() || platformUrl.isBlank()) {
            return payloadUrl;
        }

        try {
            URI payloadUri = new URI(payloadUrl);
            URI platformUri = new URI(platformUrl.replace("/+$", ""));
            if (payloadUri.getHost() != null
                    && payloadUri.getHost().equals(platformUri.getHost())
                    && platformUri.getPort() > 0
                    && payloadUri.getPort() == -1
            ) {
                return new URI(
                        payloadUri.getScheme(), payloadUri.getUserInfo(),
                        payloadUri.getHost(), platformUri.getPort(),
                        payloadUri.getPath(), payloadUri.getQuery(),
                        payloadUri.getFragment()).toString();
            }
        } catch (Exception e) {

        }
        return payloadUrl;
    }

}
