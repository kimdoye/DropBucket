package com.dropbucket.storage;

import com.dropbucket.metadata.FileMetadata;
import org.springframework.core.io.Resource;

/**
 * Record representing a stored object, including its metadata and the resource itself.
 *
 * @param metadata metadata of the stored object
 * @param resource the actual resource (file) content
 */
public record StoredObject(FileMetadata metadata, Resource resource) {
}
