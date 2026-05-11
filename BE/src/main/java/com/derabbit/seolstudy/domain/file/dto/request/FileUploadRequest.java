package com.derabbit.seolstudy.domain.file.dto.request;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FileUploadRequest {

    private MultipartFile file;

    public FileUploadRequest(MultipartFile file) {
        this.file = file;
    }
}
