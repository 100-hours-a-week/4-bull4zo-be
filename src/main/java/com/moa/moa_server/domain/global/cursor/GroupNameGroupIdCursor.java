package com.moa.moa_server.domain.global.cursor;

import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record GroupNameGroupIdCursor(String groupName, Long groupId) {

    /**
     * "groupName_groupId" 형식의 커서를 파싱
     */
    public static GroupNameGroupIdCursor parse(String cursor) {
        try {
            String[] parts = cursor.split("_");
            if (parts.length != 2) {
                log.warn("[GroupNameGroupIdCursor#parse] Invalid format: '{}'", cursor);
                throw new UserException(UserErrorCode.INVALID_CURSOR_FORMAT);
            }
            return new GroupNameGroupIdCursor(
                    parts[0],
                    Long.parseLong(parts[1])
            );
        } catch (NumberFormatException e) {
            log.warn("[GroupNameGroupIdCursor#parse] Failed to parse groupId from cursor '{}': {}", cursor, e.toString());
            throw new UserException(UserErrorCode.INVALID_CURSOR_FORMAT);
        }
    }

    public String encode() {
        return groupName + "_" + groupId;
    }
}
