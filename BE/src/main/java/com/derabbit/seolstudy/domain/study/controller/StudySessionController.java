package com.derabbit.seolstudy.domain.study.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.derabbit.seolstudy.domain.study.dto.request.StudySessionManualRequest;
import com.derabbit.seolstudy.domain.study.dto.request.StudySessionStartRequest;
import com.derabbit.seolstudy.domain.study.dto.request.StudySessionStopRequest;
import com.derabbit.seolstudy.domain.study.dto.response.StudySessionBatchResponse;
import com.derabbit.seolstudy.domain.study.dto.response.StudySessionResponse;
import com.derabbit.seolstudy.domain.study.service.StudySessionService;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentee/study-sessions")
public class StudySessionController {

    private final StudySessionService studySessionService;

    @PostMapping("/start")
    public StudySessionResponse start(
            @RequestBody StudySessionStartRequest request,
            Authentication authentication
    ) {
        Long menteeId = getCurrentUserId(authentication);
        return studySessionService.startAutoSession(menteeId, request);
    }

    @PatchMapping("/{sessionId}/stop")
    public StudySessionBatchResponse stop(
            @PathVariable("sessionId") Long sessionId,
            @RequestBody StudySessionStopRequest request,
            Authentication authentication
    ) {
        Long menteeId = getCurrentUserId(authentication);
        return studySessionService.stopAutoSession(menteeId, sessionId, request);
    }

    @PostMapping("/manual")
    public StudySessionBatchResponse createManual(
            @RequestBody StudySessionManualRequest request,
            Authentication authentication
    ) {
        Long menteeId = getCurrentUserId(authentication);
        return studySessionService.createManualSession(menteeId, request);
    }

    @GetMapping
    public List<StudySessionResponse> getByDate(
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        Long menteeId = getCurrentUserId(authentication);
        return studySessionService.getSessionsByDate(menteeId, date);
    }

    @DeleteMapping("/{sessionId}")
    public void delete(
            @PathVariable("sessionId") Long sessionId,
            Authentication authentication
    ) {
        Long menteeId = getCurrentUserId(authentication);
        studySessionService.deleteSession(menteeId, sessionId);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new CustomException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
