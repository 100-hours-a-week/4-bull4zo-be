package com.moa.moa_server.domain.user.dto.response;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 정보")
public record GroupDetail(
    @Schema(description = "그룹 ID", example = "3") Long groupId,
    @Schema(description = "그룹 이름", example = "카테부") String name,
    @Schema(description = "그룹 소개", example = "카카오 부트캠프전용 야자타임 커뮤니티 입니다.") String description,
    @Schema(description = "그룹 이미지 URL", example = "https://s3.amazonaws.com/....jpg")
        String imageUrl,
    @Schema(description = "그룹 초대 코드", example = "E78XC1") String inviteCode,
    @Schema(description = "그룹 내 사용자의 역할", example = "MEMBER") String role) {
  public static GroupDetail from(Group group, GroupMember.Role role) {
    return new GroupDetail(
        group.getId(),
        group.getName(),
        group.getDescription(),
        group.getImageUrl(),
        group.getInviteCode(),
        role.name());
  }

  public static GroupDetail from(GroupMember member) {
    return from(member.getGroup(), member.getRole());
  }
}
