package com.moa.moa_server.domain.global.cursor;

import com.moa.moa_server.domain.comment.handler.CommentErrorCode;
import com.moa.moa_server.domain.comment.handler.CommentException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public record CreatedAtCommentIdCursor(LocalDateTime createdAt, Long commentId) {

  /** "createdAt_commentId" 형식의 커서를 파싱 */
  public static CreatedAtCommentIdCursor parse(String cursor) {
    try {
      String[] parts = cursor.split("_");
      if (parts.length != 2) {
        throw new CommentException(CommentErrorCode.INVALID_CURSOR_FORMAT);
      }
      return new CreatedAtCommentIdCursor(LocalDateTime.parse(parts[0]), Long.parseLong(parts[1]));
    } catch (DateTimeParseException | NumberFormatException e) {
      throw new CommentException(CommentErrorCode.INVALID_CURSOR_FORMAT);
    }
  }

  public String encode() {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    return createdAt.format(formatter) + "_" + commentId;
  }
}
