package com.moa.moa_server.domain.notification.event;

import com.moa.moa_server.domain.notification.entity.NotificationType;

public record NotificationEvent(
    Long userId, NotificationType type, String content, String redirectUrl) {}
