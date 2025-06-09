package com.moa.moa_server.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtil {

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
