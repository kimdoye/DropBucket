package com.dropbucket.metadata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entity representing the metadata of a stored file.
 */
@Entity
@Table(
        name = "file_metadata",
        indexes = {
                @Index(name = "idx_file_metadata_bucket", columnList = "bucket_name")
        }
)
public class FileMetadata {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "bucket_name", nullable = false, length = 63)
    private String bucketName;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "stored_path", nullable = false)
    private String storedPath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Protected constructor for JPA.
     */
    protected FileMetadata() {
    }

    /**
     * Constructs a new FileMetadata.
     *
     * @param id unique identifier of the file
     * @param bucketName name of the bucket where the file is stored
     * @param originalFileName original name of the file
     * @param contentType MIME type of the file
     * @param sizeBytes size of the file in bytes
     * @param storedPath filesystem path where the file is stored
     * @param createdAt timestamp when the file was created
     */
    public FileMetadata(
            String id,
            String bucketName,
            String originalFileName,
            String contentType,
            long sizeBytes,
            String storedPath,
            Instant createdAt
    ) {
        this.id = id;
        this.bucketName = bucketName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.storedPath = storedPath;
        this.createdAt = createdAt;
    }

    /**
     * @return the unique identifier of the file
     */
    public String getId() {
        return id;
    }

    /**
     * @return the name of the bucket where the file is stored
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * @return the original name of the file
     */
    public String getOriginalFileName() {
        return originalFileName;
    }

    /**
     * @return the MIME type of the file
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return the size of the file in bytes
     */
    public long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * @return the filesystem path where the file is stored
     */
    public String getStoredPath() {
        return storedPath;
    }

    /**
     * @return the timestamp when the file was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
