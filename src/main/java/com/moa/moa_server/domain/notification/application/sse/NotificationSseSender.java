package com.moa.moa_server.domain.notification.application.sse;

import com.moa.moa_server.domain.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 알림 SSE 전송을 위한 Facade 클래스.
 *
 * <p>내부적으로 Broadcaster와 ConnectionHelper를 위임해 실제 전송 수행.
 */
@Component
@RequiredArgsConstructor
public class NotificationSseSender {

  private final NotificationSseBroadcaster broadcaster;
  private final NotificationSseConnectionHelper connectionHelper;

  /**
   * 실제 알림을 사용자에게 SSE로 전송.
   *
   * @param notification 전송할 알림 객체
   */
  public void send(Notification notification) {
    broadcaster.send(notification);
  }

  /**
   * SSE 연결 직후, 연결 유지 목적으로 더미 이벤트 전송.
   *
   * @param emitter 연결된 SseEmitter
   */
  public void sendDummyEvent(SseEmitter emitter) {
    connectionHelper.sendDummyEvent(emitter);
  }

  /**
   * 클라이언트가 놓친 이벤트들 재전송. (Last-Event-ID 기반)
   *
   * @param userId 알림 수신 대상 사용자 ID
   * @param lastEventId 마지막으로 수신된 이벤트 ID
   * @param emitter 연결된 SseEmitter
   */
  public void sendLostEvents(Long userId, String lastEventId, SseEmitter emitter) {
    connectionHelper.sendLostEvents(userId, lastEventId, emitter);
  }
}
