package com.derabbit.seolstudy.domain.feedback.service;

import com.derabbit.seolstudy.domain.todo.Todo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.derabbit.seolstudy.domain.feedback.Feedback;
import com.derabbit.seolstudy.domain.feedback.dto.request.FeedbackRequest;
import com.derabbit.seolstudy.domain.feedback.dto.response.FeedbackResponse;
import com.derabbit.seolstudy.domain.feedback.repository.FeedbackRepository;
import com.derabbit.seolstudy.domain.file.File;
import com.derabbit.seolstudy.domain.file.repository.FileRepository;
import com.derabbit.seolstudy.domain.notification.service.NotificationService;
import com.derabbit.seolstudy.domain.user.User;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FileRepository fileRepository;

    @Transactional
    public FeedbackResponse saveFeedback(FeedbackRequest request) {
        return saveFeedback(null, request.getFileId(), request.getData());
    }

    @Transactional
    public FeedbackResponse saveFeedback(Long mentorId, Long fileId, String data) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        validateMentorAssignment(mentorId, file);

        Feedback feedback = Feedback.of(file, data);
        Feedback saved = feedbackRepository.save(feedback);

        // 해당 파일의 todo 완료 처리
        Todo todo = file.getTodo();
        todo.updateState(2);


        return FeedbackResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(Long mentorId, Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        validateMentorAssignment(mentorId, file);

        // 파일에 대한 최신 Feedback 조회 (아직 피드백 없으면 빈 응답 → 200, 이미지 정상 표시)
        return feedbackRepository.findTopByFileOrderByCreatedAtDesc(file)
                .map(FeedbackResponse::from)
                .orElse(FeedbackResponse.empty());
    }

    private void validateMentorAssignment(Long mentorId, File file) {
        if (mentorId == null) {
            return;
        }
        User mentee = file.getTodo().getMentee();
        if (mentee.getMentorId() == null || !mentorId.equals(mentee.getMentorId())) {
            throw new CustomException(ErrorCode.MENTEE_NOT_ASSIGNED);
        }
    }
}
