package app.repository_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "file_details")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private short tag;

    @Column(name = "name")
    private String name;

    @Column(name = "path")
    private String path;

    @Column(name = "file_type")
    private String fileType;

    private long size;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @JoinColumn(name = "uploader_id")
    private Long uploaderId;

    @JoinColumn(name = "group_id")
    private Long groupId;

}
