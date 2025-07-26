package com.moa.moa_server.domain.group.service;

import com.moa.moa_server.domain.global.util.XssUtil;
import com.moa.moa_server.domain.group.dto.group_manage.GroupCreateRequest;
import com.moa.moa_server.domain.group.dto.group_manage.GroupCreateResponse;
import com.moa.moa_server.domain.group.dto.group_manage.GroupDeleteResponse;
import com.moa.moa_server.domain.group.dto.group_manage.GroupInfoResponse;
import com.moa.moa_server.domain.group.dto.group_manage.GroupUpdateRequest;
import com.moa.moa_server.domain.group.dto.group_manage.GroupUpdateResponse;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.group.handler.GroupErrorCode;
import com.moa.moa_server.domain.group.handler.GroupException;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.group.util.GroupValidator;
import com.moa.moa_server.domain.image.model.ImageProcessResult;
import com.moa.moa_server.domain.image.service.ImageService;
import com.moa.moa_server.domain.notification.application.producer.GroupNotificationProducerImpl;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.service.VoteCleanerService;
import com.moa.moa_server.domain.vote.service.VoteCommandService;
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
  private final VoteCommandService voteCommandService;
  private final GroupNotificationProducerImpl groupNotificationProducer;
  private final VoteCleanerService voteCleanerService;

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final GroupMemberRepository groupMemberRepository;

  /** 그룹 생성 */
  @Transactional
  public GroupCreateResponse createGroup(Long userId, GroupCreateRequest request) {
    // 유저 조회 및 검증
    User user = findActiveUser(userId);

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

  /** 그룹 삭제 */
  @Transactional
  public GroupDeleteResponse deleteGroup(Long userId, Long groupId) {
    // 유저 조회 및 검증
    User user = findActiveUser(userId);

    // 그룹 조회 및 권한 검사 (그룹 소유자만 가능)
    Group group = findGroup(groupId);
    if (!group.isOwnedBy(userId)) {
      throw new GroupException(GroupErrorCode.NOT_GROUP_OWNER);
    }

    // 그룹 soft delete
    group.softDelete();

    // 관련 데이터 처리
    voteCleanerService.deleteVoteByGroupId(groupId); // 투표

    // 알림 이벤트 발생
    groupNotificationProducer.notifyAllMembersGroupDeleted(group);

    return new GroupDeleteResponse(groupId);
  }

  /** 그룹 정보 조회 */
  @Transactional(readOnly = true)
  public GroupInfoResponse getGroupInfo(Long userId, Long groupId) {
    // 유저 조회 및 검증
    User user = findActiveUser(userId);
    // 그룹 존재 확인
    Group group = findGroup(groupId);

    // 멤버 여부 확인
    // 그룹 정보는 모든 멤버가 조회 가능
    GroupMember member =
        groupMemberRepository
            .findByGroupAndUserIncludingDeleted(groupId, userId)
            .orElseThrow(() -> new GroupException(GroupErrorCode.FORBIDDEN));

    return new GroupInfoResponse(group, member.getRole().name());
  }

  /** 그룹 정보 수정 */
  @Transactional
  public GroupUpdateResponse updateGroup(Long userId, Long groupId, GroupUpdateRequest request) {
    // 유저, 그룹 조회, 권한 확인
    User user = findActiveUser(userId);
    Group group = findGroup(groupId);
    GroupMember member = getAuthorizedMember(userId, groupId);

    // 그룹 이름, 소개 유효성 및 중복 검사
    validateGroupUpdate(request, group);

    // 초대 코드 생성
    if (request.changeInviteCode()) {
      String newCode = generateUniqueInviteCode();
      group.updateInviteCode(newCode);
    }

    // 이미지 처리
    ImageProcessResult imageResult = updateGroupImage(group, request);

    // 정보 업데이트
    group.updateInfo(
        sanitize(request.name()),
        sanitize(request.description()),
        imageResult.imageUrl(),
        imageResult.imageName());

    return GroupUpdateResponse.of(group, member.getRole());
  }

  /** 초대코드 생성 */
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

  private GroupMember getAuthorizedMember(Long userId, Long groupId) {
    GroupMember member =
        groupMemberRepository
            .findByGroupAndUserIncludingDeleted(groupId, userId)
            .orElseThrow(() -> new GroupException(GroupErrorCode.FORBIDDEN));
    if (!member.isActive() || !member.isOwnerOrManager()) {
      throw new GroupException(GroupErrorCode.FORBIDDEN);
    }
    return member;
  }

  private void validateGroupUpdate(GroupUpdateRequest request, Group group) {
    GroupValidator.validateGroupName(request.name());
    GroupValidator.validateDescription(request.description());

    String newName = sanitize(request.name());
    if (!group.getName().equals(newName) && groupRepository.existsByName(newName)) {
      throw new GroupException(GroupErrorCode.DUPLICATED_NAME);
    }
  }

  private ImageProcessResult updateGroupImage(Group group, GroupUpdateRequest request) {
    return imageService.processImageOnUpdate(
        "group",
        group.getImageUrl(),
        request.imageUrl(),
        group.getImageName(),
        request.imageName());
  }

  private String sanitize(String input) {
    return XssUtil.sanitize(input);
  }
}
