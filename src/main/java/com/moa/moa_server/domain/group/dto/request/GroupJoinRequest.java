package com.moa.moa_server.domain.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 가입 요청 DTO")
public record GroupJoinRequest(
        @Schema(description = "초대 코드", example = "EX39S1")
        String inviteCode
) {}
