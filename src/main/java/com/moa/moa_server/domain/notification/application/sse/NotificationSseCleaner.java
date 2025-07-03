package com.moa.moa_server.domain.notification.application.sse;

import com.moa.moa_server.domain.notification.config.SseProperties;
import com.moa.moa_server.domain.notification.repository.NotificationEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 주기적으로 오래된 SSE Emitter를 정리하는 컴포넌트. */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSseCleaner {

  private final NotificationEmitterRepository emitterRepository;
  private final SseProperties sseProperties;

  @Scheduled(fixedRateString = "#{@sseProperties.staleCleanInterval}")
  public void cleanUpStaleEmitters() {
    long now = System.currentTimeMillis();
    long threshold = sseProperties.getStaleThreshold();

    int before = emitterRepository.findAllEmitters().size();

    emitterRepository
        .findAllEmitters()
        .entrySet()
        .removeIf(
            entry -> {
              String key = entry.getKey(); // user_timestamp
              String[] parts = key.split("_");
              if (parts.length != 2) return true;

              try {
                long createdAt = Long.parseLong(parts[1]);
                return (now - createdAt) > threshold;
              } catch (NumberFormatException e) {
                return true; // 잘못된 형식의 키도 정리
              }
            });

    int after = emitterRepository.findAllEmitters().size();
    if (before != after) {
      log.info("[NotificationSseCleaner#cleanUpStaleEmitters] 만료된 emitter 정리: {}개", before - after);
    }
  }
}
