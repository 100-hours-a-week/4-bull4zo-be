package com.moa.moa_server.domain.global.cursor;

import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record VotedAtVoteIdCursor(LocalDateTime votedAt, Long voteId) {

  /** "votedAt_voteId" 형식의 커서를 파싱 */
  public static VotedAtVoteIdCursor parse(String cursor) {
    try {
      String[] parts = cursor.split("_");
      if (parts.length != 2) {
        log.warn("[VotedAtVoteIdCursor#parse] Cursor must contain exactly two parts.");
        throw new VoteException(VoteErrorCode.INVALID_CURSOR_FORMAT);
      }
      return new VotedAtVoteIdCursor(LocalDateTime.parse(parts[0]), Long.parseLong(parts[1]));
    } catch (DateTimeParseException | NumberFormatException e) {
      log.warn("[VotedAtVoteIdCursor#parse] Failed to parse cursor '{}': {}", cursor, e.toString());
      throw new VoteException(VoteErrorCode.INVALID_CURSOR_FORMAT);
    }
  }

  public String encode() {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    return votedAt.format(formatter) + "_" + voteId;
  }
}
