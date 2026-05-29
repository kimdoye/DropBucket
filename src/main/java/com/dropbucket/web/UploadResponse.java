package com.dropbucket.web;

/**
 * DTO representing the response after a successful file upload.
 *
 * @param id the unique identifier of the uploaded file
 */
public record UploadResponse(String id) {
}
