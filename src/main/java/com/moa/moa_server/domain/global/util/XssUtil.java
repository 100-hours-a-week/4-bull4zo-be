package com.moa.moa_server.domain.global.util;

import com.nhncorp.lucy.security.xss.XssFilter;

public class XssUtil {
  private static final XssFilter xssFilter = XssFilter.getInstance("lucy-xss-superset.xml", true);

  public static String sanitize(String input) {
    if (input == null) return null;
    String filtered = xssFilter.doFilter(input);

    // 위험한 태그 escape 형태 감지
    boolean hasDangerousTag =
        filtered.matches(
            "(?i).*&lt;\\s*/?\\s*(script|iframe|style|object|embed|img|meta|link|base)\\b[^&]*&gt;.*");

    if (hasDangerousTag) {
      // 위험한 태그가 있으면 그대로 escape 상태 유지
      return filtered;
    } else {
      // 위험한 태그 없으면 <, > 복원
      return filtered.replace("&lt;", "<").replace("&gt;", ">");
    }
  }
}
