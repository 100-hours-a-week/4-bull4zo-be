package com.moa.moa_server.domain.notification.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "sse")
public class SseProperties {
  private long timeout;
  private long pingInterval;
  private long staleCleanInterval;
  private long staleThreshold;

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public void setPingInterval(long pingInterval) {
    this.pingInterval = pingInterval;
  }

  public void setStaleCleanInterval(long staleCleanInterval) {
    this.staleCleanInterval = staleCleanInterval;
  }

  public void setStaleThreshold(long staleThreshold) {
    this.staleThreshold = staleThreshold;
  }
}
