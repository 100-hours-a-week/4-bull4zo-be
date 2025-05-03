package com.moa.moa_server.domain.global.handler;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.exception.BaseException;
import com.moa.moa_server.domain.global.exception.GlobalErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse> handleBaseException(BaseException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiResponse(ex.getCode(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUnhandled(Exception ex) {
        return ResponseEntity
                .status(500)
                .body(new ApiResponse(GlobalErrorCode.UNEXPECTED_ERROR.name(), null));
    }
}
