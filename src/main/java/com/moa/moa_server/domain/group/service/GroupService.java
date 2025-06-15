package com.moa.moa_server.domain.group.service;

import com.moa.moa_server.domain.global.util.XssUtil;
import com.moa.moa_server.domain.group.dto.request.GroupCreateRequest;
import com.moa.moa_server.domain.group.dto.request.GroupJoinRequest;
import com.moa.moa_server.domain.group.dto.response.GroupCreateResponse;
import com.moa.moa_server.domain.group.dto.response.GroupDeleteResponse;
import com.moa.moa_server.domain.group.dto.response.GroupJoinResponse;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.group.handler.GroupErrorCode;
import com.moa.moa_server.domain.group.handler.GroupException;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.group.util.GroupValidator;
import com.moa.moa_server.domain.image.service.ImageService;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.service.VoteService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

  private static final int MAX_INVITE_CODE_RETRY = 10;

  private final ImageService imageService;
  private final VoteService voteService;

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

  /** 그룹 생성 */
  @Transactional
  public GroupCreateResponse createGroup(Long userId, GroupCreateRequest request) {
    // 유저 조회 및 검증
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    // 입력 검증
    GroupValidator.validateGroupName(request.name());
    GroupValidator.validateDescription(request.description());
    imageService.validateImageUrl(request.imageUrl());
    String imageUrl = request.imageUrl().isBlank() ? null : request.imageUrl().trim();
    String imageName = request.imageName().isBlank() ? null : request.imageName().trim();

    // 그룹 이름 중복 검사
    if (groupRepository.existsByName(request.name())) {
      throw new GroupException(GroupErrorCode.DUPLICATED_NAME);
    }

    // 초대 코드 생성
    String inviteCode = generateUniqueInviteCode();

    // S3 이미지 이동
    if (imageUrl != null) {
      imageService.moveImageFromTempToTarget(imageUrl, "group");
      imageUrl = imageUrl.replace("/temp/", "/group/"); // DB에는 vote 경로 저장
      imageName = XssUtil.sanitize(request.imageName());
    }

    // 그룹 생성
    String sanitizedDescription = XssUtil.sanitize(request.description());
    Group group =
        Group.create(user, request.name(), sanitizedDescription, imageUrl, imageName, inviteCode);
    groupRepository.save(group);

    // 그룹 멤버 등록
    GroupMember member = GroupMember.createAsOwner(user, group);
    groupMemberRepository.save(member);

    return new GroupCreateResponse(
        group.getId(),
        group.getName(),
        group.getDescription(),
        group.getImageUrl(),
        group.getImageName(),
        group.getInviteCode(),
        group.getCreatedAt());
  }

  private String generateUniqueInviteCode() {
    for (int i = 0; i < MAX_INVITE_CODE_RETRY; i++) {
      String code = RandomStringUtils.randomAlphanumeric(6, 8).toUpperCase();
      if (!groupRepository.existsByInviteCode(code)) {
        return code;
      }
    }
    throw new GroupException(GroupErrorCode.INVITE_CODE_GENERATION_FAILED);
  }

  /** 그룹 소유권 승계 (그룹 소유자가 회원탈퇴 시 사용) */
  @Transactional
  public void reassignOrDeleteGroupsOwnedBy(User user) {
    // 사용자가 소유한 그룹 목록 조회
    List<Group> ownedGroups = groupRepository.findAllByUser(user);

    for (Group group : ownedGroups) {
      // 해당 그룹의 멤버 전체를 가입 순으로 조회
      List<GroupMember> members =
          groupMemberRepository.findAllByGroupOrderByJoinedAtAsc(group).stream()
              .filter(m -> !m.getUser().equals(user)) // 탈퇴한 사용자 제외
              .toList();

      // 1순위: 남아있는 활성 관리자(MANAGER) 중 가장 오래된 사용자에게 소유권 승계
      Optional<GroupMember> admin =
          members.stream()
              .filter(m -> m.getRole() == GroupMember.Role.MANAGER)
              .filter(GroupMember::isActiveUser)
              .findFirst();

      if (admin.isPresent()) {
        GroupMember newOwner = admin.get();
        newOwner.changeToOwner(); // 그룹 멤버 role 변경
        group.changeOwner(newOwner.getUser()); // 그룹의 소유자 변경
        continue;
      }

      // 2순위: 활성 일반 멤버(MEMBER) 중 가장 오래된 사용자에게 소유권 승계
      Optional<GroupMember> regularMember =
          members.stream()
              .filter(m -> m.getRole() == GroupMember.Role.MEMBER)
              .filter(GroupMember::isActiveUser)
              .findFirst();

      if (regularMember.isPresent()) {
        GroupMember newOwner = regularMember.get();
        newOwner.changeToOwner();
        group.changeOwner(newOwner.getUser());
        continue;
      }

      // 3순위: 그룹에 남은 활성 멤버가 없을 경우 그룹 자체를 소프트 삭제
      group.softDelete();
      log.info(
          "[GroupService#reassignOrDeleteGroupsOwnedBy] 활성 멤버가 없어 그룹 {}을(를) 삭제했습니다.",
          group.getId() != null ? group.getId() : "unknown");
    }
  }
}
