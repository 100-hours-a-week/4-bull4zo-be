package com.moa.moa_server.config.security;

public class SecurityConstants {
  public static final String[] ALLOWED_URLS = {
    "/api/v1/auth/login/oauth",
    "/api/v1/auth/token/refresh",
    "/api/v1/test/**",
    "/api/v1/ai/votes/moderation/callback",
    "/swagger-ui/**",
    "v3/api-docs/**",
    "/swagger-resources/**",
    "/webjars/**",
    "/test-login"
  };
}
