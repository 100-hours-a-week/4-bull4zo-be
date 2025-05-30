package com.moa.moa_server.domain.global.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class XssUtilTest {
  @Test
  @DisplayName("일반 기호는 그대로 출력됨")
  void testNormalSymbolsRemain() {
    String input = "<<배고픈 사람>>";
    String result = XssUtil.sanitize(input);
    assertEquals("<<배고픈 사람>>", result);
  }

  @Test
  @DisplayName("script 태그는 escape 되어야 함")
  void testScriptTagFiltered() {
    String input = "<script>alert('XSS')</script>";
    String result = XssUtil.sanitize(input);
    assertTrue(result.contains("&lt;script&gt;"));
    assertFalse(result.contains("<script>"));
  }

  @Test
  @DisplayName("iframe 태그도 escape 되어야 함")
  void testIframeTagFiltered() {
    String input = "<iframe src='evil.com'></iframe>";
    String result = XssUtil.sanitize(input);
    assertTrue(result.contains("&lt;iframe"));
  }

  @Test
  @DisplayName("HTML 태그 없이 부등호만 있을 경우 복원")
  void testNoTagButHasAngleBrackets() {
    String input = "이쪽 >> 저쪽";
    String result = XssUtil.sanitize(input);
    assertEquals("이쪽 >> 저쪽", result);
  }

  @Test
  @DisplayName("HTML 속성 삽입 시도 방지")
  void testAttributeInjection() {
    String input = "<img src='javascript:alert(1)'>";
    String result = XssUtil.sanitize(input);
    assertTrue(result.contains("javascript"));
    assertTrue(result.contains("&lt;img")); // 태그 자체는 escape
  }
}
