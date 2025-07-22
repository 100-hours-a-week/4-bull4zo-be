package com.moa.moa_server.domain.notification.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class FakeEmitterRepository implements EmitterRepository {
  private final Map<String, SseEmitter> store = new ConcurrentHashMap<>();

  @Override
  public void save(String id, SseEmitter emitter) {
    store.put(id, emitter);
  }

  @Override
  public void deleteById(String id) {
    store.remove(id);
  }

  @Override
  public Map<String, SseEmitter> findAllEmitters() {
    return store;
  }

  @Override
  public Map<String, SseEmitter> findAllByUserId(Long userId) {
    return store.entrySet().stream()
        .filter(e -> e.getKey().startsWith(userId + "_"))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public boolean contains(String key) {
    return store.containsKey(key);
  }
}
