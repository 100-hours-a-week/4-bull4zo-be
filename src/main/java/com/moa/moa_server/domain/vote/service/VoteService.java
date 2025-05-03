package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.dto.request.VoteCreateRequest;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.util.VoteValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public Long createVote(Long userId, VoteCreateRequest request) {
        // 유저 조회 및 유효성 검사
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 그룹 조회
        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new VoteException(VoteErrorCode.GROUP_NOT_FOUND));

        // 멤버십 확인
        GroupMember groupMember = validateGroupMembership(user, group);

        boolean adminVote = groupMember != null && switch (groupMember.getRole()) {
            case OWNER, MANAGER -> true;
            default -> false;
        };

        // 요청 값 유효성 검사
        VoteValidator.validateContent(request.content());
        VoteValidator.validateUrl(request.imageUrl());
        VoteValidator.validateClosedAt(request.closedAt());

        // Vote 생성 및 저장
        Vote vote = Vote.createUserVote(
                user,
                group,
                request.content(),
                request.imageUrl(),
                request.closedAt(),
                adminVote
        );

        voteRepository.save(vote);

        return vote.getId();
    }

    private GroupMember validateGroupMembership(User user, Group group) {
        if (group.isPublicGroup()) return null;

        return groupMemberRepository
                .findByGroupAndUserIncludingDeleted(group, user)
                .filter(GroupMember::isActive)
                .orElseThrow(() -> new VoteException(VoteErrorCode.NOT_GROUP_MEMBER));
    }
}
