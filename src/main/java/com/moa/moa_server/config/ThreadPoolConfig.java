package com.moa.moa_server.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

  @Bean("commentPollingExecutor")
  public ExecutorService commentPollingExecutor() {
    return Executors.newFixedThreadPool(20);
  }
}
