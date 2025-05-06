package com.moa.moa_server.domain.group.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.group.dto.request.GroupCreateRequest;
import com.moa.moa_server.domain.group.dto.request.GroupJoinRequest;
import com.moa.moa_server.domain.group.dto.response.GroupCreateResponse;
import com.moa.moa_server.domain.group.dto.response.GroupJoinResponse;
import com.moa.moa_server.domain.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponse> joinGroup(
            @AuthenticationPrincipal Long userId,
            @RequestBody GroupJoinRequest request
    ) {
        GroupJoinResponse response = groupService.joinGroup(userId, request);
        return ResponseEntity
                .status(201)
                .body(new ApiResponse("SUCCESS", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createGroup(
            @AuthenticationPrincipal Long userId,
            @RequestBody GroupCreateRequest request
    ) {
        GroupCreateResponse response = groupService.createGroup(userId, request);
        return ResponseEntity
                .status(201)
                .body(new ApiResponse("SUCCESS", response));
    }
}
