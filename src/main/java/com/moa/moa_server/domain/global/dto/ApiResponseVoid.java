package com.moa.moa_server.domain.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// Swagger 전용 문서화용 빈 응답 DTO
@Schema(description = "응답 데이터가 없는 공통 응답 객체")
public record ApiResponseVoid(

        @Schema(description = "응답 메시지", example = "SUCCESS")
        String message,

        @Schema(description = "응답 데이터", nullable = true, example = "null")
        Object data
) {}
