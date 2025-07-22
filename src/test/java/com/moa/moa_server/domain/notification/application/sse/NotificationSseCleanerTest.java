package com.moa.moa_server.domain.notification.application.sse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.AbstractMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** NotificationSseCleaner의 emitter 제거 조건 판단 메서드(isExpiredEmitterEntry) 단위 테스트. */
public class NotificationSseCleanerTest {

  NotificationSseCleaner cleaner;

  @BeforeEach
  void setUp() {
    cleaner = new NotificationSseCleaner(null, null); // emitterRepository, sseProperties 필요 없음
  }

  @Test
  @DisplayName("오래된 emitter는 제거 (true 반환)")
  void testExpiredEmitterEntry_shouldReturnTrue_ifTimestampTooOld() {
    long now = System.currentTimeMillis();
    long threshold = 1000L;

    String key = "123_" + (now - 2000); // 오래된 timestamp
    Map.Entry<String, SseEmitter> entry = new AbstractMap.SimpleEntry<>(key, new SseEmitter());

    boolean result = cleaner.isExpiredEmitterEntry(entry, now, threshold);
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("최신 emitter는 제거 대상 아님 (false 반환)")
  void testExpiredEmitterEntry_shouldReturnFalse_ifTimestampRecent() {
    long now = System.currentTimeMillis();
    long threshold = 5000L;

    String key = "123_" + now; // 방금 생성됨
    Map.Entry<String, SseEmitter> entry = new AbstractMap.SimpleEntry<>(key, new SseEmitter());

    boolean result = cleaner.isExpiredEmitterEntry(entry, now, threshold);
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("잘못된 키 형식 또는 파싱 실패 시 제거 대상 (true 반환)")
  void testExpiredEmitterEntry_shouldReturnTrue_ifKeyMalformed() {
    Map.Entry<String, SseEmitter> entry1 =
        new AbstractMap.SimpleEntry<>("invalidKey", new SseEmitter());
    Map.Entry<String, SseEmitter> entry2 = new AbstractMap.SimpleEntry<>("123_", new SseEmitter());
    Map.Entry<String, SseEmitter> entry3 =
        new AbstractMap.SimpleEntry<>("123_456", new SseEmitter());

    long now = System.currentTimeMillis();
    long threshold = 1000L;

    assertThat(cleaner.isExpiredEmitterEntry(entry1, now, threshold)).isTrue();
    assertThat(cleaner.isExpiredEmitterEntry(entry2, now, threshold)).isTrue();
    assertThat(cleaner.isExpiredEmitterEntry(entry3, now, threshold)).isTrue();
  }
}
