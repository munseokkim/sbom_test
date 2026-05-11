package com.derabbit.seolstudy.domain.study.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class StudySessionStopRequest {
    private LocalDateTime endAt;
}
