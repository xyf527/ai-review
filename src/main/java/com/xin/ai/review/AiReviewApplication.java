package com.xin.ai.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-05-13 11:22
 * @github https://github.com/xyf527
 * @copyright
 */

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class AiReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiReviewApplication.class, args);
    }

}
