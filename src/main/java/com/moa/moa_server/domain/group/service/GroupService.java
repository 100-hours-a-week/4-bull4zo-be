package com.moa.moa_server.domain.group.service;

import com.moa.moa_server.domain.group.dto.request.GroupCreateRequest;
import com.moa.moa_server.domain.group.dto.request.GroupJoinRequest;
import com.moa.moa_server.domain.group.dto.response.GroupCreateResponse;
import com.moa.moa_server.domain.group.dto.response.GroupJoinResponse;
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
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupService {

    private static final int MAX_INVITE_CODE_RETRY = 10;

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public Group getPublicGroup() {
        return groupRepository.findById(1L)
                .orElseThrow(() -> new VoteException(VoteErrorCode.GROUP_NOT_FOUND));
    }

    @Transactional
    public GroupJoinResponse joinGroup(Long userId, GroupJoinRequest request) {
        String inviteCode = request.inviteCode().trim().toUpperCase();

        // 초대 코드 형식 검증
        GroupValidator.validateInviteCode(inviteCode);

        // 유저 조회 및 상태 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 초대 코드로 그룹 조회
        // deletedAt IS NULL 조건은 Group 엔티티의 @Where에서 자동으로 적용됨
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new GroupException(GroupErrorCode.INVITE_CODE_NOT_FOUND));

        // 공개 그룹은 가입 불가
        if (group.isPublicGroup()) {
            throw new GroupException(GroupErrorCode.CANNOT_JOIN_PUBLIC_GROUP);
        }

        // 가입 이력 조회
        Optional<GroupMember> memberOpt = groupMemberRepository.findByGroupAndUserIncludingDeleted(group.getId(), user.getId());

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

    @Transactional
    public GroupCreateResponse createGroup(Long userId, GroupCreateRequest request) {
        // 유저 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 입력 검증
        GroupValidator.validateGroupName(request.name());
        GroupValidator.validateDescription(request.description());
        if (!request.imageUrl().isBlank()) {
            GroupValidator.validateImageUrl(request.imageUrl()); // 업로드 도메인 검증
        }
        String imageUrl = request.imageUrl().isBlank() ? null : request.imageUrl().trim();

        // 그룹 이름 중복 검사
        if (groupRepository.existsByName(request.name())) {
            throw new GroupException(GroupErrorCode.DUPLICATE_NAME);
        }

        // 초대 코드 생성
        String inviteCode = generateUniqueInviteCode();

        // 그룹 생성
        Group group = Group.create(user, request.name(), request.description(), imageUrl, inviteCode);
        groupRepository.save(group);

        // 그룹 멤버 등록
        GroupMember member = GroupMember.createAsOwner(user, group);
        groupMemberRepository.save(member);

        return new GroupCreateResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getImageUrl(),
                group.getInviteCode(),
                group.getCreatedAt()
        );
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
}
