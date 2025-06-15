package com.moa.moa_server.domain.group.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.group.dto.request.GroupJoinRequest;
import com.moa.moa_server.domain.group.dto.response.GroupJoinResponse;
import com.moa.moa_server.domain.group.service.GroupUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GroupUser", description = "그룹 유저 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupUserController {
  private final GroupUserService groupUserService;

  @Operation(summary = "그룹 가입", description = "초대 코드를 통해 사용자가 그룹에 참여합니다.")
  @PostMapping("/join")
  public ResponseEntity<ApiResponse<GroupJoinResponse>> joinGroup(
      @AuthenticationPrincipal Long userId, @RequestBody GroupJoinRequest request) {
    GroupJoinResponse response = groupUserService.joinGroup(userId, request);
    return ResponseEntity.status(201).body(new ApiResponse<>("SUCCESS", response));
  }
}
