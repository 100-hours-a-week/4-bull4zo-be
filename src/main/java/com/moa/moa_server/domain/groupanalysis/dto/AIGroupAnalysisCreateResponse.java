package com.moa.moa_server.domain.groupanalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 분석 저장 응답 DTO")
public record AIGroupAnalysisCreateResponse(
    @Schema(description = "저장된 그룹 ID", example = "123") Long groupId,
    @Schema(description = "저장 성공 여부", example = "true") boolean created) {}
