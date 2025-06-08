package com.moa.moa_server.domain.global.initializer;

import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class SystemDataInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  @Override
  public void run(ApplicationArguments args) {
    if (!userRepository.existsById(1L)) {
      throw new IllegalStateException("System user not found. Please execute init-data.sql.");
    }
    if (!groupRepository.existsById(1L)) {
      throw new IllegalStateException("Public group not found. Please execute init-data.sql.");
    }
  }
}
