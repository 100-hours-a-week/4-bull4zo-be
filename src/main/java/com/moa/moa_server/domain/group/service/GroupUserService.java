package com.moa.moa_server.domain.group.service;

import com.moa.moa_server.domain.group.dto.group_user.GroupJoinRequest;
import com.moa.moa_server.domain.group.dto.group_user.GroupJoinResponse;
import com.moa.moa_server.domain.group.dto.group_user.GroupLeaveResponse;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.group.handler.GroupErrorCode;
import com.moa.moa_server.domain.group.handler.GroupException;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.group.util.GroupValidator;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 그룹 사용자 서비스.
 *
 * <p>그룹 내 사용자 행동(가입, 탈퇴 등)을 처리하는 서비스입니다. 그룹 자체의 생성, 삭제, 소유자 변경 등의 로직은 GroupService 에서 담당하며, 이 클래스는
 * 개별 사용자의 그룹 참여 관련 행위를 책임집니다.
 */
@Service
@RequiredArgsConstructor
public class GroupUserService {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final GroupMemberRepository groupMemberRepository;

  /** 그룹 가입 */
  @Transactional
  public GroupJoinResponse joinGroup(Long userId, GroupJoinRequest request) {
    String inviteCode = request.inviteCode().trim().toUpperCase();

    // 초대 코드 형식 검증
    GroupValidator.validateInviteCode(inviteCode);

    // 유저 조회 및 상태 확인
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    // 초대 코드로 그룹 조회
    // deletedAt IS NULL 조건은 Group 엔티티의 @Where에서 자동으로 적용됨
    Group group =
        groupRepository
            .findByInviteCode(inviteCode)
            .orElseThrow(() -> new GroupException(GroupErrorCode.INVITE_CODE_NOT_FOUND));

    // 공개 그룹은 가입 불가
    if (group.isPublicGroup()) {
      throw new GroupException(GroupErrorCode.CANNOT_JOIN_PUBLIC_GROUP);
    }

    // 가입 이력 조회
    Optional<GroupMember> memberOpt =
        groupMemberRepository.findByGroupAndUserIncludingDeleted(group.getId(), user.getId());

    GroupMember member;
    if (memberOpt.isPresent()) {
      // 가입 이력이 있는 경우
      member = memberOpt.get();

      if (member.isActive()) { // 이미 가입 상태
        throw new GroupException(GroupErrorCode.ALREADY_JOINED);
      } else { // 탈퇴 상태 → 복구
        member.rejoin();
      }
    } else {
      // 가입 이력이 없는 경우
      member = GroupMember.create(user, group);
      groupMemberRepository.save(member);
    }

    return new GroupJoinResponse(group.getId(), group.getName(), member.getRole().name());
  }

  /** 그룹 탈퇴 */
  @Transactional
  public GroupLeaveResponse leaveGroup(Long userId, Long groupId) {
    // 그룹 존재 확인
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));

    // 멤버 여부 확인
    GroupMember member =
        groupMemberRepository
            .findByGroupAndUserIncludingDeleted(groupId, userId)
            .orElseThrow(() -> new GroupException(GroupErrorCode.MEMBERSHIP_NOT_FOUND));

    // 소유자는 탈퇴 불가
    if (member.getRole() == GroupMember.Role.OWNER) {
      throw new GroupException(GroupErrorCode.OWNER_CANNOT_LEAVE);
    }

    // 탈퇴 처리 (soft delete)
    member.leave();

    return new GroupLeaveResponse(groupId);
  }
}
