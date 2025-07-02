package com.moa.moa_server.domain.notification.application.event;

import com.moa.moa_server.domain.notification.application.sse.NotificationSseSender;
import com.moa.moa_server.domain.notification.entity.Notification;
import com.moa.moa_server.domain.notification.repository.NotificationRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/** 비동기 이벤트 수신 및 저장 */
@Component
@RequiredArgsConstructor
public class NotificationHandler {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  private final NotificationSseSender notificationSseSender;

  @Async("notificationExecutor")
  @TransactionalEventListener
  public void handle(NotificationEvent event) {
    // 수신자 ID로 유저 목록 일괄 조회 (Map으로 변환)
    List<User> users = userRepository.findAllById(event.userIds());
    Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));

    // 알림 객체들 생성
    List<Notification> notifications =
        event.userIds().stream()
            .filter(userMap::containsKey) // 유저 null 방어
            .map(
                userId ->
                    Notification.builder()
                        .user(userMap.get(userId))
                        .type(event.type())
                        .content(event.content())
                        .redirectUrl(event.redirectUrl())
                        .isRead(false)
                        .build())
            .toList();

    // 벌크 저장
    notificationRepository.saveAll(notifications);

    // SSE 전송
    notifications.forEach(notificationSseSender::send);
  }
}
