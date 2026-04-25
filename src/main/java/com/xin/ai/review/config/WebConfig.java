package com.xin.ai.review.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-04-22 20:48
 * @github https://github.com/xyf527
 * @copyright
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把classpath:/static/目录映射到/** 也就是访问http://localhost:5002/xxx.html会去src/main/resources/static/ 下面找对应文件
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

}
