package com.moa.moa_server.domain.group.service;

import com.moa.moa_server.domain.group.dto.request.GroupJoinRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupService {

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
}
