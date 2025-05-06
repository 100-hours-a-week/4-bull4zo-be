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

        // 공개 그룹은 패스

        // 유저 조회 및 상태 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 초대 코드로 그룹 조회
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new GroupException(GroupErrorCode.INVITE_CODE_NOT_FOUND));

        // 이미 가입 여부 확인 (deletedAt이 null 인지도 체크. null이면 패스. 아래에서 재가입)
        boolean alreadyJoined = groupMemberRepository.existsByUserAndGroupAndDeletedAtIsNull(user, group);
        if (alreadyJoined) {
            throw new GroupException(GroupErrorCode.ALREADY_JOINED);
        }

        // 그룹 멤버 생성
        GroupMember member = GroupMember.create(user, group);
        // 탈퇴 상태였으면 rejoin

        groupMemberRepository.save(member);

        return new GroupJoinResponse(group.getId(), group.getName(), member.getRole().name());
    }
}
