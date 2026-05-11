package com.derabbit.seolstudy.domain.user.dto.response;

import lombok.Getter;

@Getter
public class SubjectSummaryResponse {

    private long totalStudySeconds;
    private long todoTotal;
    private long todoCompleted;
    private long feedbackTotal;
    private long feedbackRead;
    /** 멘티가 제출한 파일 수 (해당 과목) */
    private long submittedFileCount;
    /** 제출했지만 아직 피드백이 없는 과제(Todo) 수 */
    private long pendingFeedbackTodoCount;
    /** 피드백이 1개 이상 작성된 과제(Todo) 수 = 해결완료 */
    private long feedbackCompletedTodoCount;
    private double todoCompletionRate;
    private double feedbackReadRate;

    public void addStudySeconds(long seconds) {
        this.totalStudySeconds += seconds;
    }

    public void incrementTodoTotal() {
        this.todoTotal += 1;
    }

    public void incrementTodoCompleted() {
        this.todoCompleted += 1;
    }

    public void incrementFeedbackTotal() {
        this.feedbackTotal += 1;
    }

    public void incrementFeedbackRead() {
        this.feedbackRead += 1;
    }

    public void addSubmittedFileCount(long count) {
        this.submittedFileCount += count;
    }

    public void addPendingFeedbackTodoCount(long count) {
        this.pendingFeedbackTodoCount += count;
    }

    public void addFeedbackCompletedTodoCount(long count) {
        this.feedbackCompletedTodoCount += count;
    }

    public void computeRates() {
        this.todoCompletionRate = (todoTotal == 0) ? 0.0 : (double) todoCompleted / todoTotal;
        this.feedbackReadRate = (feedbackTotal == 0) ? 0.0 : (double) feedbackRead / feedbackTotal;
    }
}
