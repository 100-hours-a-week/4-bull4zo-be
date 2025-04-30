package com.moa.moa_server.domain.ai.dto;

public record ModerationResponse(
        String status,
        String message,
        Object data
) {}
