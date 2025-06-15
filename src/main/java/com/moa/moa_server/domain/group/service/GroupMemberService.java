package com.moa.moa_server.domain.group.service;

import com.moa.moa_server.domain.group.dto.group_member.ChangeRoleResponse;
import com.moa.moa_server.domain.group.dto.group_member.MemberDeleteResponse;
import com.moa.moa_server.domain.group.dto.group_member.MemberItem;
import com.moa.moa_server.domain.group.dto.group_member.MemberListResponse;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.group.handler.GroupErrorCode;
import com.moa.moa_server.domain.group.handler.GroupException;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 그룹 멤버 서비스.
 *
 * <p>그룹 멤버 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class GroupMemberService {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final GroupMemberRepository groupMemberRepository;

  /** 멤버 목록 조회 */
  @Transactional(readOnly = true)
  public MemberListResponse getMemberList(Long userId, Long groupId) {
    // 유저 조회 및 검증
    User user = findActiveUser(userId);

    // 그룹 존재 여부 확인
    Group group = findGroup(groupId);

    // 권한 검사: 그룹 멤버이면 조회 가능
    GroupMember member =
        groupMemberRepository
            .findByGroupAndUser(group, user)
            .orElseThrow(() -> new GroupException(GroupErrorCode.FORBIDDEN));

    // 전체 그룹 멤버 조회
    List<GroupMember> members = groupMemberRepository.findAllByGroup(group);

    List<MemberItem> items =
        members.stream()
            .filter(GroupMember::isActiveUser)
            .sorted(
                Comparator.comparingInt(
                        (GroupMember m) -> m.getRole().ordinal()) // OWNER -> MANAGER -> MEMBER 순 정렬
                    .thenComparing(
                        m -> m.getUser().getNickname(), Comparator.naturalOrder()) // 닉네임 오름차순 정렬
                )
            .map(
                m ->
                    new MemberItem(
                        m.getUser().getId(), m.getUser().getNickname(), m.getRole().name()))
            .toList();

    return new MemberListResponse(groupId, items);
  }

  /** 멤버 역할 변경 */
  @Transactional
  public ChangeRoleResponse changeRole(
      Long requesterId, Long groupId, Long targetUserId, String newRoleStr) {
    User requester = findActiveUser(requesterId);
    Group group = findGroup(groupId);

    // 요청자의 멤버십 조회 및 권한 검사
    GroupMember requesterMember =
        groupMemberRepository
            .findByGroupAndUser(group, requester)
            .orElseThrow(() -> new GroupException(GroupErrorCode.FORBIDDEN));
    if (!requesterMember.isOwnerOrManager()) { // 그룹 소유자, 관리자만 가능
      throw new GroupException(GroupErrorCode.FORBIDDEN);
    }

    // 대상 사용자 조회
    User targetUser = findActiveUser(targetUserId);
    GroupMember targetMember =
        groupMemberRepository
            .findByGroupAndUser(group, targetUser)
            .orElseThrow(() -> new GroupException(GroupErrorCode.MEMBERSHIP_NOT_FOUND));

    // 역할 파싱 및 유효성 검사
    GroupMember.Role newRole =
        GroupMember.Role.from(newRoleStr)
            .orElseThrow(() -> new GroupException(GroupErrorCode.INVALID_ROLE_NAME));

    GroupMember.Role requesterRole = requesterMember.getRole();
    GroupMember.Role targetRole = targetMember.getRole();

    // 동일 역할이면 무시
    if (targetRole == newRole) {
      return new ChangeRoleResponse(targetUserId, newRole.name());
    }

    // 자기 자신 역할 변경 제약
    if (requester.getId().equals(targetUserId)) {
      // 소유자는 자기 자신 역할 변경 불가
      if (requesterRole == GroupMember.Role.OWNER) {
        throw new GroupException(GroupErrorCode.FORBIDDEN);
      }

      // 관리자는 자기 자신을 멤버로 변경 가능 (단, role == MANAGER → MEMBER)
      if (requesterRole == GroupMember.Role.MANAGER) {
        if (newRole != GroupMember.Role.MEMBER) {
          throw new GroupException(GroupErrorCode.FORBIDDEN);
        }
        requesterMember.changeRole(GroupMember.Role.MEMBER);
        return new ChangeRoleResponse(targetUserId, newRole.name());
      }

      // 기타 자기 자신 변경은 차단
      throw new GroupException(GroupErrorCode.FORBIDDEN);
    }

    // 관리자: 일반 멤버 -> 관리자로 변경 가능 (소유자나 다른 관리자의 역할 변경 불가능)
    if (requesterRole == GroupMember.Role.MANAGER) {
      if (targetRole != GroupMember.Role.MEMBER || newRole != GroupMember.Role.MANAGER) {
        throw new GroupException(GroupErrorCode.FORBIDDEN);
      }
      targetMember.changeRole(newRole);
      return new ChangeRoleResponse(targetUserId, newRole.name());
    }

    // 소유자: 모든 멤버의 역할 변경 가능
    if (newRole == GroupMember.Role.OWNER) {
      // 다른 멤버를 소유자로 변경하는 경우, 자신은 관리자로 변경
      targetMember.changeRole(GroupMember.Role.OWNER);
      requesterMember.changeRole(GroupMember.Role.MANAGER);
      group.changeOwner(targetUser);
    } else {
      targetMember.changeRole(newRole);
    }

    return new ChangeRoleResponse(targetUserId, newRole.name());
  }

  /** 멤버 내보내기 */
  @Transactional
  public MemberDeleteResponse deleteMember(Long requesterId, Long groupId, Long targetUserId) {
    User requester = findActiveUser(requesterId);
    Group group = findGroup(groupId);

    // 요청자 멤버십 및 권한 확인
    GroupMember requesterMember =
        groupMemberRepository
            .findByGroupAndUser(group, requester)
            .orElseThrow(() -> new GroupException(GroupErrorCode.FORBIDDEN));
    if (requesterMember.getRole() != GroupMember.Role.OWNER) { // 소유자만 권한 있음
      throw new GroupException(GroupErrorCode.FORBIDDEN);
    }

    // 대상 사용자 조회
    if (requesterId.equals(targetUserId)) {
      throw new GroupException(GroupErrorCode.CANNOT_KICK_SELF); // 자기 자신(소유자)은 불가
    }

    User targetUser = findActiveUser(targetUserId);

    GroupMember targetMember =
        groupMemberRepository
            .findByGroupAndUser(group, targetUser)
            .orElseThrow(() -> new GroupException(GroupErrorCode.MEMBERSHIP_NOT_FOUND));

    // 실제 추방 처리 (Soft Delete)
    targetMember.leave();

    return new MemberDeleteResponse(targetUserId);
  }

  private User findActiveUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);
    return user;
  }

  private Group findGroup(Long groupId) {
    return groupRepository
        .findById(groupId)
        .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));
  }
}
