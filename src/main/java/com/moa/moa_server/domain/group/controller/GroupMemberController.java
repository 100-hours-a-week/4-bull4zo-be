package com.moa.moa_server.domain.group.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.group.dto.response.MemberListResponse;
import com.moa.moa_server.domain.group.service.GroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
