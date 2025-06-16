package com.moa.moa_server.domain.group.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.group.dto.group_member.ChangeRoleRequest;
import com.moa.moa_server.domain.group.dto.group_member.ChangeRoleResponse;
import com.moa.moa_server.domain.group.dto.group_member.MemberDeleteResponse;
import com.moa.moa_server.domain.group.dto.group_member.MemberListResponse;
import com.moa.moa_server.domain.group.service.GroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "GroupMember", description = "그룹 멤버 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupMemberController {

  private final GroupMemberService groupMemberService;

  @Operation(summary = "그룹 멤버 목록 조회", description = "그룹에 속한 모든 멤버 목록을 조회합니다.")
  @GetMapping("/{groupId}/members")
  public ResponseEntity<ApiResponse<MemberListResponse>> getMemberList(
      @AuthenticationPrincipal Long userId, @PathVariable Long groupId) {
    MemberListResponse response = groupMemberService.getMemberList(userId, groupId);
    return ResponseEntity.status(200).body(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(summary = "그룹 멤버 역할 변경", description = "그룹 멤버의 역할을 변경합니다.")
  @PatchMapping("/{groupId}/members/{targetUserId}")
  public ResponseEntity<ApiResponse<ChangeRoleResponse>> changeMemberRole(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long groupId,
      @PathVariable Long targetUserId,
      @RequestBody @Valid ChangeRoleRequest request) {
    ChangeRoleResponse response =
        groupMemberService.changeRole(userId, groupId, targetUserId, request.role());
    return ResponseEntity.status(200).body(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(summary = "그룹 멤버 내보내기", description = "소유자가 그룹 멤버를 그룹에서 내보낼 수 있습니다.")
  @DeleteMapping("/{groupId}/members/{targetUserId}")
  public ResponseEntity<ApiResponse<MemberDeleteResponse>> deleteMember(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long groupId,
      @PathVariable Long targetUserId) {
    MemberDeleteResponse response = groupMemberService.deleteMember(userId, groupId, targetUserId);
    return ResponseEntity.status(200).body(new ApiResponse<>("SUCCESS", response));
  }
}
