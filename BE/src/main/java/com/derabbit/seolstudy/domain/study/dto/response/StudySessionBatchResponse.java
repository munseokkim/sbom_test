package com.derabbit.seolstudy.domain.study.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudySessionBatchResponse {

    private List<StudySessionResponse> sessions;

    public static StudySessionBatchResponse from(List<StudySessionResponse> sessions) {
        return StudySessionBatchResponse.builder()
                .sessions(sessions)
                .build();
    }
}
