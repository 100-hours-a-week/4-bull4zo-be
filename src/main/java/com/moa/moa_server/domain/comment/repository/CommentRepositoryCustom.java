package com.moa.moa_server.domain.comment.repository;

import com.moa.moa_server.domain.comment.entity.Comment;
import com.moa.moa_server.domain.global.cursor.CreatedAtCommentIdCursor;
import com.moa.moa_server.domain.vote.entity.Vote;
import jakarta.annotation.Nullable;
import java.util.List;

public interface CommentRepositoryCustom {
  List<Comment> findByVoteWithCursor(
      Vote vote, @Nullable CreatedAtCommentIdCursor cursor, int size);
}
