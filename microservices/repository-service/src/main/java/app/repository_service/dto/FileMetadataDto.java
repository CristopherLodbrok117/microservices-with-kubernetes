package app.repository_service.dto;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import app.repository_service.model.FileMetadata;

import java.time.LocalDateTime;

@Data
@Builder
public class FileMetadataDto {

    private Long id;

    private short tag;

    private String name;

    private String fileType;

    private long size;

    private LocalDateTime uploadedAt;

    private Long uploaderId;

    private Long groupId;

    public static FileMetadataDto fromEntity(FileMetadata metadata){
        return FileMetadataDto.builder()
                .id(metadata.getId())
                .tag(metadata.getTag())
                .name(metadata.getName())
                .fileType(metadata.getFileType())
                .size(metadata.getSize())
                .uploadedAt(metadata.getUploadedAt())
                .uploaderId(metadata.getUploaderId())
                .groupId(metadata.getGroupId())
                .build();
    }
}
