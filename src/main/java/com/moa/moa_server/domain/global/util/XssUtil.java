package com.moa.moa_server.domain.global.util;

import com.nhncorp.lucy.security.xss.XssFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XssUtil {
  private static final XssFilter xssFilter = XssFilter.getInstance("lucy-xss-superset.xml");

  public static String sanitize(String input) {
    if (input == null) return null;
    return xssFilter.doFilter(input);
  }
}
