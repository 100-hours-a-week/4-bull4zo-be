package com.moa.moa_server.domain.user.dto.response;

import com.moa.moa_server.domain.group.entity.Group;

public record GroupLabel(
        Long groupId,
        String name
) {

    public static GroupLabel from(Group group) {
        return new GroupLabel(group.getId(), group.getName());
    }
}
