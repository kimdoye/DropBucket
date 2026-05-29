package com.dropbucket.storage;

/**
 * Exception thrown when a storage operation fails due to internal errors.
 */
public class StorageOperationException extends RuntimeException {

    /**
     * Constructs a new StorageOperationException.
     *
     * @param message error message
     * @param cause underlying cause of the failure
     */
    public StorageOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
