package com.moa.moa_server.domain.vote.util;

import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;

import java.net.URL;
import java.time.LocalDateTime;

public class VoteValidator {

    public static void validateContent(String content) {
        if (content == null || content.isBlank() || content.length() > 255 || content.length() < 2) {
            throw new VoteException(VoteErrorCode.INVALID_CONTENT);
        }
    }

    public static void validateUrl(String url) {
        if (url == null || url.isBlank()) return;
        try {
            new URL(url).toURI();
        } catch (Exception e) {
            throw new VoteException(VoteErrorCode.INVALID_URL);        }
    }

    public static void validateClosedAt(LocalDateTime closedAt) {
        if (closedAt == null || !closedAt.isAfter(LocalDateTime.now())) {
            throw new VoteException(VoteErrorCode.INVALID_TIME);        }
    }
}
