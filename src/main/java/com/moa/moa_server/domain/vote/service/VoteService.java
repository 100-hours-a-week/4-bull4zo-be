package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.global.cursor.VoteClosedCursor;
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
import com.moa.moa_server.domain.vote.dto.request.VoteSubmitRequest;
import com.moa.moa_server.domain.vote.dto.response.VoteDetailResponse;
import com.moa.moa_server.domain.vote.dto.response.list.VoteListItem;
import com.moa.moa_server.domain.vote.dto.response.list.VoteListResponse;
import com.moa.moa_server.domain.vote.dto.response.result.VoteOptionResult;
import com.moa.moa_server.domain.vote.dto.response.result.VoteResultResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteRepositoryCustom;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import com.moa.moa_server.domain.vote.util.VoteValidator;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class VoteService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_UNAUTHENTICATED_PAGE_SIZE = 3;

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
    public void submitVote(Long userId, Long voteId, VoteSubmitRequest request) {
        // 유저 조회 및 유효성 검사
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 응답 값 유효성 검증
        int response = request.userResponse();
        if (response < 0 || response > 2) {
            throw new VoteException(VoteErrorCode.INVALID_OPTION);
        }

        // 투표 조회
        Vote vote = findVoteOrThrow(voteId);

        // 투표 상태 체크
        if (!vote.isOpen()) {
            throw new VoteException(VoteErrorCode.VOTE_NOT_OPENED);
        }

        // 투표 권한 조회
        Group group = vote.getGroup();
        if (!group.isPublicGroup()) {
            boolean isGroupMember = groupMemberRepository
                    .findByGroupAndUserIncludingDeleted(group, user)
                    .filter(m -> m.getDeletedAt() == null)
                    .isPresent();

            if (!isGroupMember) {
                throw new VoteException(VoteErrorCode.NOT_GROUP_MEMBER);
            }
        }

        // 중복 투표 확인
        if (voteResponseRepository.existsByVoteAndUser(vote, user)) {
            throw new VoteException(VoteErrorCode.ALREADY_VOTED);
        }

        // 투표 응답 저장
        VoteResponse voteResponse = VoteResponse.create(vote, user, response);
        try {
            voteResponseRepository.save(voteResponse);
        } catch (DataIntegrityViolationException e) {
            throw new VoteException(VoteErrorCode.ALREADY_VOTED);
        }
    }

    @Transactional
    public VoteDetailResponse getVoteDetail(Long userId, Long voteId) {
        // 유저 조회 및 유효성 검사
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 투표 조회
        Vote vote = findVoteOrThrow(voteId);

        // 상태/권한 검사
        validateVoteReadable(vote);
        validateVoteAccess(user, vote);

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

    @Transactional
    public VoteResultResponse getVoteResult(Long userId, Long voteId) {
        // 유저 조회 및 유효성 검사
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 투표 조회
        Vote vote = findVoteOrThrow(voteId);

        // 상태/권한 검사
        validateVoteReadable(vote);
        validateVoteAccess(user, vote);

        // 사용자 응답 조회 (없으면 null)
        Optional<VoteResponse> userVoteResponse = voteResponseRepository.findByVoteAndUser(vote, user);
        Integer userResponse = userVoteResponse.map(VoteResponse::getOptionNumber).orElse(null);

        // 전체 응답 조회
        List<VoteResponse> responses = voteResponseRepository.findAllByVote(vote);
        int totalCount = (int) responses.stream()
                .filter(vr -> vr.getOptionNumber() > 0)
                .count();

        // 항목별 집계 (0: 기권 제외)
        Map<Integer, Long> countMap = responses.stream()
                .filter(vr -> vr.getOptionNumber() > 0)
                .collect(Collectors.groupingBy(
                        VoteResponse::getOptionNumber,
                        Collectors.counting()
                ));

        List<VoteOptionResult> results = List.of(1, 2).stream()
                .map(option -> {
                    int count = countMap.getOrDefault(option, 0L).intValue();
                    int ratio = totalCount == 0 ? 0 : (int) ((count * 100.0) / totalCount);
                    return new VoteOptionResult(option, count, ratio);
                })
                .collect(Collectors.toList());

        return new VoteResultResponse(
                voteId,
                userResponse,
                totalCount,
                results
        );
    }

    @Transactional(readOnly = true)
    public VoteListResponse getActiveVotes(@Nullable Long userId, @Nullable Integer groupId, @Nullable String cursor, @Nullable Integer size) {
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
        VoteClosedCursor parsedCursor = cursor != null ? VoteClosedCursor.parse(cursor) : null;

        // 비로그인 요청
        if (userId == null) {
            // 공개 그룹만 허용 (groupId가 1이 아닌 경우 거절)
            Long targetGroupId = (groupId != null) ? groupId.longValue() : 1L;
            Group group = groupRepository.findById(targetGroupId)
                    .orElseThrow(() -> new VoteException(VoteErrorCode.GROUP_NOT_FOUND));
            if (!group.isPublicGroup()) {
                throw new VoteException(VoteErrorCode.FORBIDDEN);
            }

            List<Vote> votes = voteRepository.findActiveVotes(
                    List.of(group), parsedCursor, null, DEFAULT_UNAUTHENTICATED_PAGE_SIZE);
            List<VoteListItem> items = votes.stream().map(VoteListItem::from).toList();
            return new VoteListResponse(items, null, false, items.size());
        }
        // 로그인 사용자 요청
        else {
            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
            AuthUserValidator.validateActive(user);

            // 그룹 목록 수집 및 권한 검사
            List<Group> accessibleGroups = getAccessibleGroups(user, groupId);

            // 투표 목록 조회
            List<Vote> votes = voteRepository.findActiveVotes(accessibleGroups, parsedCursor, user, pageSize + 1);

            // 응답 구성
            boolean hasNext = votes.size() > pageSize;
            if (hasNext) votes = votes.subList(0, pageSize);

            String nextCursor = votes.isEmpty() ? null :
                    new VoteClosedCursor(votes.getLast().getClosedAt(), votes.getLast().getCreatedAt()).encode();

            List<VoteListItem> items = votes.stream().map(VoteListItem::from).toList();
            return new VoteListResponse(items, nextCursor, hasNext, items.size());
        }
    }

    /**
     * 투표 조회
     */
    private Vote findVoteOrThrow(Long voteId) {
        return voteRepository.findById(voteId)
                .orElseThrow(() -> new VoteException(VoteErrorCode.VOTE_NOT_FOUND));
    }

    /**
     * 그룹에 소속된 유저인지 검사 (등록/참여에 사용)
     */
    private GroupMember validateGroupMembership(User user, Group group) {
        if (group.isPublicGroup()) return null;

        return groupMemberRepository
                .findByGroupAndUserIncludingDeleted(group, user)
                .filter(GroupMember::isActive)
                .orElseThrow(() -> new VoteException(VoteErrorCode.NOT_GROUP_MEMBER));
    }

    /**
     * 투표 조회 가능한 상태인지 검사 (내용, 결과, 댓글 읽기)
     * 허용 상태: OPEN, CLOSED
     */
    private void validateVoteReadable(Vote vote) {
        if (vote.getVoteStatus() != Vote.VoteStatus.OPEN &&
                vote.getVoteStatus() != Vote.VoteStatus.CLOSED) {
            throw new VoteException(VoteErrorCode.FORBIDDEN);
        }
    }

    /**
     * 투표 조회 권한 검사 (내용, 결과, 댓글 읽기)
     * 조건: 등록자이거나 참여자(기권 불가)여야 함
     * * top3 투표는 추후 그룹 멤버 여부로도 허용 예정
     */
    private void validateVoteAccess(User user, Vote vote) {
        if (isVoteAuthor(user, vote)) return;
        if (hasParticipatedWithValidOption(user, vote)) return;

        // TODO: top3 투표일 경우 isGroupMember 검사 후 허용
        throw new VoteException(VoteErrorCode.FORBIDDEN);
    }

    private boolean isVoteAuthor(User user, Vote vote) {
        return vote.getUser().equals(user);
    }

    private boolean hasParticipatedWithValidOption(User user, Vote vote) {
        return voteResponseRepository.findByVoteAndUser(vote, user)
                .map(vr -> vr.getOptionNumber() > 0)
                .orElse(false);
    }

    /**
     * 접근 가능한 그룹 목록 조회 (투표 리스트 조회에 사용)
     * - groupId가 지정된 경우: 해당 그룹만 조회 (권한 확인 포함)
     * - groupId가 없는 경우: 공개 그룹 + 사용자가 속한 모든 그룹 반환
     */
    public List<Group> getAccessibleGroups(User user, @Nullable Integer groupId) {
        if (groupId != null) {
            // 단일 그룹만 조회 (권한 확인 포함)
            Group group = groupRepository.findById(groupId.longValue())
                    .orElseThrow(() -> new VoteException(VoteErrorCode.GROUP_NOT_FOUND));

            if (!group.isPublicGroup()) {
                groupMemberRepository.findByGroupAndUserIncludingDeleted(group, user)
                        .filter(GroupMember::isActive)
                        .orElseThrow(() -> new VoteException(VoteErrorCode.FORBIDDEN));
            }

            return List.of(group);
        }

        // 전체 그룹 조회: 공개 그룹 + 사용자의 그룹
        Group publicGroup = groupRepository.findById(1L)
                .orElseThrow(() -> new VoteException(VoteErrorCode.GROUP_NOT_FOUND));
        List<Group> userGroups = groupMemberRepository.findAllActiveGroupsByUser(user);

        return Stream.concat(Stream.of(publicGroup), userGroups.stream()).distinct().toList();
    }
}
