package com.derabbit.seolstudy.domain.study.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class StudySessionStartRequest {
    private String subject;
    private LocalDateTime startAt;
}
