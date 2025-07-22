package com.moa.moa_server.domain.notification.repository;

import com.moa.moa_server.domain.notification.entity.Notification;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class NotificationEmitterRepository implements EmitterRepository {

  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  private final NotificationRepository notificationRepository;

  public void save(String id, SseEmitter emitter) {
    emitters.put(id, emitter);
  }

  public void deleteById(String id) {
    emitters.remove(id);
  }

  public Map<String, SseEmitter> findAllEmitters() {
    return emitters;
  }

  public Map<String, SseEmitter> findAllByUserId(Long userId) {
    return emitters.entrySet().stream()
        .filter(e -> e.getKey().startsWith(userId + "_"))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public void deleteAllByUserId(Long userId) {
    findAllByUserId(userId).keySet().forEach(this::deleteById);
  }

  @Transactional(readOnly = true)
  public List<Notification> findCachedEventsAfter(Long userId, String lastEventId) {
    Long lastId = Long.parseLong(lastEventId);
    return notificationRepository.findByUserIdAndIdGreaterThanOrderById(userId, lastId);
  }
}
