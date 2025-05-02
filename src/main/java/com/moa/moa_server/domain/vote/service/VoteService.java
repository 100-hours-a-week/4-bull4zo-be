package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.dto.request.VoteCreateRequest;
import com.moa.moa_server.domain.vote.entity.Vote;
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

    @Transactional
    public Long createVote(Long userId, VoteCreateRequest request) {
        // 유저 조회 및 유효성 검사
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        AuthUserValidator.validateActive(user);

        // 그룹 조회 및 멤버십 확인
        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new RuntimeException("GROUP_NOT_FOUND"));

//        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
//            throw new RuntimeException("NOT_GROUP_MEMBER");
//        }

        // 요청 값 유효성 검사
        VoteValidator.validateContent(request.content());
        VoteValidator.validateUrl(request.imageUrl());
        VoteValidator.validateClosedAt(request.closedAt());

        boolean adminVote = false;

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
}
