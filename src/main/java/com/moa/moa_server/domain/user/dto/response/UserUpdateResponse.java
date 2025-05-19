package com.moa.moa_server.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원정보 수정 응답 DTO")
public record UserUpdateResponse(
        @Schema(description = "변경된 닉네임", example = "nickname")
        String nickname
) {}
