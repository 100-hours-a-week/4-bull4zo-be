package com.moa.moa_server.config;

import java.util.concurrent.RejectedExecutionHandler;
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
  public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("async-pool-");
    executor.initialize();
    return executor;
  }

  // 댓글 롱폴링 전용 스레드풀
  // - 특징: 대기 시간이 길 수 있고, 동시 요청이 많을 수 있음
  // - 큐/풀 초과 시: 작업 거부 및 예외 발생시켜 클라이언트에게 즉시 실패 응답 → 재요청 유도
  @Bean(name = "commentPollingExecutor")
  public ThreadPoolTaskExecutor commentPollingExecutor() {
    return buildExecutor("comment-polling-", 10, 20, 200, new ThreadPoolExecutor.AbortPolicy());
  }

  // NotificationHandler (알림 비동기 처리) 전용 스레드풀
  // - 특징: 빠르게 끝나는 경량 작업
  // - 큐/풀 초과 시: 호출한 스레드에서 처리하여 알림 유실 방지
  @Bean(name = "notificationExecutor")
  public ThreadPoolTaskExecutor notificationExecutor() {
    return buildExecutor(
        "notification-async-", 3, 6, 100, new ThreadPoolExecutor.CallerRunsPolicy());
  }

  // 공통 executor 생성 로직
  private ThreadPoolTaskExecutor buildExecutor(
      String name, int core, int max, int queue, RejectedExecutionHandler handler) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(core); // 스레드풀 크기
    executor.setMaxPoolSize(max); // 최대 스레드풀 크기
    executor.setQueueCapacity(queue); // 작업 대기 큐 용량
    executor.setThreadNamePrefix(name); // 스레드 이름
    executor.setRejectedExecutionHandler(handler); // 풀이 가득 찼을 때 대응
    executor.initialize();
    return executor;
  }
}
