package com.derabbit.seolstudy.domain.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.derabbit.seolstudy.domain.user.dto.request.UserUpdateRequest;
import com.derabbit.seolstudy.domain.user.dto.response.MenteeSummaryResponse;
import com.derabbit.seolstudy.domain.user.dto.response.UserResponse;
import com.derabbit.seolstudy.domain.user.service.UserService;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentee/me")
public class MenteeMyPageController {

    private final UserService userService;

    @GetMapping
    public UserResponse getProfile(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return userService.getProfile(userId);
    }

    @PatchMapping
    public UserResponse updateProfile(
            @RequestBody UserUpdateRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return userService.updateUser(userId, request);
    }

    @GetMapping("/summary")
    public MenteeSummaryResponse getSummary(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return userService.getMenteeSummary(userId);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new CustomException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
