package com.derabbit.seolstudy.domain.file;

import com.derabbit.seolstudy.domain.common.BaseTimeEntity;
import com.derabbit.seolstudy.domain.todo.Todo;
import com.derabbit.seolstudy.domain.user.User;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false, length = 500)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType type;

    public enum FileType {
        PDF("pdf"),
        JPG("jpg"),
        PNG("png");

        private final String ext;

        FileType(String ext) {
            this.ext = ext;
        }

        public static FileType from(String extension) {
            for (FileType type : values()) {
                if (type.ext.equalsIgnoreCase(extension)) {
                    return type;
                }
            }
            throw new CustomException(ErrorCode.FILE_TYPE_INVALID);
        }
    }

    public static File of(Todo todo, String name, User creator, String url, FileType type) {
        File file = new File();
        file.todo = todo;
        file.name = name;
        file.creator = creator;
        file.url = url;
        file.type = type;
        return file;
    }
}
