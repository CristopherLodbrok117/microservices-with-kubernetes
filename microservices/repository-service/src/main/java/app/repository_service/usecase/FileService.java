package app.repository_service.usecase;

import app.repository_service.dto.FileMetadataDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

    public List<FileMetadataDto> getAllByGroup(long groupId) ;

    public FileMetadataDto saveFile(MultipartFile file, long userId, long groupId) ;

    public FileMetadataDto getMetadata(long id) ;

    public byte[] getFile(long id) ;

    public void deleteFile(long id);
}
