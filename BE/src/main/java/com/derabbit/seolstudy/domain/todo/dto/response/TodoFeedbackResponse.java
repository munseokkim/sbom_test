package com.derabbit.seolstudy.domain.todo.dto.response;

import com.derabbit.seolstudy.domain.feedback.Feedback;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoFeedbackResponse {

    private Long feedbackId;
    private String type;
    private String data;

    public static TodoFeedbackResponse from(Feedback feedback) {
        return TodoFeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .data(feedback.getData())
                .build();
    }
}
