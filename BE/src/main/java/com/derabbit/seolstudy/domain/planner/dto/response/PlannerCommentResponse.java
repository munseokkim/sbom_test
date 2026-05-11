package com.derabbit.seolstudy.domain.planner.dto.response;

import java.time.LocalDate;

import com.derabbit.seolstudy.domain.planner.Planner;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlannerCommentResponse {

    private Long plannerId;
    private LocalDate date;
    private String comment;

    public static PlannerCommentResponse from(Planner planner) {
        return PlannerCommentResponse.builder()
                .plannerId(planner.getId())
                .date(planner.getDate())
                .comment(planner.getComment())
                .build();
    }

    /** 해당 날짜에 플래너(코멘트)가 없을 때 */
    public static PlannerCommentResponse empty(LocalDate date) {
        return PlannerCommentResponse.builder()
                .plannerId(null)
                .date(date)
                .comment(null)
                .build();
    }
}
