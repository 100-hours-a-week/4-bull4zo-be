package com.moa.moa_server.domain.notification.repository;

import com.moa.moa_server.domain.global.cursor.CreatedAtNotificationIdCursor;
import com.moa.moa_server.domain.notification.entity.Notification;
import com.moa.moa_server.domain.user.entity.User;
import jakarta.annotation.Nullable;
import java.util.List;

public interface NotificationRepositoryCustom {
  List<Notification> findByUser(
      User user, @Nullable CreatedAtNotificationIdCursor cursor, int size);
}
