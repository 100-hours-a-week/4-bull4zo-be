package com.moa.moa_server.domain.comment.repository;

import com.moa.moa_server.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query(
      "SELECT c.anonymousNumber FROM Comment c WHERE c.vote.id = :voteId AND c.user.id = :userId AND c.anonymousNumber > 0 ORDER BY c.createdAt ASC")
  Integer findFirstAnonymousNumberByVoteIdAndUserId(
      @Param("voteId") Long voteId, @Param("userId") Long userId);
}
