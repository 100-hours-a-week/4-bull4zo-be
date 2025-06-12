package com.moa.moa_server.domain.vote.util;

import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class VoteValidator {

  public static void validateContent(String content) {
    if (content == null || content.isBlank()) {
      throw new VoteException(VoteErrorCode.INVALID_CONTENT);
    }
    int codePointLength = content.codePointCount(0, content.length());
    if (codePointLength < 2 || codePointLength > 100) {
      throw new VoteException(VoteErrorCode.INVALID_CONTENT);
    }
  }

  public static void validateOpenAt(LocalDateTime openAt) {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    if (openAt == null || openAt.isBefore(now)) {
      throw new VoteException(VoteErrorCode.INVALID_TIME);
    }
  }

  public static void validateUserVoteClosedAt(LocalDateTime closedAt) {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    if (closedAt == null || !closedAt.isAfter(now) || closedAt.isAfter(now.plusDays(7))) {
      throw new VoteException(VoteErrorCode.INVALID_TIME);
    }
  }

  public static void validateAIVoteClosedAt(LocalDateTime openAt, LocalDateTime closedAt) {
    if (openAt == null
        || closedAt == null
        || !closedAt.isAfter(openAt)
        || closedAt.isAfter(openAt.plusDays(7))) {
      throw new VoteException(VoteErrorCode.INVALID_TIME);
    }
  }
}
