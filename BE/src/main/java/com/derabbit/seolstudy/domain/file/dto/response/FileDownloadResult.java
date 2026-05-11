package com.derabbit.seolstudy.domain.file.dto.response;

import org.springframework.core.io.Resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FileDownloadResult {

    private final Resource resource;
    private final String contentType;
    private final String downloadFilename;
}
