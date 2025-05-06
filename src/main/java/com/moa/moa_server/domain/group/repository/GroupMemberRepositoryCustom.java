package com.moa.moa_server.domain.group.repository;

import com.moa.moa_server.domain.global.cursor.GroupNameGroupIdCursor;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.user.entity.User;
import jakarta.annotation.Nullable;

import java.util.List;

public interface GroupMemberRepositoryCustom {
    List<Group> findJoinedGroupLabels(User user, @Nullable GroupNameGroupIdCursor cursor, int size);
}
