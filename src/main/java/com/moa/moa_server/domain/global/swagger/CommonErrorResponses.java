package com.moa.moa_server.domain.global.swagger;

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
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "INVALID_TOKEN",
                                        description = "토큰 없음 / 잘못된 토큰 / 토큰 만료",
                                        value = "{\"message\": \"INVALID_TOKEN\", \"data\": null}"
                                ),
                                @ExampleObject(
                                        name = "USER_NOT_FOUND",
                                        description = "토큰 userId로 유저 찾을 수 없음",
                                        value = "{\"message\": \"USER_NOT_FOUND\", \"data\": null}"
                                ),
                                @ExampleObject(
                                        name = "USER_WITHDRAWN",
                                        description = "탈퇴한 유저",
                                        value = "{\"message\": \"USER_WITHDRAWN\", \"data\": null}"
                                )
                        }
                )
        ),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                content = @io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        examples =
                                @ExampleObject(
                                        name = "UNEXPECTED_ERROR",
                                        description = "서버 내부 오류",
                                        value = "{\"message\": \"UNEXPECTED_ERROR\", \"data\": null}"
                                )
                        )
        )
})
public @interface CommonErrorResponses {
}
