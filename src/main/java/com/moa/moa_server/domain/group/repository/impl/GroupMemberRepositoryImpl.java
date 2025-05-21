package com.moa.moa_server.domain.group.repository.impl;

import com.moa.moa_server.domain.global.cursor.GroupNameGroupIdCursor;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.group.entity.QGroup;
import com.moa.moa_server.domain.group.entity.QGroupMember;
import com.moa.moa_server.domain.group.repository.GroupMemberRepositoryCustom;
import com.moa.moa_server.domain.user.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupMemberRepositoryImpl implements GroupMemberRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Group> findJoinedGroupLabels(
      User user, @Nullable GroupNameGroupIdCursor cursor, int size) {
    QGroup group = QGroup.group;
    QGroupMember member = QGroupMember.groupMember;

    BooleanBuilder builder =
        new BooleanBuilder().and(member.user.eq(user)).and(member.deletedAt.isNull());

    if (cursor != null) {
      builder.and(
          group
              .name
              .gt(cursor.groupName())
              .or(group.name.eq(cursor.groupName()).and(group.id.gt(cursor.groupId()))));
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

  @Override
  public List<GroupMember> findJoinedGroups(
      User user, @Nullable GroupNameGroupIdCursor cursor, int size) {
    QGroupMember member = QGroupMember.groupMember;
    QGroup group = QGroup.group;

    BooleanBuilder builder =
        new BooleanBuilder()
            .and(member.user.eq(user))
            .and(member.deletedAt.isNull())
            .and(group.id.ne(1L)); // 공개 그룹 제외

    if (cursor != null) {
      builder.and(
          group
              .name
              .gt(cursor.groupName())
              .or(group.name.eq(cursor.groupName()).and(group.id.gt(cursor.groupId()))));
    }

    return queryFactory
        .selectFrom(member)
        .join(member.group, group)
        .fetchJoin()
        .where(builder)
        .orderBy(group.name.asc(), group.id.asc())
        .limit(size)
        .fetch();
  }
}
