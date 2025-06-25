package com.moa.moa_server.domain.notification.repository.impl;

import com.moa.moa_server.domain.global.cursor.CreatedAtNotificationIdCursor;
import com.moa.moa_server.domain.notification.entity.Notification;
import com.moa.moa_server.domain.notification.entity.QNotification;
import com.moa.moa_server.domain.notification.repository.NotificationRepositoryCustom;
import com.moa.moa_server.domain.user.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Notification> findByUser(
      User user, @Nullable CreatedAtNotificationIdCursor cursor, int size) {
    QNotification notification = QNotification.notification;

    BooleanBuilder builder = new BooleanBuilder().and(notification.user.id.eq(user.getId()));

    if (cursor != null) {
      builder.and(
          notification
              .createdAt
              .lt(cursor.createdAt())
              .or(
                  notification
                      .createdAt
                      .eq(cursor.createdAt())
                      .and(notification.id.lt(cursor.notificationId()))));
    }

    return queryFactory
        .selectFrom(notification)
        .where(builder)
        .orderBy(notification.createdAt.desc(), notification.id.desc())
        .limit(size + 1)
        .fetch();
  }
}
