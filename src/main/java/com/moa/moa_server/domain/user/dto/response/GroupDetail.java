package com.moa.moa_server.domain.user.dto.response;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;

public record GroupDetail(
        Long groupId,
        String name,
        String description,
        String imageUrl,
        String inviteCode,
        String role
) {
    public static GroupDetail from(Group group, GroupMember.Role role) {
        return new GroupDetail(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getImageUrl(),
                group.getInviteCode(),
                role.name()
        );
    }

    public static GroupDetail from(GroupMember member) {
        return from(member.getGroup(), member.getRole());
    }
}
