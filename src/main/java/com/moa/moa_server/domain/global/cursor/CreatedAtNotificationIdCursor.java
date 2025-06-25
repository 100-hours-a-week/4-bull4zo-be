package com.moa.moa_server.domain.global.cursor;

import com.moa.moa_server.domain.notification.handler.NotificationErrorCode;
import com.moa.moa_server.domain.notification.handler.NotificationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public record CreatedAtNotificationIdCursor(LocalDateTime createdAt, Long notificationId) {

  /** "createdAt_notificationId" 형식의 커서를 파싱 */
  public static CreatedAtNotificationIdCursor parse(String cursor) {
    try {
      String[] parts = cursor.split("_");
      if (parts.length != 2) {
        throw new NotificationException(NotificationErrorCode.INVALID_CURSOR_FORMAT);
      }
      return new CreatedAtNotificationIdCursor(
          LocalDateTime.parse(parts[0]), Long.parseLong(parts[1]));
    } catch (DateTimeParseException | NumberFormatException e) {
      throw new NotificationException(NotificationErrorCode.INVALID_CURSOR_FORMAT);
    }
  }

  public String encode() {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    return createdAt.format(formatter) + "_" + notificationId;
  }
}
