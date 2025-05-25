package com.moa.moa_server.domain.global.handler;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.exception.BaseException;
import com.moa.moa_server.domain.global.exception.GlobalErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
    return ResponseEntity.status(ex.getStatus()).body(new ApiResponse<>(ex.getCode(), null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnhandled(Exception ex) {
    log.error("Unhandled Exception Occurred", ex);
    return ResponseEntity.status(500)
        .body(new ApiResponse<>(GlobalErrorCode.UNEXPECTED_ERROR.name(), null));
  }
}
