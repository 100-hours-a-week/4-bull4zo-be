package com.moa.moa_server.domain.group.dto.group_member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "그룹 멤버 역할 변경 요청 DTO")
public record ChangeRoleRequest(
    @Schema(description = "변경할 역할", example = "MANAGER") @NotBlank String role) {}
