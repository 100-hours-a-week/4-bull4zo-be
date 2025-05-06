package com.moa.moa_server.domain.vote.util;

import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

public class VoteValidator {

    private static String uploadUrlPrefix;

    @Value("${file.upload-url-prefix}")
    public void setUploadUrlPrefix(String prefix) {
        VoteValidator.uploadUrlPrefix = prefix + "/vote";
    }

    public static void validateContent(String content) {
        if (content == null || content.isBlank() || content.length() > 255 || content.length() < 2) {
            throw new VoteException(VoteErrorCode.INVALID_CONTENT);
        }
    }

    public static void validateImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.isBlank() && !imageUrl.startsWith(uploadUrlPrefix)) {
            throw new VoteException(VoteErrorCode.INVALID_URL);
        }
    }

    public static void validateClosedAt(LocalDateTime closedAt) {
        if (closedAt == null || !closedAt.isAfter(LocalDateTime.now())) {
            throw new VoteException(VoteErrorCode.INVALID_TIME);        }
    }
}
