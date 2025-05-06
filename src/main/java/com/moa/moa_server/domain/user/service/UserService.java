package com.moa.moa_server.domain.user.service;

import com.moa.moa_server.domain.global.cursor.GroupNameGroupIdCursor;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.service.GroupService;
import com.moa.moa_server.domain.user.dto.response.GroupLabel;
import com.moa.moa_server.domain.user.dto.response.GroupLabelResponse;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    private final GroupService groupService;

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
}
