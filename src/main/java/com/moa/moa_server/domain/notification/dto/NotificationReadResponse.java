package com.moa.moa_server.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 읽음 처리 응답 DTO")
public record NotificationReadResponse(
    @Schema(description = "알림 ID", example = "123") Long notificationId) {}
