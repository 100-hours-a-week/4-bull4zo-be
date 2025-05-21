package com.moa.moa_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate() {
    var factory = new HttpComponentsClientHttpRequestFactory();
    factory.setConnectTimeout(2000); // 연결 타임아웃 (2ms)
    factory.setReadTimeout(3000); // 응답 대기 타임아웃 (3ms)
    return new RestTemplate(factory);
  }
}
