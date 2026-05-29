package com.dropbucket.storage;

/**
 * Exception thrown when a bucket name does not meet the required format.
 */
public class InvalidBucketNameException extends RuntimeException {

    /**
     * Constructs a new InvalidBucketNameException.
     *
     * @param bucketName the invalid bucket name
     */
    public InvalidBucketNameException(String bucketName) {
        super("Invalid bucket name: " + bucketName);
    }
}
