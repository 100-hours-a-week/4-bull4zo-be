package com.moa.moa_server.domain.vote.util;

import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class VoteValidator {

  private static final String UPLOAD_URL_PREFIX = "https://upload-domain/vote";

  public static void validateContent(String content) {
    if (content == null || content.isBlank() || content.length() > 255 || content.length() < 2) {
      throw new VoteException(VoteErrorCode.INVALID_CONTENT);
    }
  }

  public static void validateImageUrl(String imageUrl) {
    if (imageUrl != null && !imageUrl.isBlank() && !imageUrl.startsWith(UPLOAD_URL_PREFIX)) {
      throw new VoteException(VoteErrorCode.INVALID_URL);
    }
  }

  public static void validateClosedAt(LocalDateTime closedAt) {
    if (closedAt == null || !closedAt.isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
      throw new VoteException(VoteErrorCode.INVALID_TIME);
    }
  }
}
