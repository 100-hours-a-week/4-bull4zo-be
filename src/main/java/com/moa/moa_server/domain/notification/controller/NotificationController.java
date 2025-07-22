package com.moa.moa_server.domain.notification.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.security.SecurityContextUtil;
import com.moa.moa_server.domain.notification.application.service.NotificationService;
import com.moa.moa_server.domain.notification.application.service.NotificationSseService;
import com.moa.moa_server.domain.notification.dto.NotificationListResponse;
import com.moa.moa_server.domain.notification.dto.NotificationReadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notification", description = "알림 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final NotificationService notificationService;
  private final NotificationSseService notificationSseService;

  @Operation(summary = "알림 목록 조회", description = "사용자의 최신 알림 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
      @AuthenticationPrincipal Long userId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Integer size) {
    NotificationListResponse response = notificationService.getNotifications(userId, cursor, size);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(summary = "알림 읽음 처리", description = "사용자의 알림을 읽음 처리합니다.")
  @PatchMapping("/{notificationId}/read")
  public ResponseEntity<ApiResponse<NotificationReadResponse>> readNotification(
      @AuthenticationPrincipal Long userId, @PathVariable Long notificationId) {
    NotificationReadResponse response =
        notificationService.readNotification(userId, notificationId);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(summary = "알림 SSE", description = "알림 SSE 커넥션을 열고, SseEmitter를 반환합니다.")
  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(
      @AuthenticationPrincipal Long userId,
      @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse) {
    SecurityContextUtil.propagateSecurityContextToRequest(httpServletRequest, httpServletResponse);
    return notificationSseService.subscribe(userId, lastEventId);
  }
}
