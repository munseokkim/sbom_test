package com.derabbit.seolstudy.domain.planner.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.derabbit.seolstudy.domain.planner.dto.response.PlannerSummaryResponse;
import com.derabbit.seolstudy.domain.planner.service.PlannerService;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentee/planners")
public class MenteePlannerController {

    private final PlannerService plannerService;

    @GetMapping
    public PlannerSummaryResponse getPlannerSummary(
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        Long menteeId = getCurrentUserId(authentication);
        return plannerService.getPlannerSummaryByMentee(menteeId, date);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new CustomException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
