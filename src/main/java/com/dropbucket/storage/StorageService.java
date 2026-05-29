package com.dropbucket.storage;

import com.dropbucket.metadata.FileMetadata;
import com.dropbucket.metadata.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Service for managing file storage operations.
 * Handles uploading, downloading, and deleting files, as well as metadata management.
 */
@Service
public class StorageService {

    private static final Pattern SAFE_BUCKET_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{0,62}$");
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final FileMetadataRepository repository;
    private final Path objectRoot;

    /**
     * Constructs a new StorageService.
     *
     * @param repository the repository for file metadata
     * @param properties the storage configuration properties
     */
    public StorageService(FileMetadataRepository repository, StorageProperties properties) {
        this.repository = repository;
        this.objectRoot = properties.getObjectDir().toAbsolutePath().normalize();
    }

    /**
     * Uploads a file to a specific bucket.
     *
     * @param bucketName name of the bucket
     * @param file the file to upload
     * @return metadata of the uploaded file
     * @throws IllegalArgumentException if the file is empty
     * @throws StorageOperationException if an error occurs during file storage
     */
    public FileMetadata upload(String bucketName, MultipartFile file) {
        validateBucketName(bucketName);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Upload file must not be empty");
        }

        String id = UUID.randomUUID().toString();
        String originalFileName = normalizeOriginalFileName(file.getOriginalFilename());
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : DEFAULT_CONTENT_TYPE;
        Path target = bucketDirectory(bucketName).resolve(id).normalize();

        try {
            Files.createDirectories(target.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, REPLACE_EXISTING);
            }

            FileMetadata metadata = new FileMetadata(
                    id,
                    bucketName,
                    originalFileName,
                    contentType,
                    file.getSize(),
                    target.toString(),
                    Instant.now()
            );
            return repository.saveAndFlush(metadata);
        } catch (IOException | RuntimeException exception) {
            deleteIfExists(target);
            throw new StorageOperationException("Failed to store uploaded object", exception);
        }
    }

    /**
     * Downloads a file from a specific bucket.
     *
     * @param bucketName name of the bucket
     * @param id unique identifier of the file
     * @return the stored object containing metadata and resource
     * @throws StorageObjectNotFoundException if the file does not exist
     */
    @Transactional(readOnly = true)
    public StoredObject download(String bucketName, String id) {
        FileMetadata metadata = metadata(bucketName, id);
        Path path = Path.of(metadata.getStoredPath()).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            throw new StorageObjectNotFoundException(bucketName, id);
        }
        return new StoredObject(metadata, new org.springframework.core.io.FileSystemResource(path));
    }

    /**
     * Retrieves metadata for a file.
     *
     * @param bucketName name of the bucket
     * @param id unique identifier of the file
     * @return the file metadata
     * @throws StorageObjectNotFoundException if the metadata does not exist
     */
    @Transactional(readOnly = true)
    public FileMetadata metadata(String bucketName, String id) {
        validateBucketName(bucketName);
        return repository.findByBucketNameAndId(bucketName, id)
                .orElseThrow(() -> new StorageObjectNotFoundException(bucketName, id));
    }

    /**
     * Deletes a file and its metadata.
     *
     * @param bucketName name of the bucket
     * @param id unique identifier of the file
     * @throws StorageObjectNotFoundException if the file does not exist
     * @throws StorageOperationException if an error occurs during deletion
     */
    @Transactional
    public void delete(String bucketName, String id) {
        FileMetadata metadata = metadata(bucketName, id);
        Path path = Path.of(metadata.getStoredPath()).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            throw new StorageObjectNotFoundException(bucketName, id);
        }

        try {
            Files.delete(path);
        } catch (IOException exception) {
            throw new StorageOperationException("Failed to delete stored object", exception);
        }

        repository.delete(metadata);
    }

    /**
     * Resolves the directory for a specific bucket.
     *
     * @param bucketName name of the bucket
     * @return the path to the bucket directory
     * @throws InvalidBucketNameException if the bucket name is invalid
     */
    private Path bucketDirectory(String bucketName) {
        Path bucketDir = objectRoot.resolve(bucketName).normalize();
        if (!bucketDir.startsWith(objectRoot)) {
            throw new InvalidBucketNameException(bucketName);
        }
        return bucketDir;
    }

    /**
     * Validates if the bucket name is safe and follows naming rules.
     *
     * @param bucketName name of the bucket
     * @throws InvalidBucketNameException if the bucket name is invalid
     */
    private void validateBucketName(String bucketName) {
        if (!StringUtils.hasText(bucketName)
                || !SAFE_BUCKET_NAME.matcher(bucketName).matches()
                || bucketName.contains("..")) {
            throw new InvalidBucketNameException(bucketName);
        }
    }

    /**
     * Normalizes the original file name to prevent path traversal and ensure a valid name.
     *
     * @param originalFilename the original name of the file
     * @return the normalized file name
     */
    private String normalizeOriginalFileName(String originalFilename) {
        String filename = StringUtils.getFilename(originalFilename);
        return StringUtils.hasText(filename) ? filename : "upload.bin";
    }

    /**
     * Deletes a file if it exists, ignoring errors.
     *
     * @param target the path to delete
     */
    private void deleteIfExists(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
        }
    }
}
