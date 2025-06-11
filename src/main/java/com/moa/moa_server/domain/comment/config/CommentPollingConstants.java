package com.moa.moa_server.domain.comment.config;

/**
 * 댓글 롱폴링에 사용되는 고정 상수 정의 클래스.
 *
 * <p>현재는 코드 내 고정값으로 사용하지만, 추후 운영 환경 별로 설정값을 분리하는 경우 application-{profile}.yml에
 * 정의하고 @ConfigurationProperties 클래스로 리팩토링할 예정.
 */
public final class CommentPollingConstants {

  public static final long TIMEOUT_MILLIS = 10_000L; // 롱폴링 내부 대기 시간 (10초)
  public static final int INTERVAL_MILLIS = 500; // 폴링 주기 (0.5초)
  public static final int MAX_POLL_SIZE = 10; // 최대 조회 개수
  public static final long CONTROLLER_TIMEOUT_MILLIS =
      11_000L; // 컨트롤러 응답 타임아웃 (timeoutMillis 보다 길어야 함)

  private CommentPollingConstants() {
    throw new AssertionError("Cannot instantiate constant class");
  }
}
