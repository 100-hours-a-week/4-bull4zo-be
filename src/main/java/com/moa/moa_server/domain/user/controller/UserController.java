package com.moa.moa_server.domain.user.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.user.dto.request.UserUpdateRequest;
import com.moa.moa_server.domain.user.dto.response.GroupLabelResponse;
import com.moa.moa_server.domain.user.dto.response.JoinedGroupResponse;
import com.moa.moa_server.domain.user.dto.response.UserInfoResponse;
import com.moa.moa_server.domain.user.dto.response.UserUpdateResponse;
import com.moa.moa_server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/groups/labels")
    public ResponseEntity<ApiResponse> getJoinedGroupLabels(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    ) {
        GroupLabelResponse response = userService.getJoinedGroupLabels(userId, cursor, size);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", response));
    }

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse> getJoinedGroups(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    ) {
        JoinedGroupResponse response = userService.getJoinedGroups(userId, cursor, size);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUserInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserInfoResponse response = userService.getUserInfo(userId);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", response));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse> updateUserInfo(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserUpdateRequest request
    ) {
        UserUpdateResponse response = userService.updateUserInfo(userId, request);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteUser(
            @AuthenticationPrincipal Long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", null));
    }
}
