package com.moa.moa_server.domain.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
  private static ObjectMapper objectMapper = new ObjectMapper();

  public static String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON 직렬화 실패", e);
    }
  }
}
