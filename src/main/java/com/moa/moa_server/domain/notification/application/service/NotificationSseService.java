package com.moa.moa_server.domain.notification.application.service;

import com.moa.moa_server.domain.notification.application.sse.NotificationSseSender;
import com.moa.moa_server.domain.notification.config.SseProperties;
import com.moa.moa_server.domain.notification.repository.NotificationEmitterRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** SSE 구독 요청을 처리하는 서비스 클래스. */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSseService {

  private final SseProperties sseProperties;
  private final NotificationEmitterRepository emitterRepository;
  private final UserRepository userRepository;
  private final NotificationSseSender notificationSseSender;

  /**
   * 사용자 SSE 구독 처리.
   *
   * <p>SseEmitter를 등록하고, 연결 직후 dummy 전송과 유실 이벤트 복구를 수행한다.
   */
  public SseEmitter subscribe(Long userId, String lastEventId) {
    validateUser(userId);

    // emitter 등록
    SseEmitter emitter = registerEmitter(userId);

    // 최초 연결 시 더미 이벤트 전송
    notificationSseSender.sendDummyEvent(emitter);

    // 마지막 이벤트 ID로 유실된 이벤트 전송
    if (lastEventId != null) {
      notificationSseSender.sendLostEvents(userId, lastEventId, emitter);
    }

    return emitter;
  }

  private SseEmitter registerEmitter(Long userId) {
    SseEmitter emitter = new SseEmitter(sseProperties.getTimeout());
    String emitterId = makeEmitterId(userId);

    emitterRepository.save(emitterId, emitter);

    // 연결 끊김/에러 시 emitter 정리
    emitter.onCompletion(() -> emitterRepository.deleteById(emitterId)); // 클라이언트 정상 종료
    emitter.onTimeout(
        () -> {
          emitter.complete(); // AsyncRequestTimeoutException 예외 로그 남지 않도록 명시적 종료
          emitterRepository.deleteById(emitterId);
        }); // 서버 타임아웃
    emitter.onError((e) -> emitterRepository.deleteById(emitterId)); // 전송 중 예외 (IOException)

    return emitter;
  }

  private String makeEmitterId(Long userId) {
    return userId + "_" + System.currentTimeMillis();
  }

  private void validateUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);
  }
}
