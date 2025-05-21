package com.moa.moa_server.domain.global.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
  String name();

  HttpStatus getStatus();
}
