package com.derabbit.seolstudy.domain.feedback.dto.request;

import lombok.Getter;

@Getter
public class FeedbackRequest {
    private Long fileId;
    private String data; // JSON 문자열로 전달
}