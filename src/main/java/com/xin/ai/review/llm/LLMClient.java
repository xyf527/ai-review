package com.xin.ai.review.llm;

import java.util.List;
import java.util.Map;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-05-08 10:49
 * @github https://github.com/xyf527
 * @copyright
 */

public interface LLMClient {

    String completions(List<Map<String, String>> message);

}
