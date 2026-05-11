package com.derabbit.seolstudy.domain.file.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.derabbit.seolstudy.domain.file.File;
import com.derabbit.seolstudy.domain.file.File.FileType;
import com.derabbit.seolstudy.domain.file.dto.response.FileDownloadResult;
import com.derabbit.seolstudy.domain.file.dto.response.FileResponse;
import com.derabbit.seolstudy.domain.file.repository.FileRepository;
import com.derabbit.seolstudy.domain.todo.Todo;
import com.derabbit.seolstudy.domain.todo.repository.TodoRepository;
import com.derabbit.seolstudy.domain.user.User;
import com.derabbit.seolstudy.domain.user.repository.UserRepository;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final String FILE_DIR;

    private static final int MAX_DISPLAY_NAME_LENGTH = 255;

    @Transactional
    public FileResponse upload(Long todoId, MultipartFile multipartFile, Long userId) {

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new CustomException(ErrorCode.TODO_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        FileType fileType = getFileType(multipartFile);

        String displayName = sanitizeDisplayName(multipartFile.getOriginalFilename());

        String savedFilename = saveToLocal(multipartFile, fileType);

        File file = File.of(todo, displayName, user, savedFilename, fileType);

        File saved = fileRepository.save(file);

        return FileResponse.from(saved);
    }

    /** 멘토: 본인이 올린 파일. 멘티: 담당 멘토가 올린 파일. */
    @Transactional(readOnly = true)
    public List<FileResponse> getMentorFilesByTodo(Long todoId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Long creatorId = user.getMentorId() != null ? user.getMentorId() : userId;

        return fileRepository
                .findAllByTodo_IdAndCreator_Id(todoId, creatorId)
                .stream()
                .map(FileResponse::from)
                .toList();
    }

    /**
     * 다운로드: 권한 있는 사용자(파일 업로더 또는 해당 Todo 소유 멘티)만 접근 가능.
     * Path Traversal 방지를 위해 저장 경로를 정규화·검증 후 스트리밍.
     */
    @Transactional(readOnly = true)
    public FileDownloadResult getFileForDownload(Long fileId, Long userId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if (!canDownload(file, userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        String stored = file.getUrl();
        if (stored == null || stored.contains("..")) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        try {
            Path baseDir = Paths.get(FILE_DIR).toRealPath();
            Path filePath;
            if (stored.contains("/") || stored.contains("\\")) {
                // 레거시: url이 전체 경로인 경우 (기존 DB 호환)
                filePath = Paths.get(stored).normalize().toRealPath();
                if (!filePath.startsWith(baseDir)) {
                    throw new CustomException(ErrorCode.FILE_NOT_FOUND);
                }
            } else {
                filePath = baseDir.resolve(stored).normalize();
                if (!filePath.toRealPath().startsWith(baseDir)) {
                    throw new CustomException(ErrorCode.FILE_NOT_FOUND);
                }
            }
            if (!Files.isRegularFile(filePath)) {
                throw new CustomException(ErrorCode.FILE_NOT_FOUND);
            }
            Resource resource = new InputStreamResource(Files.newInputStream(filePath));
            String contentType = getContentTypeForDownload(file);
            String downloadFilename = getDownloadFilename(file);
            return new FileDownloadResult(resource, contentType, downloadFilename);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    private String getContentTypeForDownload(File file) {
        return switch (file.getType()) {
            case PDF -> "application/pdf";
            case JPG -> "image/jpeg";
            case PNG -> "image/png";
        };
    }

    private String getDownloadFilename(File file) {
        String name = file.getName();
        if (name == null || name.isBlank()) {
            name = "download";
        }
        String ext = file.getType().name().toLowerCase();
        if (!name.toLowerCase().endsWith("." + ext)) {
            name = name + "." + ext;
        }
        return name;
    }

    /** 파일 업로더, 해당 Todo의 멘티, Todo 생성자(멘토), 또는 해당 멘티의 담당 멘토만 다운로드 가능 */
    private boolean canDownload(File file, Long userId) {
        Long creatorId = file.getCreator().getId();
        Long menteeId = file.getTodo().getMentee().getId();
        Long todoCreatorId = file.getTodo().getCreator() != null ? file.getTodo().getCreator().getId() : null;
        Long menteeMentorId = file.getTodo().getMentee().getMentorId(); // 담당 멘토(멘티가 만든 Todo도 피드백 가능)
        return userId.equals(creatorId)
                || userId.equals(menteeId)
                || (todoCreatorId != null && userId.equals(todoCreatorId))
                || (menteeMentorId != null && userId.equals(menteeMentorId));
    }

    private FileType getFileType(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new CustomException(ErrorCode.FILE_TYPE_INVALID);
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        return FileType.from(ext);
    }

    /**
     * 저장 파일명: timestamp + sanitized 원본 파일명 (Path Traversal 문자 제거).
     * DB url 필드에는 파일명만 저장 (예: 1739123456789_과제.pdf).
     */
    private String saveToLocal(MultipartFile multipartFile, FileType fileType) {
        try {
            Path baseDirPath = Paths.get(FILE_DIR);
            if (!Files.exists(baseDirPath)) {
                Files.createDirectories(baseDirPath);
            }
            Path baseDir = baseDirPath.toRealPath();

            String ext = fileType.name().toLowerCase();
            String baseName = sanitizeForStoredFilename(multipartFile.getOriginalFilename(), ext);
            String safeFilename = System.currentTimeMillis() + "_" + baseName + "." + ext;
            Path fullPath = baseDir.resolve(safeFilename).normalize();
            if (!fullPath.startsWith(baseDir)) {
                throw new CustomException(ErrorCode.FILE_UPLOAD_FAIL);
            }

            try (FileOutputStream fos = new FileOutputStream(fullPath.toFile())) {
                fos.write(multipartFile.getBytes());
            }

            return safeFilename;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    /** 저장용 파일명 베이스: 경로·Path Traversal 제거, 확장자 제외한 이름만 (길이 제한). */
    private String sanitizeForStoredFilename(String originalFilename, String ext) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "file";
        }
        String name = originalFilename.replace("..", "").replace("/", "").replace("\\", "");
        int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (lastSlash >= 0 && lastSlash < name.length() - 1) {
            name = name.substring(lastSlash + 1);
        }
        if (name.toLowerCase().endsWith("." + ext)) {
            name = name.substring(0, name.length() - ext.length() - 1);
        }
        if (name.length() > 200) {
            name = name.substring(0, 200);
        }
        return name.isBlank() ? "file" : name;
    }

    /** 표시용 파일명: Path Traversal 문자 제거, 경로 제거, 길이 제한. */
    private String sanitizeDisplayName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "파일";
        }
        String name = originalFilename.replace("..", "").replace("/", "").replace("\\", "");
        int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (lastSlash >= 0 && lastSlash < name.length() - 1) {
            name = name.substring(lastSlash + 1);
        }
        if (name.length() > MAX_DISPLAY_NAME_LENGTH) {
            name = name.substring(0, MAX_DISPLAY_NAME_LENGTH);
        }
        return name.isBlank() ? "파일" : name;
    }
}
