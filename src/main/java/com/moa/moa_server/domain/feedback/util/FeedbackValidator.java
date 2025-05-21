package com.moa.moa_server.domain.feedback.util;

import com.moa.moa_server.domain.feedback.handler.FeedbackErrorCode;
import com.moa.moa_server.domain.feedback.handler.FeedbackException;

public class FeedbackValidator {

  public static void validateContent(String content) {
    if (content == null || content.isBlank() || content.length() > 500 || content.length() < 2) {
      throw new FeedbackException(FeedbackErrorCode.INVALID_INPUT);
    }
  }
}
