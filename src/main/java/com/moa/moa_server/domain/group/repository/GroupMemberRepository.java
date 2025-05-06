package com.moa.moa_server.domain.group.repository;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long>, GroupMemberRepositoryCustom {
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group AND gm.user = :user")
    Optional<GroupMember> findByGroupAndUserIncludingDeleted(Group group, User user);

    @Query("SELECT gm.group FROM GroupMember gm WHERE gm.user = :user AND gm.deletedAt IS NULL")
    List<Group> findAllActiveGroupsByUser(@Param("user") User user);
}
