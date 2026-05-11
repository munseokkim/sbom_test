package com.derabbit.seolstudy.domain.planner.dto.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlannerSummaryResponse {

    private Long plannerId;
    private LocalDate date;
    private String comment;
    private PlannerTodoCountResponse todoCounts;
    private PlannerStudySummaryResponse studySummary;
}
