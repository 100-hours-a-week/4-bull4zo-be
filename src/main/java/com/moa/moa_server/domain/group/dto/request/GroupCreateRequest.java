package com.moa.moa_server.domain.group.dto.request;

public record GroupCreateRequest(
        String name,
        String description,
        String imageUrl
) {}
