package com.moa.moa_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoaServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(MoaServerApplication.class, args);
  }
}
