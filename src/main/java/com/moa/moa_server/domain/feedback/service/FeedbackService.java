package com.moa.moa_server.domain.feedback.service;

import com.moa.moa_server.domain.feedback.dto.request.FeedbackCreateRequest;
import com.moa.moa_server.domain.feedback.entity.Feedback;
import com.moa.moa_server.domain.feedback.repository.FeedbackRepository;
import com.moa.moa_server.domain.feedback.util.FeedbackValidator;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createFeedback(Long userId, FeedbackCreateRequest request) {
        // 유저 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        AuthUserValidator.validateActive(user);

        //
        FeedbackValidator.validateContent(request.content());

        Feedback feedback = Feedback.create(user, request.content());

        feedbackRepository.save(feedback);
    }
}
