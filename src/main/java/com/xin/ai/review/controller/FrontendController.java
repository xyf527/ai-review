package com.xin.ai.review.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-05-14 10:18
 * @github https://github.com/xyf527
 * @copyright
 */

@RestController
public class FrontendController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("static/index.html");
            if (is == null) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("<h1>Error loading fronted</h1>");
        }
    }

}
