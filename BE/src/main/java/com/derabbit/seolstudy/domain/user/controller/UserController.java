package com.derabbit.seolstudy.domain.user.controller;

import java.time.LocalDate;
import java.util.List;

import com.derabbit.seolstudy.domain.user.dto.response.MenteeSummaryResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.derabbit.seolstudy.domain.user.dto.response.MenteeResponse;
import com.derabbit.seolstudy.domain.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")   // 보통 이렇게 씀
public class UserController {

    private final UserService userService;

    @GetMapping("/mentor/mentees")
    public List<MenteeResponse> getMyMentees() {
        Long mentorId = getLoginUserId();
        return userService.getMyMentees(mentorId);
    }

    @PostMapping("/mentor/{menteeId}/assign")
    public void assignMentee(@PathVariable("menteeId") Long menteeId) {
        Long mentorId = getLoginUserId();
        userService.assignMentee(mentorId, menteeId);
    }

    private Long getLoginUserId() {
        return (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @GetMapping("/mentor/{menteeId}/summary")
    public ResponseEntity<MenteeSummaryResponse> getMenteeSummary(
            @PathVariable("menteeId") Long menteeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        Long mentorId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getMenteeSummaryForMentor(mentorId, menteeId, date));
    }
}
