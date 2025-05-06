package com.moa.moa_server.domain.global.cursor;

import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public record ClosedAtVoteIdCursor(LocalDateTime closedAt, Long voteId) {

    /**
     * "closedAt_voteId" 형식의 커서를 파싱
     */
    public static ClosedAtVoteIdCursor parse(String cursor) {
        try {
            String[] parts = cursor.split("_");
            if (parts.length != 2) {
                log.warn("[ClosedAtVoteIdCursor#parse] Cursor must contain exactly two parts.");
                throw new VoteException(VoteErrorCode.INVALID_CURSOR_FORMAT);
            }
            return new ClosedAtVoteIdCursor(
                    LocalDateTime.parse(parts[0]),
                    Long.parseLong(parts[1])
            );
        } catch (DateTimeParseException | NumberFormatException e) {
            log.warn("[ClosedAtVoteIdCursor#parse] Failed to parse cursor '{}': {}", cursor, e.toString());
            throw new VoteException(VoteErrorCode.INVALID_CURSOR_FORMAT);
        }
    }

    public String encode() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return closedAt.format(formatter) + "_" + voteId;
    }
}
