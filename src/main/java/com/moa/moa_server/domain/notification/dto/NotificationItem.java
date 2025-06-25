package com.moa.moa_server.domain.notification.dto;

import com.moa.moa_server.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 항목")
public record NotificationItem(
    @Schema(description = "알림 ID", example = "123") Long notificationId,
    @Schema(description = "알림 유형", example = "MY_VOTE_COMMENT") String type,
    @Schema(description = "알림 내용", example = "댓글 내용") String content,
    @Schema(description = "읽음 여부 (0: 읽지 않음, 1: 읽음)", example = "1") int read,
    @Schema(description = "알림 클릭 시 이동할 경로", example = "https://domain.com/votes/42")
        String redirectUrl,
    @Schema(description = "알림 생성 시각", example = "2025-04-25T13:30:00") String createdAt) {
  public static NotificationItem from(Notification notification) {
    return new NotificationItem(
        notification.getId(),
        notification.getType().name(),
        notification.getContent(),
        notification.isRead() ? 1 : 0,
        notification.getRedirectUrl(),
        notification.getCreatedAt().toString());
  }
}
