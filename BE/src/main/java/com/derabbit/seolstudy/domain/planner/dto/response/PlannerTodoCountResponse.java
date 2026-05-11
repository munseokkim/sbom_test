package com.derabbit.seolstudy.domain.planner.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlannerTodoCountResponse {

    private int total;
    private int completed;

    public static PlannerTodoCountResponse of(int total, int completed) {
        return PlannerTodoCountResponse.builder()
                .total(total)
                .completed(completed)
                .build();
    }
}
