package com.derabbit.seolstudy.domain.file.dto.response;

import com.derabbit.seolstudy.domain.file.File;
import com.derabbit.seolstudy.domain.file.File.FileType;

import lombok.Getter;

@Getter
public class FileResponse {

    private Long id;
    private Long todoId;
    private String url;
    private String name;
    private FileType type;
    private Long creatorId;

    public FileResponse(Long id, Long todoId, String url, String name, FileType type, Long creatorId) {
        this.id = id;
        this.todoId = todoId;
        this.url = url;
        this.name = name;
        this.type = type;
        this.creatorId = creatorId;
    }

    public static FileResponse from(File file) {
        String downloadUrl = "/api/files/" + file.getId() + "/download";
        return new FileResponse(
                file.getId(),
                file.getTodo().getId(),
                downloadUrl,
                file.getName(),
                file.getType(),
                file.getCreator().getId()
        );
    }
}
