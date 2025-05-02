package com.moa.moa_server.domain.vote.util;

import java.net.URL;
import java.time.LocalDateTime;

public class VoteValidator {

    public static void validateContent(String content) {
        if (content == null || content.isBlank() || content.length() > 255) {
            throw new RuntimeException("INVALID_CONTENT");
        }
    }

    public static void validateUrl(String url) {
        if (url == null) return; // null 허용
        try {
            new URL(url).toURI();
        } catch (Exception e) {
            throw new RuntimeException("INVALID_URL");
        }
    }

    public static void validateClosedAt(LocalDateTime closedAt) {
        if (closedAt == null || !closedAt.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("INVALID_TIME");
        }
    }
}
