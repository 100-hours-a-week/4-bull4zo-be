package com.moa.moa_server.domain.notification.application.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** 이벤트 발행 역할 */
@Component
@RequiredArgsConstructor
public class EventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  public void publish(NotificationEvent event) {
    eventPublisher.publishEvent(event);
  }
}
