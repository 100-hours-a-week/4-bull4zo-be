package com.moa.moa_server.domain.comment.repository.impl;

import com.moa.moa_server.domain.comment.entity.Comment;
import com.moa.moa_server.domain.comment.entity.QComment;
import com.moa.moa_server.domain.comment.repository.CommentRepositoryCustom;
import com.moa.moa_server.domain.global.cursor.CreatedAtCommentIdCursor;
import com.moa.moa_server.domain.user.entity.QUser;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CommentRepositoryImpl implements CommentRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Comment> findByVoteWithCursor(
      Vote vote, @Nullable CreatedAtCommentIdCursor cursor, int size) {
    QComment comment = QComment.comment;

    BooleanBuilder builder = buildCursorCondition(comment, vote, cursor);

    return queryFactory
        .selectFrom(comment)
        .where(builder)
        .orderBy(comment.createdAt.asc(), comment.id.asc())
        .limit(size + 1)
        .fetch();
  }

  @Override
  public List<Comment> findByVoteWithCursorFetchUser(
      Vote vote, @Nullable CreatedAtCommentIdCursor cursor, int size) {
    QComment comment = QComment.comment;
    QUser user = QUser.user;

    BooleanBuilder builder = buildCursorCondition(comment, vote, cursor);

    return queryFactory
        .selectFrom(comment)
        .join(comment.user, user)
        .fetchJoin()
        .where(builder)
        .orderBy(comment.createdAt.asc(), comment.id.asc())
        .limit(size + 1)
        .fetch();
  }

  private BooleanBuilder buildCursorCondition(
      QComment comment, Vote vote, @Nullable CreatedAtCommentIdCursor cursor) {
    BooleanBuilder builder =
        new BooleanBuilder().and(comment.vote.eq(vote)).and(comment.deletedAt.isNull());

    if (cursor != null) {
      builder.and(
          comment
              .createdAt
              .gt(cursor.createdAt())
              .or(comment.createdAt.eq(cursor.createdAt()).and(comment.id.gt(cursor.commentId()))));
    }

    return builder;
  }
}
