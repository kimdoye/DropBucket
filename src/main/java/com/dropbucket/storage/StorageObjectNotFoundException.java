package com.dropbucket.storage;

/**
 * Exception thrown when a requested storage object cannot be found.
 */
public class StorageObjectNotFoundException extends RuntimeException {

    /**
     * Constructs a new StorageObjectNotFoundException.
     *
     * @param bucketName name of the bucket
     * @param id unique identifier of the object
     */
    public StorageObjectNotFoundException(String bucketName, String id) {
        super("Object not found: " + bucketName + "/" + id);
    }
}
