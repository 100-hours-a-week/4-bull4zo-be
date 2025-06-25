package com.moa.moa_server.domain.notification.service;

import com.moa.moa_server.domain.global.cursor.CreatedAtCommentIdCursor;
import com.moa.moa_server.domain.global.cursor.CreatedAtNotificationIdCursor;
import com.moa.moa_server.domain.notification.dto.NotificationItem;
import com.moa.moa_server.domain.notification.dto.NotificationListResponse;
import com.moa.moa_server.domain.notification.entity.Notification;
import com.moa.moa_server.domain.notification.repository.NotificationRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private static final int DEFAULT_PAGE_SIZE = 20;

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public NotificationListResponse getNotifications(Long userId, String cursor, Integer size) {
    // 커서 파싱
    int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
    CreatedAtNotificationIdCursor parsedCursor =
        cursor != null ? CreatedAtNotificationIdCursor.parse(cursor) : null;

    // 유저 조회 및 유효성 검사
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    // 알림 목록 조회
    List<Notification> notifications =
        notificationRepository.findByUser(user, parsedCursor, pageSize);

    // 응답 구성
    boolean hasNext = notifications.size() > pageSize;
    if (hasNext) notifications = notifications.subList(0, pageSize);

    String nextCursor =
        hasNext
            ? new CreatedAtCommentIdCursor(
                    notifications.getLast().getCreatedAt(), notifications.getLast().getId())
                .encode()
            : null;

    List<NotificationItem> items = notifications.stream().map(NotificationItem::from).toList();

    // 응답
    return new NotificationListResponse(items, nextCursor, hasNext, notifications.size());
  }
}
