package com.moa.moa_server.domain.vote.repository.impl;

import com.moa.moa_server.domain.global.cursor.VoteClosedCursor;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.QVote;
import com.moa.moa_server.domain.vote.entity.QVoteResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class VoteRepositoryImpl implements VoteRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Vote> findActiveVotes(List<Group> accessibleGroups, @Nullable VoteClosedCursor cursor, @Nullable User user, int size) {
        QVote vote = QVote.vote;
        QVoteResponse voteResponse = QVoteResponse.voteResponse;

        // 조회하려는 그룹 조건
        BooleanBuilder builder = new BooleanBuilder()
                .and(vote.group.in(accessibleGroups)) // 사용자가 접근 가능한 그룹의 투표만 조회
                .and(vote.closedAt.gt(LocalDateTime.now())); // 진행 중인 투표만 조회

        // 커서 조건 (closedAt, createdAt 기준)
        if (cursor != null) {
            builder.and(
                    vote.closedAt.gt(cursor.closedAt())
                            .or(vote.closedAt.eq(cursor.closedAt())
                                    .and(vote.createdAt.gt(cursor.createdAt())))
            );
        }

        // 유저가 응답하지 않은 투표만 조회
        if (user != null) {
            builder.and(
                    JPAExpressions
                            .selectOne()
                            .from(voteResponse)
                            .where(voteResponse.vote.eq(vote)
                                    .and(voteResponse.user.eq(user)))
                            .notExists()
            );
        }

        return queryFactory
                .selectFrom(vote)
                .where(builder)
                .orderBy(vote.closedAt.asc(), vote.createdAt.asc())
                .limit(size)
                .fetch();
    }
}
