package app.repository_service.repository;

import app.repository_service.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    public List<FileMetadata> findAllByGroupId(long groupId);

    public Optional<FileMetadata> findByName(String name);
}
