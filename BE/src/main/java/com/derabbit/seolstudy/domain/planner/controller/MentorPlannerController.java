package com.derabbit.seolstudy.domain.planner.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.derabbit.seolstudy.domain.planner.dto.request.PlannerCommentRequest;
import com.derabbit.seolstudy.domain.planner.dto.response.PlannerCommentResponse;
import com.derabbit.seolstudy.domain.planner.service.PlannerService;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentor/mentees")
public class MentorPlannerController {

    private final PlannerService plannerService;

    @GetMapping("/{menteeId}/planners/comment")
    public PlannerCommentResponse getComment(
            @PathVariable("menteeId") Long menteeId,
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        Long mentorId = getCurrentUserId(authentication);
        return plannerService.getCommentByMentor(mentorId, menteeId, date);
    }

    @PatchMapping("/{menteeId}/planners/comment")
    public PlannerCommentResponse upsertComment(
            @PathVariable("menteeId") Long menteeId,
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody PlannerCommentRequest request,
            Authentication authentication
    ) {
        Long mentorId = getCurrentUserId(authentication);
        return plannerService.upsertCommentByMentor(mentorId, menteeId, date, request.getComment());
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new CustomException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
