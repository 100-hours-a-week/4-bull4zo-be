package com.moa.moa_server.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  // 기본 비동기 풀 (전제 @Async의 기본)
  // 명시적으로 기본 풀을 등록해 둠
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("async-pool-");
    executor.initialize();
    return executor;
  }

  // CommentPollingService 전용 스레드풀
  @Bean(name = "commentPollingExecutor")
  public Executor commentPollingExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10); // 스레드풀 크기
    executor.setMaxPoolSize(20); // 최대 스레드풀 크기
    executor.setQueueCapacity(200); // 작업 대기 큐 용량 (초과 시 새로운 스레드 생성 또는 거부 정책 적용)
    executor.setRejectedExecutionHandler(
        new ThreadPoolExecutor.AbortPolicy() // 작업 거부 및 예외 발생 (FE에서 재요청 유도)
        ); // 풀이 가득 찼을 때 대응
    executor.setThreadNamePrefix("comment-polling-"); // 스레드 이름
    executor.initialize();
    return executor;
  }
}
