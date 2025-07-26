package com.moa.moa_server.domain.group.repository;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupRepository extends JpaRepository<Group, Long> {
  Optional<Group> findByInviteCode(String inviteCode);

  boolean existsByInviteCode(String inviteCode);

  boolean existsByName(String groupName);

  List<Group> findAllByUser(User user);

  @Query("select g.id from Group g")
  List<Long> findAllGroupIds();
}
