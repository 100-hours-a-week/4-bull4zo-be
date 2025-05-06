package com.moa.moa_server.domain.group.dto.response;

import java.time.LocalDateTime;

public record GroupCreateResponse(
        Long groupId,
        String name,
        String description,
        String imageUrl,
        String inviteCode,
        LocalDateTime createdAt
) {}
