package com.moa.moa_server.domain.group.dto.response;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 수정 응답 DTO")
public record GroupUpdateResponse(
    @Schema(description = "그룹 ID", example = "3") Long groupId,
    @Schema(description = "그룹 이름", example = "KTB") String name,
    @Schema(description = "그룹 소개", example = "카카오 부트캠프 야자타임 전용 그룹입니다.") String description,
    @Schema(description = "그룹 이미지 URL", example = "https://s3.amazonaws.com/group-img/ktb.jpg")
        String imageUrl,
    @Schema(description = "그룹 이미지 이름", example = "카테부단체사진.jpeg") String imageName,
    @Schema(description = "초대코드", example = "E78XC1") String changeInviteCode,
    @Schema(description = "사용자의 그룹 역할", example = "MEMBER") String role) {
  public static GroupUpdateResponse of(Group group, GroupMember.Role role) {
    return new GroupUpdateResponse(
        group.getId(),
        group.getName(),
        group.getDescription(),
        group.getImageUrl(),
        group.getImageName(),
        group.getInviteCode(),
        role.name());
  }
}
