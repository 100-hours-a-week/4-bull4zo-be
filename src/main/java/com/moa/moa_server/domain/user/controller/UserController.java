package com.moa.moa_server.domain.user.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.user.dto.response.GroupLabelResponse;
import com.moa.moa_server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
