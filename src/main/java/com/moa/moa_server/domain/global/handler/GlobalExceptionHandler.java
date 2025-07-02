package com.moa.moa_server.domain.global.handler;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.exception.BaseException;
import com.moa.moa_server.domain.global.exception.GlobalErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
    return ResponseEntity.status(ex.getStatus()).body(new ApiResponse<>(ex.getCode(), null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnhandled(
      HttpServletRequest request, Exception ex) {
    // 클라이언트 연결 끊김은 무시
    if (ex instanceof AsyncRequestNotUsableException || ex.getCause() instanceof IOException) {
      log.debug("클라이언트 연결 끊김으로 인한 예외 무시: {}", ex.getMessage());
      return ResponseEntity.noContent().build();
    }

    // SSE 요청인 경우 별도 처리 (converter 에러 방지)
    if ("text/event-stream".equals(request.getHeader("Accept"))) {
      return ResponseEntity.noContent().build();
    }

    log.error("Unhandled Exception Occurred", ex);
    return ResponseEntity.status(500)
        .body(new ApiResponse<>(GlobalErrorCode.UNEXPECTED_ERROR.name(), null));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    FieldError error = ex.getBindingResult().getFieldError();
    String code = "INVALID_REQUEST";

    if (error != null && error.getCode() != null) {
      code =
          switch (error.getCode()) {
            case "NotNull" -> "NULL_" + error.getField().toUpperCase();
            case "NotBlank" -> "BLANK_" + error.getField().toUpperCase();
            case "Size" -> "TOO_LONG_" + error.getField().toUpperCase();
            default -> "INVALID_" + error.getField().toUpperCase();
          };
    }

    return ResponseEntity.badRequest().body(new ApiResponse<>(code, null));
  }

  @ExceptionHandler(AsyncRequestTimeoutException.class)
  public void handleTimeout() {
    log.debug("SSE 타임아웃 발생 - 이미 emitter에서 처리됨"); // 로그만 남기고 무시
  }
}
