package com.moa.moa_server.domain.auth.swagger;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Not Found",
                content = @io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "INVALID_PROVIDER",
                                description = "지원하지 않는 소셜 로그인 제공자",
                                value = "{\"message\": \"INVALID_PROVIDER\", \"data\": null}"
                        )
                )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = {
                        @ExampleObject(
                                name = "KAKAO_TOKEN_FAILED",
                                description = "카카오 토큰 받기 실패",
                                value = "{\"message\": \"KAKAO_TOKEN_FAILED\", \"data\": null}"
                        ),
                        @ExampleObject(
                                name = "KAKAO_USERINFO_FAILED",
                                description = "카카오 사용자 정보 받기 실패",
                                value = "{\"message\": \"KAKAO_USERINFO_FAILED\", \"data\": null}"
                        )
                })
        ),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                content = @io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        examples = {
                            @ExampleObject(
                                    name = "UNEXPECTED_ERROR",
                                    description = "서버 내부 오류",
                                    value = "{\"message\": \"UNEXPECTED_ERROR\", \"data\": null}"
                            ),
                            @ExampleObject(
                                    name = "NICKNAME_GENERATION_FAILED",
                                    description = "닉네임 생성 실패",
                                    value = "{\"message\": \"NICKNAME_GENERATION_FAILED\", \"data\": null}"
                            )
                        }
                )
        )
})
public @interface LoginResponses {
}
