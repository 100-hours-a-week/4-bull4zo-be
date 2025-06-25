package com.moa.moa_server.domain.notification.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.notification.dto.NotificationListResponse;
import com.moa.moa_server.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "알림 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(summary = "알림 목록 조회", description = "사용자의 최신 알림 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
      @AuthenticationPrincipal Long userId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Integer size) {
    NotificationListResponse response = notificationService.getNotifications(userId, cursor, size);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }
}
