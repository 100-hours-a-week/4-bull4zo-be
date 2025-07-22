package com.moa.moa_server.domain.notification.application.sse;

import com.moa.moa_server.domain.notification.dto.NotificationItem;
import com.moa.moa_server.domain.notification.entity.Notification;
import com.moa.moa_server.domain.notification.repository.NotificationEmitterRepository;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** SSE 연결 후 초기 이벤트 전송(dummy, 유실 이벤트)을 담당하는 헬퍼 클래스. */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSseConnectionHelper {

  private final NotificationEmitterRepository emitterRepository;

  public void sendDummyEvent(SseEmitter emitter) {
    try {
      SseEmitter.SseEventBuilder event =
          SseEmitter.event()
              .name("dummy")
              .data("ping")
              .reconnectTime(10000); // 클라이언트 재연결 간격 설정 (최초 1회)
      emitter.send(event);
    } catch (IOException e) {
      log.warn("초기 SSE 이벤트 전송 실패. emitter는 유지", e); // 초기 전송 실패는 일시적일 수 있으므로 무시하고 연결 유지
    }
  }

  public void sendLostEvents(Long userId, String lastEventId, SseEmitter emitter) {
    // lastEventId 이후 이벤트 찾아 전송
    List<Notification> lostEvents = emitterRepository.findCachedEventsAfter(userId, lastEventId);

    for (Notification notification : lostEvents) {
      try {
        SseEmitter.SseEventBuilder event =
            SseEmitter.event()
                .id(String.valueOf(notification.getId()))
                .name("notification")
                .data(NotificationItem.from(notification));
        emitter.send(event);
      } catch (IOException e) {
        log.warn("초기 SSE 이벤트 전송 실패. emitter는 유지", e); // 초기 전송 실패는 일시적일 수 있으므로 무시하고 연결 유지
        // 유실된 이벤트는 재시도하지 않음
        // - 이유: DB에 저장되어 목록 조회 가능, 다음 알림으로 빨간점 표시 가능, 타임아웃(5분) 이후 재연결 시 재전송됨
        // - 필요 시 큐 + 재시도 스케줄러 구조 도입 (구현 복잡도 대비 실효성 낮아 현재는 미도입)
      }
    }
  }
}
