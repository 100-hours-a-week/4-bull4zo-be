package com.moa.moa_server.domain.notification.application.sse;

import static com.moa.moa_server.domain.global.util.JsonUtil.toJson;

import com.moa.moa_server.domain.notification.dto.NotificationItem;
import com.moa.moa_server.domain.notification.entity.Notification;
import com.moa.moa_server.domain.notification.repository.NotificationEmitterRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 알림(Notification)을 특정 사용자에게 연결된 모든 SSE emitter에 브로드캐스팅하는 컴포넌트. */
@Component
@RequiredArgsConstructor
public class NotificationSseBroadcaster {

  private final NotificationEmitterRepository emitterRepository;

  public void send(Notification notification) {
    String payload = toJson(NotificationItem.from(notification));
    SseEmitter.SseEventBuilder event =
        SseEmitter.event()
            .id(String.valueOf(notification.getId()))
            .name("notification")
            .data(payload);

    emitterRepository
        .findAllByUserId(notification.getUser().getId())
        .forEach(
            (id, emitter) -> {
              try {
                emitter.send(event);
              } catch (IOException e) {
                emitterRepository.deleteById(id); // 연결이 끊긴 emitter는 삭제
              }
            });
  }
}
