package com.moa.moa_server.domain.group.dto.response;

public record GroupJoinResponse(
        Long groupId,
        String groupName,
        String role
) {}
