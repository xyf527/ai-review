package com.xin.ai.review.llm.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Slf4j
public class LLMMetricsInterceptor implements Interceptor {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();

        // 1. 记录开始时间
        long startNanos = System.nanoTime();

        // 2. 执行请求
        Response response = chain.proceed(request);

        // 3. 计算耗时（毫秒）
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;

        if (response.isSuccessful() && response.body() != null) {
            // 关键点：使用 peekBody 复制一份流，否则 response.body().string() 会导致下游业务代码拿不到数据
            // 设定一个足够大的限制（比如 1MB），防止大回复撑爆内存
            ResponseBody responseBody = response.peekBody(1024 * 1024);
            String content = responseBody.string();

            try {
                JsonNode root = objectMapper.readTree(content);
                // 提取 OpenAI 标准协议中的 usage 字段
                JsonNode usage = root.path("usage");
                if (!usage.isMissingNode()) {
                    int promptTokens = usage.path("prompt_tokens").asInt();
                    int completionTokens = usage.path("completion_tokens").asInt();
                    int totalTokens = usage.path("total_tokens").asInt();

                    log.info("===> LLM 调用统计 | 耗时: {}ms | Prompt: {} | Completion: {} | Total: {} <===",
                            durationMs, promptTokens, completionTokens, totalTokens);
                }
            } catch (Exception e) {
                log.warn("无法解析 LLM Token 使用情况: {}", e.getMessage());
            }
        }

        return response;
    }
}