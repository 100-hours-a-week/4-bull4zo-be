package com.moa.moa_server.domain.notification.application.event;

import com.moa.moa_server.domain.notification.entity.NotificationType;
import java.util.List;

/** 알림 전송용 DTO */
public record NotificationEvent(
    List<Long> userIds, NotificationType type, String content, String redirectUrl) {

  public static NotificationEvent forSingleUser(
      Long userId, NotificationType type, String content, String redirectUrl) {
    return new NotificationEvent(List.of(userId), type, content, redirectUrl);
  }

  public static NotificationEvent forMultipleUsers(
      List<Long> userIds, NotificationType type, String content, String redirectUrl) {
    return new NotificationEvent(userIds, type, content, redirectUrl);
  }
}
