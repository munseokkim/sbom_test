package com.derabbit.seolstudy.domain.feedback.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.derabbit.seolstudy.domain.feedback.dto.request.FileFeedbackRequest;
import com.derabbit.seolstudy.domain.feedback.dto.response.FeedbackResponse;
import com.derabbit.seolstudy.domain.feedback.service.FeedbackService;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentor/files")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/{fileId}/feedback")
    public FeedbackResponse createFeedback(
            @PathVariable("fileId") Long fileId,
            @RequestBody FileFeedbackRequest request,
            Authentication authentication
    ) {
        Long mentorId = getCurrentUserId(authentication);
        return feedbackService.saveFeedback(mentorId, fileId, request.getData());
    }
    
    @GetMapping("/{fileId}/feedback")
    public FeedbackResponse getFeedback(
            @PathVariable("fileId") Long fileId,
            Authentication authentication
    ) {
        Long mentorId = getCurrentUserId(authentication);
        return feedbackService.getFeedback(mentorId, fileId);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new CustomException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
