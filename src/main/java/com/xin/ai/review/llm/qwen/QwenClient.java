package com.xin.ai.review.llm.qwen;

import com.xin.ai.review.llm.BaseOpenAIClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xyf527
 * @version 1.0
 * @description 对接通义千问
 * @date 2026-05-08 11:54
 * @github https://github.com/xyf527
 * @copyright
 */

@Slf4j
public class QwenClient extends BaseOpenAIClient {

    public QwenClient(String apiKey, String baseUrl, String provider, String model) {
        super(apiKey, baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl, provider, model);
        log.info("Qwen client initialized, model: {}, baseUrl: {}", model, baseUrl);
    }

}
