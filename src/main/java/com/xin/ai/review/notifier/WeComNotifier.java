package com.xin.ai.review.notifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xin.ai.review.constant.AiReviewConstants;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author xyf527
 * @version 1.0
 * @description 企业微信通知器
 * @date 2026-05-10 14:15
 * @github https://github.com/xyf527
 * @copyright
 */

@Slf4j
@Component
public class WeComNotifier {

    @Value("${notification.wecom.enabled:false}")
    private boolean enabled;

    @Value("${notification.wecom.webhook-url:}")
    private String defaultWebhookUrl;

    // text类型最大2048字节，markdown类型最大4096字节，超长内容自动分割发送
    private static final int MAX_TEXT_BYTES = 2048;
    private static final int MAX_MARKDOWN_BYTES = 4096;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WeComNotifier() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private String getWebhookUrl(String projectName, String urlSlug) {
        if (projectName != null && !projectName.isBlank()) {
            String envKey = AiReviewConstants.ENV_WECOM_WEBHOOK_URL_PREFIX + projectName.toUpperCase().replace("-", "_");
            String url = System.getenv(envKey);
            if (url != null && !url.isBlank()) {
                return url;
            }
        }

        if (urlSlug != null && !urlSlug.isBlank()) {
            String envKey = AiReviewConstants.ENV_WECOM_WEBHOOK_URL_PREFIX + urlSlug.toUpperCase();
            String url = System.getenv(envKey);
            if (url != null && !url.isBlank()) {
                return url;
            }
        }

        return defaultWebhookUrl;
    }


    public void sendMarkdown(String content) {
        sendMarkdown(content, null, null, null);
    }

    public void sendMarkdown(String content, String title, String projectName, String urlSlug) {
        if (!enabled) {
            log.debug("企业微信推送未启用");
            return;
        }

        String postUrl = getWebhookUrl(projectName, urlSlug);
        if (postUrl == null || postUrl.isBlank()) {
            log.debug("未配置企业微信webhook URL");
            return;
        }

        String formatted = formatMarkdownContent(content, title);
        byte[] bytes = formatted.getBytes(StandardCharsets.UTF_8);
        int contentLength = bytes.length;

        if (contentLength <= MAX_MARKDOWN_BYTES) {
            sendRequest(postUrl, buildMarkdownMessage(formatted));
        } else {
            log.warn("企业微信消息内容超过{}字节限制，将分割发送。总长度: {}字节", MAX_MARKDOWN_BYTES, contentLength);
            List<String> chunks = splitContent(formatted, MAX_MARKDOWN_BYTES);
            for (int i = 0; i < chunks.size(); i++) {
                String chunkTitle = title != null ? title + " (第" + (i + 1) + "/" + chunks.size() + "部分)"
                        : "消息 (第" + (i + 1) + "/" + chunks.size() + "部分)";
                sendRequest(postUrl, buildMarkdownMessage(formatMarkdownContent(chunks.get(i), chunkTitle)));
            }
        }
    }

    public void sendText(String content, boolean isAtAll, String projectName, String urlSlug) {
        if (!enabled) {
            return;
        }

        String postUrl = getWebhookUrl(projectName, urlSlug);
        if (postUrl == null || postUrl.isBlank()) {
            return;
        }

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= MAX_TEXT_BYTES) {
            sendRequest(postUrl, buildTextMessage(content, isAtAll));
        } else {
            List<String> chunks = splitContent(content, MAX_TEXT_BYTES);
            for (String chunk : chunks) {
                sendRequest(postUrl, buildTextMessage(chunk, isAtAll));
            }
        }

    }

    // 按照最大字节数分个内容 尽量在换行出分割
    private List<String> splitContent(String content, int maxBytes) {
        List<String> chunks = new ArrayList<>();
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        int startPos = 0;
        int totalLength = contentBytes.length;

        while (startPos < totalLength) {
            int endPos = startPos + maxBytes;
            if (endPos >= totalLength) {
               chunks.add(new String(contentBytes, startPos, totalLength - startPos, StandardCharsets.UTF_8));
               break;
            }

            // 尝试在换行处分隔
            while (endPos > startPos) {
                if (contentBytes[endPos - 1] == '\n') {
                    break;
                }
                endPos--;
            }
            if (endPos == startPos) {
                // 没有找到换行 强制分割
                endPos = startPos + maxBytes;
            }
            chunks.add(new String(contentBytes, startPos, endPos - startPos, StandardCharsets.UTF_8));
            startPos = endPos;
        }
        return chunks;
    }

    private String formatMarkdownContent(String content, String title) {
        String formatted = (title != null && !title.isBlank()) ? "## " + title + "\n\n" : "";

        content = Pattern.compile("#{5,}\\s").matcher(content).replaceAll("#### ");

        content = Pattern.compile("<[^>]+>").matcher(content).replaceAll("");
        return formatted + content;
    }

    private ObjectNode buildMarkdownMessage(String content) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("msgtype", "markdown");
        ObjectNode markdown = objectMapper.createObjectNode();
        markdown.put("content", content);
        payload.set("markdown", markdown);
        return payload;
    }

    private ObjectNode buildTextMessage(String content, boolean isAtAll) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("msgtype", "text");
        ObjectNode text = objectMapper.createObjectNode();
        text.put("content", content);
        ArrayNode mentionedList = objectMapper.createArrayNode();
        if (isAtAll) {
            mentionedList.add("@all");
        }
        text.set("text", text);
        payload.set("text", mentionedList);
        return payload;
    }

    private void sendRequest(String postUrl, ObjectNode payLoad) {
        try {
            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(payLoad),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(postUrl)
                    .addHeader(AiReviewConstants.HEADER_CONTENT_TYPE, "application/json")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String rspBody = response.body() != null ? response.body().string() : "{}";
                    JsonNode respJson = objectMapper.readTree(rspBody);
                    if (respJson.path("errcode").asInt() == 0) {
                        log.info("企业微信消息发送成功 webhook_url: {}", postUrl);
                    } else {
                        log.error("企业微信消息发送失败 webhook_url: {}, errmsg: {}", postUrl, respJson);
                    }
                } else {
                    log.error("企业微信消息发送失败 HTTP状态码: {}", response.code());
                }
            }
        }catch (Exception e) {
            log.error("企业微信消息发送失败 {}", e.getMessage(), e);
        }
    }



}
