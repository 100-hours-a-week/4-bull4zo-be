package com.moa.moa_server.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원정보 수정 요청 DTO")
public record UserUpdateRequest(
        @Schema(description = "변경할 닉네임", example = "nickname")
        String nickname
) {}
