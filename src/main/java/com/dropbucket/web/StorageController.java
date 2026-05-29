package com.dropbucket.web;

import com.dropbucket.metadata.FileMetadata;
import com.dropbucket.storage.StorageService;
import com.dropbucket.storage.StoredObject;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST controller for managing storage operations via HTTP.
 */
@RestController
@RequestMapping("/api/storage")
public class StorageController {

    private final StorageService storageService;

    /**
     * Constructs a new StorageController.
     *
     * @param storageService the service to use for storage operations
     */
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Uploads a file to a specific bucket.
     *
     * @param bucketName name of the bucket
     * @param file the file to upload
     * @return a ResponseEntity with the created location header and no body
     */
    @PostMapping(path = "/{bucketName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(
            @PathVariable String bucketName,
            @RequestPart("file") MultipartFile file
    ) {
        FileMetadata metadata = storageService.upload(bucketName, file);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(metadata.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    /**
     * Downloads a file from a specific bucket.
     *
     * @param bucketName name of the bucket
     * @param id unique identifier of the file
     * @return a ResponseEntity containing the file resource
     */
    @GetMapping("/{bucketName}/{id}")
    public ResponseEntity<Resource> download(@PathVariable String bucketName, @PathVariable String id) {
        StoredObject storedObject = storageService.download(bucketName, id);
        FileMetadata metadata = storedObject.metadata();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .contentLength(metadata.getSizeBytes())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(metadata.getOriginalFileName())
                                .build()
                                .toString()
                )
                .body(storedObject.resource());
    }

    /**
     * Retrieves metadata for a specific file.
     *
     * @param bucketName name of the bucket
     * @param id unique identifier of the file
     * @return the metadata response
     */
    @GetMapping("/{bucketName}/{id}/info")
    public MetadataResponse metadata(@PathVariable String bucketName, @PathVariable String id) {
        return MetadataResponse.from(storageService.metadata(bucketName, id));
    }

    /**
     * Deletes a specific file.
     *
     * @param bucketName name of the bucket
     * @param id unique identifier of the file
     * @return a ResponseEntity with NO_CONTENT status
     */
    @DeleteMapping("/{bucketName}/{id}")
    public ResponseEntity<Void> delete(@PathVariable String bucketName, @PathVariable String id) {
        storageService.delete(bucketName, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
