package com.moa.moa_server.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SystemInfoLogger {

  @PostConstruct
  public void logCpuInfo() {
    int cpuCount = Runtime.getRuntime().availableProcessors();
    log.info("Available processors (CPU cores): {}", cpuCount);
  }
}
