package com.moa.moa_server.domain.user.service;

import com.moa.moa_server.domain.auth.entity.OAuth;
import com.moa.moa_server.domain.auth.handler.AuthErrorCode;
import com.moa.moa_server.domain.auth.handler.AuthException;
import com.moa.moa_server.domain.auth.repository.OAuthRepository;
import com.moa.moa_server.domain.auth.repository.TokenRepository;
import com.moa.moa_server.domain.auth.service.AuthService;
import com.moa.moa_server.domain.global.cursor.GroupNameGroupIdCursor;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.service.GroupService;
import com.moa.moa_server.domain.user.dto.request.UserUpdateRequest;
import com.moa.moa_server.domain.user.dto.response.*;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.user.util.UserValidator;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final OAuthRepository oauthRepository;
    private final TokenRepository tokenRepository;
    private final VoteRepository voteRepository;

    private final GroupService groupService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public GroupLabelResponse getJoinedGroupLabels(Long userId, @Nullable String cursor, @Nullable Integer size) {
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
        GroupNameGroupIdCursor parsedCursor = cursor != null ? GroupNameGroupIdCursor.parse(cursor) : null;

        // 유저 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 그룹 목록 조회
        List<Group> groups = new LinkedList<>(groupMemberRepository.findJoinedGroupLabels(user, parsedCursor, pageSize + 1));

        // 첫 페이지인 경우 공개 그룹을 제일 앞에 추가
        if (cursor == null) {
            Group publicGroup = groupService.getPublicGroup();
            groups.addFirst(publicGroup);
        }

        // 응답 구성
        boolean hasNext = groups.size() > pageSize;
        if (hasNext) groups = groups.subList(0, pageSize);

        String nextCursor = groups.isEmpty() ? null :
                new GroupNameGroupIdCursor(groups.getLast().getName(), groups.getLast().getId()).encode();

        List<GroupLabel> labels = groups.stream()
                .map(GroupLabel::from)
                .toList();

        return new GroupLabelResponse(labels, nextCursor, hasNext, labels.size());
    }

    @Transactional(readOnly = true)
    public JoinedGroupResponse getJoinedGroups(Long userId, @Nullable String cursor, @Nullable Integer size) {
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
        GroupNameGroupIdCursor parsedCursor = cursor != null ? GroupNameGroupIdCursor.parse(cursor) : null;

        // 유저 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 그룹 멤버 목록 조회
        List<GroupMember> members = groupMemberRepository.findJoinedGroups(user, parsedCursor, pageSize + 1);

        // 응답 구성
        boolean hasNext = members.size() > pageSize;
        if (hasNext) members = members.subList(0, pageSize);

        String nextCursor = members.isEmpty() ? null :
                new GroupNameGroupIdCursor(
                        members.getLast().getGroup().getName(),
                        members.getLast().getGroup().getId()
                ).encode();

        List<GroupDetail> groups = members.stream()
                .map(GroupDetail::from)
                .toList();

        return new JoinedGroupResponse(groups, nextCursor, hasNext, groups.size());
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        return UserInfoResponse.from(user.getNickname());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserUpdateResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        String newNickname = request.nickname();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 닉네임 유효성 검사
        UserValidator.validateNickname(newNickname);

        // 동일 닉네임이면 중복 처리 없이 바로 반환
        if (user.getNickname().equals(newNickname)) {
            return new UserUpdateResponse(user.getNickname());
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(newNickname)) {
            throw new UserException(UserErrorCode.DUPLICATED_NICKNAME);
        }

        // 닉네임 변경
        user.updateNickname(newNickname);
        return new UserUpdateResponse(newNickname);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        // 1. 그룹 소유자 승계
        groupService.reassignOrDeleteGroupsOwnedBy(user);

        // 2. 그룹 멤버 삭제 (hard delete)
        groupMemberRepository.hardDeleteAllByUserId(userId);

        // 3. 유저가 생성한 투표 삭제 (soft delete)
        voteRepository.softDeleteAllByUser(user);

        // 4. 카카오 연동 해제
        String kakaoUserId = oauthRepository.findByUser(user)
                .filter(oauth -> oauth.getProviderCode() == OAuth.ProviderCode.KAKAO)
                .map(OAuth::getId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.OAUTH_NOT_FOUND));
        try {
            Long kakaoUserIdLong = Long.parseLong(kakaoUserId);
            authService.unlinkKakaoAccount(kakaoUserIdLong);
        } catch (NumberFormatException e) {
            throw new AuthException(AuthErrorCode.INVALID_KAKAO_ID);
        }

        // 5. 토큰, OAuth 삭제 (hard delete)
        tokenRepository.deleteByUserId(userId);
        oauthRepository.deleteByUserId(userId);

        // 6. 회원 상태 변경 (soft delete)
        user.withdraw();
    }
}