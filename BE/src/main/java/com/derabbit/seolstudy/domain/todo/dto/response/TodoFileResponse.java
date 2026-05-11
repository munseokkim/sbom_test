package com.derabbit.seolstudy.domain.todo.dto.response;

import java.util.List;

import com.derabbit.seolstudy.domain.file.File;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoFileResponse {

    private Long fileId;
    /** 클라이언트용 다운로드 URL: /api/files/{id}/download */
    private String url;
    private String name;
    private String type;
    /** 업로드한 사용자 ID (멘티: 학습 점검에만 표시, 멘토: 과제 다운로드용) */
    private Long creatorId;
    private Long version;
    private List<TodoFeedbackResponse> feedbacks;

    public static TodoFileResponse from(File file, List<TodoFeedbackResponse> feedbacks) {
        String downloadUrl = "/api/files/" + file.getId() + "/download";
        return TodoFileResponse.builder()
                .fileId(file.getId())
                .url(downloadUrl)
                .name(file.getName())
                .type(file.getType() == null ? null : file.getType().name().toLowerCase())
                .creatorId(file.getCreator() != null ? file.getCreator().getId() : null)
                .feedbacks(feedbacks)
                .build();
    }
}
