package com.moa.moa_server.domain.comment.repository;

import com.moa.moa_server.domain.comment.entity.Comment;
import com.moa.moa_server.domain.global.cursor.CreatedAtCommentIdCursor;
import com.moa.moa_server.domain.vote.entity.Vote;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query(
      "SELECT c.anonymousNumber FROM Comment c WHERE c.vote.id = :voteId AND c.user.id = :userId AND c.anonymousNumber > 0 ORDER BY c.createdAt ASC")
  Integer findFirstAnonymousNumberByVoteIdAndUserId(
      @Param("voteId") Long voteId, @Param("userId") Long userId);

  @Query(
      """
    SELECT c FROM Comment c
    WHERE c.vote.id = :voteId
      AND c.deletedAt IS NULL
      AND (c.createdAt > :createdAt OR (c.createdAt = :createdAt AND c.id > :commentId))
    ORDER BY c.createdAt ASC, c.id ASC
""")
  List<Comment> findByVoteIdAfterCursor(
      @Param("voteId") Long voteId,
      @Param("createdAt") LocalDateTime createdAt,
      @Param("commentId") Long commentId,
      Pageable pageable);

  default List<Comment> findComments(Vote vote, CreatedAtCommentIdCursor cursor, int size) {
    LocalDateTime createdAt = (cursor != null) ? cursor.createdAt() : null;
    Long commentId = (cursor != null) ? cursor.commentId() : 0L;
    Pageable pageable =
        org.springframework.data.domain.PageRequest.of(0, size + 1); // hasNext 판단 위해 +1
    return findByVoteIdAfterCursor(vote.getId(), createdAt, commentId, pageable);
  }
}
