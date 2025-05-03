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
import com.moa.moa_server.domain.vote.dto.response.VoteDetailResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
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
    private final VoteResponseRepository voteResponseRepository;

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

        // 관리자 투표 여부 판단
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

    @Transactional
    public VoteDetailResponse getVoteDetail(Long userId, Long voteId) {
        // 유저 조회 및 유효성 검사
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 투표 조회
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new VoteException(VoteErrorCode.VOTE_NOT_FOUND));

        // 접근 권한 확인
        validateGroupAccess(user, vote);

        return new VoteDetailResponse(
                vote.getId(),
                vote.getGroup().getId(),
                vote.getUser().getNickname(),
                vote.getContent(),
                vote.getImageUrl(),
                vote.getCreatedAt(),
                vote.getClosedAt()
        );
    }

    /**
     * 그룹에 소속된 유저인지 검사 (등록/수정 등에 사용)
     */
    private GroupMember validateGroupMembership(User user, Group group) {
        if (group.isPublicGroup()) return null;

        return groupMemberRepository
                .findByGroupAndUserIncludingDeleted(group, user)
                .filter(GroupMember::isActive)
                .orElseThrow(() -> new VoteException(VoteErrorCode.NOT_GROUP_MEMBER));
    }

    /**
     * 그룹 조회 권한 검사 (읽기 접근에 사용)
     */
    private void validateGroupAccess(User user, Vote vote) {
        if (vote.getGroup().isPublicGroup()) return;

        if (isVoteAuthor(user, vote)) return;
        if (hasParticipated(user, vote)) return;

        // TODO: top3 투표일 경우 isGroupMember 검사 후 허용
        throw new VoteException(VoteErrorCode.FORBIDDEN);
    }

    private boolean isVoteAuthor(User user, Vote vote) {
        return vote.getUser().equals(user);
    }

    private boolean hasParticipated(User user, Vote vote) {
        return voteResponseRepository.existsByVoteAndUser(vote, user);
    }
}
