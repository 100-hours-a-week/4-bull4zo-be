package com.moa.moa_server.domain.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 응답 객체")
public record ApiResponse<T>(

        @Schema(description = "응답 메시지", example = "SUCCESS")
        String message,

        @Schema(description = "응답 데이터")
        T data
) {}
