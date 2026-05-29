package com.dropbucket.metadata;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for {@link FileMetadata} entities.
 */
public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {

    /**
     * Finds file metadata by bucket name and unique identifier.
     *
     * @param bucketName name of the bucket
     * @param id unique identifier of the file
     * @return an Optional containing the metadata if found, or empty if not
     */
    Optional<FileMetadata> findByBucketNameAndId(String bucketName, String id);
}
