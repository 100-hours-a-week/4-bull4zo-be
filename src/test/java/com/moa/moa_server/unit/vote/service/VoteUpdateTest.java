package com.moa.moa_server.unit.vote.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.moa.moa_server.domain.image.model.ImageProcessResult;
import com.moa.moa_server.domain.image.service.ImageService;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.dto.request.VoteUpdateRequest;
import com.moa.moa_server.domain.vote.dto.response.VoteUpdateResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.service.VoteService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService#updateVote")
public class VoteUpdateTest {

  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock ImageService imageService;

  @InjectMocks VoteService voteService;

  @Nested
  class 성공_케이스 {
    @Test
    @DisplayName("투표 수정 성공 - 새로운 이미지 URL")
    void updateVote_success_newImage() {
      // given
      Long userId = 2L, voteId = 10L;
      User user = mock(User.class);
      Vote vote = mock(Vote.class);
      VoteUpdateRequest req =
          new VoteUpdateRequest("본문", "temp/abc.jpg", "파일이름.jpeg", LocalDateTime.now().plusDays(1));

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(user.getId()).thenReturn(userId);
      when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));
      when(vote.getId()).thenReturn(voteId);
      when(vote.getUser()).thenReturn(user);
      when(vote.getVoteStatus()).thenReturn(Vote.VoteStatus.REJECTED);
      when(imageService.processImageOnVoteUpdate(any(), any(), any(), any()))
          .thenReturn(new ImageProcessResult("vote/abc.jpg", "파일이름.jpeg"));

      // when
      VoteUpdateResponse resp = voteService.updateVote(userId, voteId, req);

      // then
      assertThat(resp.voteId()).isEqualTo(voteId); // 서비스 응답 값 검사
      verify(vote)
          .updateForEdit(
              eq("본문"),
              eq("vote/abc.jpg"),
              eq("파일이름.jpeg"),
              any(LocalDateTime.class)); // vote 수정 검사
      verify(voteRepository).save(any(Vote.class)); // DB 저장 검사
    }

    @Test
    @DisplayName("투표 수정 성공 - 기존 이미지 URL")
    void updateVote_success_keepImage() {
      // given
      Long userId = 2L, voteId = 10L;
      User user = mock(User.class);
      Vote vote = mock(Vote.class);
      String oldImageUrl = "vote/abc.jpg";
      String oldImageName = "abc.jpg";
      String newImageUrl = "vote/abc.jpg";
      String newImageName = "abc.jpg";
      VoteUpdateRequest req =
          new VoteUpdateRequest("본문", newImageUrl, newImageName, LocalDateTime.now().plusDays(1));

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(user.getId()).thenReturn(userId);
      when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));
      when(vote.getId()).thenReturn(voteId);
      when(vote.getUser()).thenReturn(user);
      when(vote.getImageUrl()).thenReturn(oldImageUrl);
      when(vote.getVoteStatus()).thenReturn(Vote.VoteStatus.REJECTED);

      // 기존 이미지와 동일한 경우 그대로 반환
      when(imageService.processImageOnVoteUpdate(any(), any(), any(), any()))
          .thenReturn(new ImageProcessResult(oldImageUrl, oldImageName));

      // when
      VoteUpdateResponse resp = voteService.updateVote(userId, voteId, req);

      // then
      assertThat(resp.voteId()).isEqualTo(voteId);
      verify(vote)
          .updateForEdit(eq("본문"), eq(oldImageUrl), eq(oldImageName), any(LocalDateTime.class));
      verify(voteRepository).save(any(Vote.class));
      verify(imageService, never()).deleteImage(any());
    }

    @Test
    @DisplayName("투표 수정 성공 - 빈 이미지 URL")
    void updateVote_success_deleteImage() {
      // given
      Long userId = 2L, voteId = 10L;
      User user = mock(User.class);
      Vote vote = mock(Vote.class);
      String oldImageUrl = "vote/abc.jpg";
      String oldImageName = "abc.jpg";
      String newImageUrl = ""; // 빈 문자열 (이미지 삭제 요청)
      String newImageName = "";
      VoteUpdateRequest req =
          new VoteUpdateRequest("본문", newImageUrl, newImageName, LocalDateTime.now().plusDays(1));

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(user.getId()).thenReturn(userId);
      when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));
      when(vote.getId()).thenReturn(voteId);
      when(vote.getUser()).thenReturn(user);
      when(vote.getImageUrl()).thenReturn(oldImageUrl);
      when(vote.getVoteStatus()).thenReturn(Vote.VoteStatus.REJECTED);

      // 빈 이미지면 기존 이미지 삭제 & 빈 문자열 반환
      when(imageService.processImageOnVoteUpdate(any(), any(), any(), any()))
          .thenReturn(new ImageProcessResult("", ""));

      // when
      VoteUpdateResponse resp = voteService.updateVote(userId, voteId, req);

      // then
      assertThat(resp.voteId()).isEqualTo(voteId);
      verify(vote).updateForEdit(eq("본문"), eq(""), eq(""), any(LocalDateTime.class));
      verify(voteRepository).save(any(Vote.class));
      verify(imageService).processImageOnVoteUpdate(any(), any(), any(), any());
    }
  }
}
