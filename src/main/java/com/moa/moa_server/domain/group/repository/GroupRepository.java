package com.moa.moa_server.domain.group.repository;

import com.moa.moa_server.domain.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
