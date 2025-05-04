package com.moa.moa_server.domain.global.cursor;

import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public record VoteClosedCursor(LocalDateTime closedAt, LocalDateTime createdAt) {

    /**
     * "closedAt_createdAt" 형식의 커서를 LocalDateTime 쌍으로 파싱
     */
    public static VoteClosedCursor parse(String cursor) {
        try {
            String[] parts = cursor.split("_");
            if (parts.length != 2) {
                log.warn("[VoteClosedCursor#parse] Cursor must contain exactly two parts.");
                throw new VoteException(VoteErrorCode.INVALID_CURSOR_FORMAT);
            }
            return new VoteClosedCursor(
                    LocalDateTime.parse(parts[0]),
                    LocalDateTime.parse(parts[1])
            );
        } catch (Exception e) {
            log.warn("[VoteClosedCursor#parse] Failed to parse cursor '{}': {}", cursor, e.toString());
            throw new VoteException(VoteErrorCode.INVALID_CURSOR_FORMAT);
        }
    }

    public String encode() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return closedAt.format(formatter) + "_" + createdAt.format(formatter);
    }
}
