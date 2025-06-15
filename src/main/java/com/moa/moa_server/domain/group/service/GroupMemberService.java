package com.moa.moa_server.domain.group.service;

import com.moa.moa_server.domain.group.dto.response.MemberItem;
import com.moa.moa_server.domain.group.dto.response.MemberListResponse;
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
