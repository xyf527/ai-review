package com.xin.ai.review.llm.deepseek;

import com.xin.ai.review.llm.BaseOpenAIClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xyf527
 * @version 1.0
 * @description 对接DeepSeek
 * @date 2026-05-08 11:52
 * @github https://github.com/xyf527
 * @copyright
 */

@Slf4j
public class DeepSeekClient extends BaseOpenAIClient {

    public DeepSeekClient(String apiKey, String baseUrl, String provider, String model) {
        super(apiKey, baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl, model, provider);
        log.info("DeepSeek client initialized, model: {}, baseUrl: {}", model, baseUrl);
    }

}
