package com.derabbit.seolstudy.domain.file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.derabbit.seolstudy.domain.file.File;
import com.derabbit.seolstudy.domain.todo.Todo;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findAllByTodoOrderByCreatedAtAsc(Todo todo);

    List<File> findByTodo_Id(Long todoId);

    List<File> findAllByTodo_IdAndCreator_Id(Long todoId, Long creatorId);

    List<File> findByTodo_IdInAndCreator_Id(Iterable<Long> todoIds, Long creatorId);

    /** 과제(todo)별로 올라간 파일 개수 (목록에서 이미지 N장 표시용) */
    @Query("SELECT f.todo.id, COUNT(f) FROM File f WHERE f.todo.id IN :todoIds GROUP BY f.todo.id")
    List<Object[]> countByTodoIdIn(@Param("todoIds") List<Long> todoIds);
}
