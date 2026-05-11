package com.derabbit.seolstudy.domain.feedback.dto.response;

import com.derabbit.seolstudy.domain.feedback.Feedback;
import lombok.Getter;

@Getter
public class FeedbackResponse {
    private Long id;
    private String data;

    public static FeedbackResponse from(Feedback feedback) {
        FeedbackResponse res = new FeedbackResponse();
        res.id = feedback.getId();
        res.data = feedback.getData();
        return res;
    }

    /** 아직 피드백이 없을 때 (멘티만 업로드한 경우) */
    public static FeedbackResponse empty() {
        FeedbackResponse res = new FeedbackResponse();
        res.id = null;
        res.data = null;
        return res;
    }
}
