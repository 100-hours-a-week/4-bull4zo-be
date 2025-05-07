package com.moa.moa_server.domain.group.repository;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long>, GroupMemberRepositoryCustom {

    // 탈퇴하지 않은 그룹 멤버 조회 (@Where(clause = "deleted_at IS NULL")가 적용되어 GroupMember가 삭제되지 않은 경우만 조회됨)
    Optional<GroupMember> findByGroupAndUser(Group group, User user);

    // soft delete된 멤버도 포함하여 조회
    @Query(value = "SELECT * FROM group_member WHERE group_id = :groupId AND user_id = :userId", nativeQuery = true)
    Optional<GroupMember> findByGroupAndUserIncludingDeleted(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Query("SELECT gm.group FROM GroupMember gm WHERE gm.user = :user AND gm.deletedAt IS NULL")
    List<Group> findAllActiveGroupsByUser(@Param("user") User user);

    void deleteAllByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM GroupMember gm WHERE gm.user.id = :userId")
    void hardDeleteAllByUserId(@Param("userId") Long userId);

    List<GroupMember> findAllByGroupOrderByJoinedAtAsc(Group group);
}
