package com.derabbit.seolstudy.domain.planner.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.derabbit.seolstudy.domain.planner.dto.request.PlannerCommentRequest;
import com.derabbit.seolstudy.domain.planner.dto.response.PlannerCommentResponse;
import com.derabbit.seolstudy.domain.planner.service.PlannerService;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentor/planners")
public class PlannerController {

    private final PlannerService plannerService;

    @PatchMapping("/{plannerId}/comment")
    public PlannerCommentResponse updateComment(
            @PathVariable("plannerId") Long plannerId,
            @RequestBody PlannerCommentRequest request,
            Authentication authentication
    ) {
        Long mentorId = getCurrentUserId(authentication);
        return plannerService.updateCommentByMentor(mentorId, plannerId, request.getComment());
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new CustomException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
