package com.xin.ai.review.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xin.ai.review.constant.AiReviewConstants;
import com.xin.ai.review.llm.interceptor.LLMMetricsInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-05-08 10:50
 * @github https://github.com/xyf527
 * @copyright
 */

@Slf4j
public abstract class BaseOpenAIClient implements LLMClient{

    protected final String apiKey;
    protected final String baseUrl;
    protected final String model;
    protected final String provider;
    protected final OkHttpClient httpClient;
    protected final ObjectMapper objectMapper;

    protected BaseOpenAIClient(String apiKey, String baseUrl, String model, String provider) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.provider = provider;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .addInterceptor(new LLMMetricsInterceptor())
                .connectTimeout(60, TimeUnit.SECONDS)
                // 大模型生成内容可能慢 尤其代码量大时
                // 长连接处理 LLM生成代码属于长耗时任务 默认10s会导致大量Timeout异常
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                // 忽略证书 生产环境谨慎
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }

    @Override
    public String completions(List<Map<String, String>> messages) {
        try {
            // 1.构建请求体JSON
            // 扩展性 使用ObjectNode而非DTO 是为了在对接不同的LLM厂商微笑参数差异时 无需频繁修改实体类
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put(AiReviewConstants.MODEL, model);

            ArrayNode messageNode = objectMapper.createArrayNode();
            for (Map<String, String> message : messages) {
                ObjectNode msgNode = objectMapper.createObjectNode();
                msgNode.put(AiReviewConstants.ROLE, message.get(AiReviewConstants.ROLE));
                msgNode.put(AiReviewConstants.CONTENT, message.get(AiReviewConstants.CONTENT));
                messageNode.add(msgNode);
            }
            requestBody.set(AiReviewConstants.MESSAGES, messageNode);
            // 低温度让输出更稳定 更少随机性 代码审查不需要创意
            // 模型稳定性 第采样温度可显著降低AI的幻觉 确保代码审查建议的确定性和逻辑严密性
            requestBody.put(AiReviewConstants.TEMPERATURE, 0.1);

            // 2.构建URL(GLM特殊处理)
            String url = baseUrl;

            if (AiReviewConstants.LLM_PROVIDER_GLM.equals(provider) && AiReviewConstants.LLM_MODEL_GLM_47_FLASH.equals(model)) {
                url = url + AiReviewConstants.GLM_CHAT_COMPLETIONS_PATH;
            } else {
                url = url + AiReviewConstants.LLM_CHAT_COMPLETIONS_PATH;
            }

            // 3.创建okhttp请求
            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(requestBody),
                    MediaType.parse(AiReviewConstants.MEDIA_TYPE_JSON)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(AiReviewConstants.HEADER_AUTHORIZATION, AiReviewConstants.AUTH_BEARER_PREFIX + apiKey)
                    .addHeader(AiReviewConstants.HEADER_CONTENT_TYPE, AiReviewConstants.APPLICATION_JSON)
                    .post(body)
                    .build();

            log.info("Sending request to LLM API: {}, model: {}", url, model);

            // 4.执行请求并解析响应
            // 资源泄露防护 利用try-with-resources自动调用response.close() 防止OkHttp连接池在家庭服务器上因Socket泄漏而崩溃
            try (Response response = httpClient.newCall(request).execute()) {

                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    log.error("LLM API call failed: {} - {}", response.code(), responseBody);
                    throw new RuntimeException("LLM API call failed: " + response.code() + "-" + responseBody);
                }

                JsonNode responseJson = objectMapper.readTree(responseBody);
                String content = responseJson
                        .path(AiReviewConstants.CHOICES)
                        .path(0)
                        .path(AiReviewConstants.MESSAGES)
                        .path(AiReviewConstants.CONTENT)
                        .asText();

                log.info("LLM API call succeed, response length: {}", content.length());
                return content;
            }
        } catch (IOException e) {
            log.error("LLM API call IO error: {}", e.getMessage(), e);
            throw new RuntimeException("LLLM API call failed: " + e.getMessage(), e);
        }
    }
}
