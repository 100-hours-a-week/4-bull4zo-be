package com.moa.moa_server.domain.notification.application.sse;

import static org.assertj.core.api.Assertions.assertThat;

import com.moa.moa_server.domain.notification.config.SseProperties;
import com.moa.moa_server.domain.notification.repository.FakeEmitterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class NotificationSseCleanerIntegrationTest {

  FakeEmitterRepository fakeRepo;
  SseProperties props;
  NotificationSseCleaner cleaner;

  @BeforeEach
  void setUp() {
    fakeRepo = new FakeEmitterRepository();

    props = new SseProperties();
    props.setStaleThreshold(1000L); // 1초 넘으면 stale
    props.setStaleCleanInterval(5000L); // 무관

    cleaner = new NotificationSseCleaner(fakeRepo, props);
  }

  @Test
  @DisplayName("오래된 emitter는 cleanUpStaleEmitters 실행 시 제거됨")
  void testOldEmitterIsRemoved() {
    long now = System.currentTimeMillis();

    String staleKey = "user1_" + (now - 5000);
    String validKey = "user2_" + now;

    fakeRepo.save(staleKey, new SseEmitter());
    fakeRepo.save(validKey, new SseEmitter());

    // when
    cleaner.cleanUpStaleEmitters();

    // then
    assertThat(fakeRepo.contains(staleKey)).isFalse();
    assertThat(fakeRepo.contains(validKey)).isTrue();
    assertThat(fakeRepo.findAllEmitters()).hasSize(1);
  }
}
