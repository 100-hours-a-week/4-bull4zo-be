package com.moa.moa_server.domain.notification.repository;

import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {
  void save(String id, SseEmitter emitter);

  void deleteById(String id);

  Map<String, SseEmitter> findAllEmitters();

  public Map<String, SseEmitter> findAllByUserId(Long userId);
}
