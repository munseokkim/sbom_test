package com.derabbit.seolstudy.domain.file.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.derabbit.seolstudy.domain.file.dto.response.FileDownloadResult;
import com.derabbit.seolstudy.domain.file.dto.response.FileResponse;
import com.derabbit.seolstudy.domain.file.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload/{todoId}")
    public ResponseEntity<FileResponse> uploadFile(
            @PathVariable("todoId") Long todoId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        FileResponse response = fileService.upload(todoId, file, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/todos/{todoId}/mentor")
    public ResponseEntity<List<FileResponse>> getFilesByTodo(
            @PathVariable("todoId") Long todoId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(fileService.getMentorFilesByTodo(todoId, userId));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable("fileId") Long fileId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        FileDownloadResult result = fileService.getFileForDownload(fileId, userId);

        String encodedFilename = encodeFilename(result.getDownloadFilename());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFilename)
                .body(result.getResource());
    }

    private static String encodeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return "download";
        }
    }
}
