package com.derabbit.seolstudy.domain.feedback.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.derabbit.seolstudy.domain.feedback.Feedback;
import com.derabbit.seolstudy.domain.file.File;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findAllByFileIn(List<File> fileIds);

    Optional<Feedback> findTopByFileOrderByCreatedAtDesc(File file);

    /** 해당 Todo들 중 피드백이 1개 이상 있는 Todo id 목록 */
    @Query("SELECT DISTINCT f.file.todo.id FROM Feedback f WHERE f.file.todo.id IN :todoIds")
    Set<Long> findDistinctTodoIdsByFile_Todo_IdIn(@Param("todoIds") List<Long> todoIds);
}
