package com.moa.moa_server.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원정보 조회 응답 DTO")
public record UserInfoResponse(
        @Schema(description = "닉네임", example = "nickname")
        String nickname
) {
    public static UserInfoResponse from(String nickname) {
        return new UserInfoResponse(nickname);
    }
}
