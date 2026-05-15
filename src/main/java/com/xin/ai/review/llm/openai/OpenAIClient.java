package com.xin.ai.review.llm.openai;

import com.xin.ai.review.llm.BaseOpenAIClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xyf527
 * @version 1.0
 * @description 对接OpenAI
 * @date 2026-05-08 11:50
 * @github https://github.com/xyf527
 * @copyright
 */

@Slf4j
public class OpenAIClient extends BaseOpenAIClient {

    public OpenAIClient(String apiKey, String baseUrl, String provider, String model) {
        super(apiKey, baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl, provider, model);
        log.info("OpenAI client initialized, model: {}, baseUrl: {}", model, baseUrl);
    }

}
