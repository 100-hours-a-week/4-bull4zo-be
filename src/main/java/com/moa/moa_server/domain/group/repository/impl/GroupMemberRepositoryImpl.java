package com.moa.moa_server.domain.group.repository.impl;

import com.moa.moa_server.domain.global.cursor.GroupNameGroupIdCursor;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.QGroup;
import com.moa.moa_server.domain.group.entity.QGroupMember;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.group.repository.GroupMemberRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GroupMemberRepositoryImpl implements GroupMemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Group> findJoinedGroupLabels(User user, @Nullable GroupNameGroupIdCursor cursor, int size) {
        QGroup group = QGroup.group;
        QGroupMember member = QGroupMember.groupMember;

        BooleanBuilder builder = new BooleanBuilder()
                .and(member.user.eq(user))
                .and(member.deletedAt.isNull());

        if (cursor != null) {
            builder.and(
                    group.name.gt(cursor.groupName())
                            .or(group.name.eq(cursor.groupName()).and(group.id.gt(cursor.groupId())))
            );
        }

        return queryFactory
                .select(group)
                .from(member)
                .join(member.group, group)
                .where(builder)
                .orderBy(group.name.asc(), group.id.asc())
                .limit(size)
                .fetch();
    }
}
