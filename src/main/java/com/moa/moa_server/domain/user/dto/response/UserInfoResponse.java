package com.moa.moa_server.domain.user.dto.response;

public record UserInfoResponse(
        String nickname
) {
    public static UserInfoResponse from(String nickname) {
        return new UserInfoResponse(nickname);
    }
}
