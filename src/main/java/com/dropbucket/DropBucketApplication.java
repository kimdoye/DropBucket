package com.dropbucket;

import com.dropbucket.storage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main entry point for the DropBucket application.
 * Configures Spring Boot and enables storage properties.
 */
@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class DropBucketApplication {

    /**
     * Starts the DropBucket application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DropBucketApplication.class, args);
    }
}
