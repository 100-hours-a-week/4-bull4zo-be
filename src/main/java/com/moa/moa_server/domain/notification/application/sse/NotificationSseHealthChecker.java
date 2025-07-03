package com.moa.moa_server.domain.notification.application.sse;

import com.moa.moa_server.domain.notification.repository.NotificationEmitterRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 주기적으로 서버에 존재하는 모든 SseEmitter에 ping 이벤트를 전송하여 연결 상태를 점검하는 컴포넌트. */
@Component
@RequiredArgsConstructor
public class NotificationSseHealthChecker {

  private final NotificationEmitterRepository emitterRepository;

  @Scheduled(fixedRateString = "#{@sseProperties.pingInterval}")
  public void sendPing() {
    emitterRepository
        .findAllEmitters()
        .forEach(
            (id, emitter) -> {
              try {
                SseEmitter.SseEventBuilder event = SseEmitter.event().name("dummy").data("ping");
                emitter.send(event);
              } catch (IOException e) {
                emitterRepository.deleteById(id);
              }
            });
  }
}
