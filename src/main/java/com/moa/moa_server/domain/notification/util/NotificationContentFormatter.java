package com.moa.moa_server.domain.notification.util;

public class NotificationContentFormatter {
  private static final int MAX_LENGTH = 36;

  public static String truncateComment(String content) {
    if (content == null) return "";
    int codePointLength = content.codePointCount(0, content.length());
    return codePointLength > MAX_LENGTH
        ? content.substring(0, content.offsetByCodePoints(0, MAX_LENGTH)) + "..."
        : content;
  }
}
