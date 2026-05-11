package com.derabbit.seolstudy.domain.planner.dto.response;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlannerStudySummaryResponse {

    private Long totalSeconds;
    private Map<String, Long> bySubject;

    public static PlannerStudySummaryResponse of(Long totalSeconds, Map<String, Long> bySubject) {
        return PlannerStudySummaryResponse.builder()
                .totalSeconds(totalSeconds)
                .bySubject(bySubject)
                .build();
    }
}
