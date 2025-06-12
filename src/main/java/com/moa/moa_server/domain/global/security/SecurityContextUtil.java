package com.moa.moa_server.domain.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

/**
 * 비동기 컨트롤러에서 인증 정보를 수동으로 전파하기 위한 헬퍼 메서드. DeferredResult, @Async 등에서 SecurityContext가 끊기는 문제를 방지함.
 */
public class SecurityContextUtil {

  public static void propagateSecurityContextToRequest(
      HttpServletRequest req, HttpServletResponse res) {
    // 현재 스레드의 SecurityContext에서 인증 정보 추출
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      // 새로운 SecurityContext에 인증 정보 복사
      SecurityContext newContext = SecurityContextHolder.createEmptyContext();
      newContext.setAuthentication(auth);

      // Request에 SecurityContext 저장하여 이후 비동기 흐름에서 인증 정보 유지
      new RequestAttributeSecurityContextRepository().saveContext(newContext, req, res);
    }
  }
}
