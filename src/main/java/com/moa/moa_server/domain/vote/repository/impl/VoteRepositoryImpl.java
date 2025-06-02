package com.moa.moa_server.domain.vote.repository.impl;

import com.moa.moa_server.domain.global.cursor.CreatedAtVoteIdCursor;
import com.moa.moa_server.domain.global.cursor.VoteClosedCursor;
import com.moa.moa_server.domain.global.cursor.VotedAtVoteIdCursor;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.QVote;
import com.moa.moa_server.domain.vote.entity.QVoteResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.model.VoteWithVotedAt;
import com.moa.moa_server.domain.vote.repository.VoteRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class VoteRepositoryImpl implements VoteRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Vote> findActiveVotes(
      List<Group> accessibleGroups,
      @Nullable VoteClosedCursor cursor,
      @Nullable User user,
      int size) {
    QVote vote = QVote.vote;
    QVoteResponse voteResponse = QVoteResponse.voteResponse;

    // 조회 조건
    BooleanBuilder builder =
        new BooleanBuilder()
            .and(vote.group.in(accessibleGroups)) // 사용자가 접근 가능한 그룹의 투표만 조회
            .and(vote.closedAt.gt(LocalDateTime.now())) // 진행 중인 투표만 조회 (시간으로 먼저 정확히 검사)
            .and(vote.voteStatus.eq(Vote.VoteStatus.OPEN)); // OPEN 투표만 조회 (PENDING, REJECTED 필터링)

    // 커서 조건 (closedAt, createdAt 기준)
    if (cursor != null) {
      builder.and(
          vote.closedAt
              .gt(cursor.closedAt())
              .or(vote.closedAt.eq(cursor.closedAt()).and(vote.createdAt.gt(cursor.createdAt()))));
    }

    // 유저가 응답하지 않은 투표만 조회
    if (user != null) {
      builder.and(
          JPAExpressions.selectOne()
              .from(voteResponse)
              .where(voteResponse.vote.eq(vote).and(voteResponse.user.eq(user)))
              .notExists());
    }

    return queryFactory
        .selectFrom(vote)
        .where(builder)
        .orderBy(vote.closedAt.asc(), vote.createdAt.asc())
        .limit(size)
        .fetch();
  }

  @Override
  public List<Vote> findMyVotes(
      User user, List<Group> groups, @Nullable CreatedAtVoteIdCursor cursor, int size) {
    QVote vote = QVote.vote;

    BooleanBuilder builder =
        new BooleanBuilder()
            .and(vote.user.eq(user))
            .and(vote.deletedAt.isNull())
            .and(vote.group.in(groups));

    if (cursor != null) {
      builder.and(
          vote.createdAt
              .lt(cursor.createdAt())
              .or(vote.createdAt.eq(cursor.createdAt()).and(vote.id.lt(cursor.voteId()))));
    }

    return queryFactory
        .selectFrom(vote)
        .where(builder)
        .orderBy(vote.createdAt.desc(), vote.id.desc())
        .limit(size)
        .fetch();
  }

  @Override
  public List<VoteWithVotedAt> findSubmittedVotes(
      User user, List<Group> groups, @Nullable VotedAtVoteIdCursor cursor, int size) {
    QVote vote = QVote.vote;
    QVoteResponse voteResponse = QVoteResponse.voteResponse;

    BooleanBuilder builder =
        new BooleanBuilder()
            .and(voteResponse.user.eq(user))
            .and(voteResponse.optionNumber.ne(0)) // 기권 제외
            .and(vote.group.in(groups))
            .and(
                vote.voteStatus.in(
                    Vote.VoteStatus.OPEN, Vote.VoteStatus.CLOSED)); // PENDING, REJECTED 제외

    if (cursor != null) {
      builder.and(
          voteResponse
              .votedAt
              .lt(cursor.votedAt())
              .or(voteResponse.votedAt.eq(cursor.votedAt()).and(vote.id.lt(cursor.voteId()))));
    }

    return queryFactory
        .select(Projections.constructor(VoteWithVotedAt.class, vote, voteResponse.votedAt))
        .from(voteResponse)
        .join(voteResponse.vote, vote)
        .where(builder)
        .orderBy(voteResponse.votedAt.desc(), vote.id.desc())
        .limit(size)
        .fetch();
  }
}
