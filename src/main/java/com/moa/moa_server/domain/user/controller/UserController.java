package com.moa.moa_server.domain.user.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.dto.ApiResponseVoid;
import com.moa.moa_server.domain.user.dto.request.UserUpdateRequest;
import com.moa.moa_server.domain.user.dto.response.GroupLabelResponse;
import com.moa.moa_server.domain.user.dto.response.JoinedGroupResponse;
import com.moa.moa_server.domain.user.dto.response.UserInfoResponse;
import com.moa.moa_server.domain.user.dto.response.UserUpdateResponse;
import com.moa.moa_server.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "유저 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "가입한 그룹 라벨 조회", description = "사용자가 가입한 그룹들의 Id와 이름을 조회합니다.")
    @GetMapping("/groups/labels")
    public ResponseEntity<ApiResponse<GroupLabelResponse>> getJoinedGroupLabels(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    ) {
        GroupLabelResponse response = userService.getJoinedGroupLabels(userId, cursor, size);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
    }

    @Operation(summary = "가입한 그룹 목록 조회", description = "사용자가 가입한 그룹들의 상세 정보를 조회합니다.")
    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<JoinedGroupResponse>> getJoinedGroups(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    ) {
        JoinedGroupResponse response = userService.getJoinedGroups(userId, cursor, size);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
    }

    @Operation(summary = "회원 정보 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserInfoResponse response = userService.getUserInfo(userId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
    }

    @Operation(summary = "회원 정보 수정")
    @PatchMapping
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateUserInfo(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserUpdateRequest request
    ) {
        UserUpdateResponse response = userService.updateUserInfo(userId, request);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
    }

    @Operation(summary = "회원 탈퇴",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ApiResponseVoid.class))
                    )
            }
    )
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal Long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", null));
    }
}
