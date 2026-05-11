package com.derabbit.seolstudy.domain.study.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.derabbit.seolstudy.domain.study.StudySession;
import com.derabbit.seolstudy.domain.study.StudySessionMode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudySessionResponse {

    private Long sessionId;
    private LocalDate date;
    private String subject;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Long durationSeconds;
    private StudySessionMode mode;

    public static StudySessionResponse from(StudySession session) {
        return StudySessionResponse.builder()
                .sessionId(session.getId())
                .date(session.getDate())
                .subject(session.getSubject())
                .startAt(session.getStartAt())
                .endAt(session.getEndAt())
                .durationSeconds(session.getDurationSeconds())
                .mode(session.getMode())
                .build();
    }
}
