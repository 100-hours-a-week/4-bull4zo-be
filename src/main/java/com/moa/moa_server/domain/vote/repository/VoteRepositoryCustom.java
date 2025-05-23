package com.moa.moa_server.domain.vote.repository;

import com.moa.moa_server.domain.global.cursor.ClosedAtVoteIdCursor;
import com.moa.moa_server.domain.global.cursor.CreatedAtVoteIdCursor;
import com.moa.moa_server.domain.global.cursor.VoteClosedCursor;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import jakarta.annotation.Nullable;
import java.util.List;

public interface VoteRepositoryCustom {
  List<Vote> findActiveVotes(
      List<Group> accessibleGroups,
      @Nullable VoteClosedCursor cursor,
      @Nullable User user,
      int size);

  List<Vote> findMyVotes(
      User user, List<Group> groups, @Nullable CreatedAtVoteIdCursor cursor, int size);

  List<Vote> findSubmittedVotes(
      User user, List<Group> groups, @Nullable ClosedAtVoteIdCursor cursor, int size);
}
