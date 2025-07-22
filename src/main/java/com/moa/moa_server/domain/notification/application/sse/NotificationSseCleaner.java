package com.moa.moa_server.domain.notification.application.sse;

import com.moa.moa_server.domain.notification.config.SseProperties;
import com.moa.moa_server.domain.notification.repository.NotificationEmitterRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    Map<String, SseEmitter> allEmitters = emitterRepository.findAllEmitters();
    int before = allEmitters.size();

    allEmitters.entrySet().removeIf(entry -> isExpiredEmitterEntry(entry, now, threshold));

    int after = emitterRepository.findAllEmitters().size();
    if (before != after) {
      log.info("[NotificationSseCleaner#cleanUpStaleEmitters] 만료된 emitter 정리: {}개", before - after);
    }
  }

  boolean isExpiredEmitterEntry(Map.Entry<String, SseEmitter> entry, long now, long threshold) {
    String key = entry.getKey(); // userId_timestamp
    int idx = key.lastIndexOf('_');
    if (idx == -1 || idx == key.length() - 1) return true;

    try {
      long createdAt = Long.parseLong(key.substring(idx + 1));
      return (now - createdAt) > threshold;
    } catch (NumberFormatException e) {
      return true; // 잘못된 형식의 키도 정리
    }
  }
}
