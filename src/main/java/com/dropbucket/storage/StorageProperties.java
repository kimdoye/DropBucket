package com.dropbucket.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * Configuration properties for storage settings.
 */
@ConfigurationProperties(prefix = "dropbucket.storage")
public class StorageProperties {

    /**
     * Root directory where objects are stored.
     */
    private Path objectDir = Path.of("/data/objects");

    /**
     * @return the root directory for storage
     */
    public Path getObjectDir() {
        return objectDir;
    }

    /**
     * @param objectDir the root directory for storage
     */
    public void setObjectDir(Path objectDir) {
        this.objectDir = objectDir;
    }
}
