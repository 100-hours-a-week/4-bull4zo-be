package com.moa.moa_server.domain.group.dto.group_manage;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 수정 요청 DTO")
public record GroupUpdateRequest(
    @Schema(description = "변경할 그룹 이름", example = "KTB") String name,
    @Schema(description = "변경할 그룹 소개", example = "카카오 부트캠프 야자타임 전용 그룹입니다.") String description,
    @Schema(description = "변경할 그룹 이미지 URL", example = "https://s3.amazonaws.com/group-img/ktb.jpg")
        String imageUrl,
    @Schema(description = "첨부 이미지 이름", example = "카테부단체사진.jpeg") String imageName,
    @Schema(description = "초대코드 재생성 여부", example = "false") boolean changeInviteCode) {}
