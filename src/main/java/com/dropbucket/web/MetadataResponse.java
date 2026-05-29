package com.dropbucket.web;

import com.dropbucket.metadata.FileMetadata;

import java.time.Instant;

/**
 * DTO representing the metadata of a file for API responses.
 *
 * @param id unique identifier of the file
 * @param bucketName name of the bucket
 * @param originalFileName original name of the file
 * @param contentType MIME type of the file
 * @param sizeBytes size of the file in bytes
 * @param createdAt timestamp when the file was created
 */
public record MetadataResponse(
        String id,
        String bucketName,
        String originalFileName,
        String contentType,
        long sizeBytes,
        Instant createdAt
) {

    /**
     * Maps a {@link FileMetadata} entity to a {@link MetadataResponse} DTO.
     *
     * @param metadata the file metadata entity
     * @return a new MetadataResponse
     */
    public static MetadataResponse from(FileMetadata metadata) {
        return new MetadataResponse(
                metadata.getId(),
                metadata.getBucketName(),
                metadata.getOriginalFileName(),
                metadata.getContentType(),
                metadata.getSizeBytes(),
                metadata.getCreatedAt()
        );
    }
}
