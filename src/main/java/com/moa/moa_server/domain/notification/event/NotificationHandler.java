package com.moa.moa_server.domain.notification.event;

import com.moa.moa_server.domain.notification.entity.Notification;
import com.moa.moa_server.domain.notification.repository.NotificationRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationHandler {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Async("notificationExecutor")
  @TransactionalEventListener
  public void handle(NotificationEvent event) {
    // 이벤트를 수신하여 테이블에 저장
    User user = userRepository.getReferenceById(event.userId());
    Notification notification =
        Notification.builder()
            .user(user)
            .type(event.type())
            .content(event.content())
            .redirectUrl(event.redirectUrl())
            .isRead(false)
            .build();
    notificationRepository.save(notification);
  }
}
