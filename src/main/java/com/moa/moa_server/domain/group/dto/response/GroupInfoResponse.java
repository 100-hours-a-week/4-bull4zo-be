package com.moa.moa_server.domain.group.dto.response;

import com.moa.moa_server.domain.group.entity.Group;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 정보 조회 응답 DTO")
public record GroupInfoResponse(
    @Schema(description = "그룹 ID", example = "17") Long groupId,
    @Schema(description = "그룹 이름", example = "춘식이 연구회") String name,
    @Schema(description = "그룹 소개", example = "매일 춘식이 사진 공유") String description,
    @Schema(description = "그룹 이미지 URL", example = "https://s3.amazonaws.com/....jpg")
        String imageUrl,
    @Schema(description = "그룹 이미지 이름", example = "이미지.jpeg") String imageName,
    @Schema(description = "초대 코드", example = "ABC123") String inviteCode,
    @Schema(description = "사용자의 그룹 역할", example = "MANAGER") String role) {
  public GroupInfoResponse(Group group, String role) {
    this(
        group.getId(),
        group.getName(),
        group.getDescription(),
        group.getImageUrl(),
        group.getImageName(),
        group.getInviteCode(),
        role);
  }
}
