package app.repository_service.service;

import app.repository_service.config.FileStorageProperties;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import app.repository_service.repository.FileMetadataRepository;

import app.repository_service.usecase.FileService;
import app.repository_service.model.FileMetadata;
import app.repository_service.exception.FileException;
import app.repository_service.dto.FileMetadataDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 5MB

    private final FileStorageProperties fileStorageProperties;
    private final FileMetadataRepository fileMetadataRepository;
    private final Set<String> allowedTypes;

    private final ProjectValidatorService projectValidatorService;

    @Override
    @Transactional(readOnly = true)
    public List<FileMetadataDto> getAllByGroup(long groupId) {
        List<FileMetadata> metadataList = fileMetadataRepository.findAllByGroupId(groupId);

        return metadataList.stream()
                .map(FileMetadataDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public FileMetadataDto saveFile(MultipartFile file, long uploaderId, long groupId) {
        /* 1. Validar archivo */
        validateGroup(groupId);
        validateUser(uploaderId);

        validateFile(file);

        short tag = 1;
        try{
            /* 2. Cerar directorio si no existe */
            String storageLocation = fileStorageProperties.getLocation();

            Path storagePath = Paths.get(storageLocation);
            if(!Files.exists(storagePath)){
                Files.createDirectories(storagePath);
            }

            /* 3. Guardar archivo */
            String fileLocation = storageLocation + File.separator + file.getOriginalFilename();
            Path filePath = Paths.get(fileLocation);

            /* To replace */
            FileMetadata fileMetadata;
            String fileName = file.getOriginalFilename();

            if(Files.exists(filePath)){
                fileMetadata = fileMetadataRepository.findByName(fileName)
                        .orElseThrow(() -> new FileException("No se encontro el archivo con el nombre: " + fileName));

                Files.delete(filePath);
            }
            else{
                fileMetadata = FileMetadata.builder()
                        .tag(tag)
                        .name(fileName)
                        .path(fileLocation)
                        .fileType(file.getContentType())
                        .groupId(groupId)
                        .build();
            }

            Files.copy(file.getInputStream(), filePath);

            /* 4. Guardar metadatos en BD */
            fileMetadata.setSize(file.getSize());
            fileMetadata.setUploadedAt(LocalDateTime.now());
            fileMetadata.setUploaderId(uploaderId);

            fileMetadata = fileMetadataRepository.save(fileMetadata);

            /* 5. Crear DTO y retornar */
            return FileMetadataDto.fromEntity(fileMetadata);
        }
        catch(IOException ex) {
            throw new FileException(ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadataDto getMetadata(long id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileException("No se encontro el archivo con ID: " + id));


        return FileMetadataDto.fromEntity(metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getFile(long id) {
        /* 1. Buscar metadata*/
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileException("No se encontro el archivo con ID: " + id));

//        filePolicy.authorize("downloadFile", metadata.getGroup());

        Path filePath = Paths.get(metadata.getPath());

        /* 2. Verificar exixtencia en sistema de archivos */
        if(!Files.exists(filePath)){
            throw new FileException("El archivo no se encuentra en el sistema de archivos");
        }

        try{
            /* 3. Retornar archivo */
            return Files.readAllBytes(filePath);
        }
        catch(IOException ex) {
            throw new FileException(ex.getMessage());
        }

    }

    @Override
    @Transactional
    public void deleteFile(long id) {
        // 1. Buscar los metadatos del archivo en la BD
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileException("No se encontró el archivo con ID: " + id));

        // 2. Obtener la ruta del archivo y eliminarlo del sistema de archivos
        Path filePath = Paths.get(metadata.getPath());
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new FileException( e.getMessage());
        }

        // 3. Eliminar los metadatos de la BD
        fileMetadataRepository.delete(metadata);
    }

    private void validateFile(MultipartFile file) {
        if(file.isEmpty()){
            throw new FileException("El archivo esta vacio");
        }
        if(file.getSize() > MAX_FILE_SIZE){
            throw new FileException("El archivo excede el tamaño maximo permitido de " +
                    (MAX_FILE_SIZE/1024) + " KB");
        }
        if(!allowedTypes.contains(file.getContentType())){
            System.out.println("allowed: " + allowedTypes);
            throw new FileException("Tipo de arcihvo no permitido: " + file.getContentType());
        }
    }

    private void validateUser(long userId){
        if(false){
            throw new FileException("No se encontro el usuario con ID: " + userId);
        }
    }

    private void validateGroup(long groupId) {
        if(!projectValidatorService.projectExists(groupId)){
            throw new FileException("No se encontro el grupo con ID: " + groupId);
        }
    }
}
