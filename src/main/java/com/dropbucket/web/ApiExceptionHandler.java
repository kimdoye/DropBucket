package com.dropbucket.web;

import com.dropbucket.storage.InvalidBucketNameException;
import com.dropbucket.storage.StorageObjectNotFoundException;
import com.dropbucket.storage.StorageOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Global exception handler for the API.
 * Maps specific exceptions to appropriate HTTP status codes and problem details.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles bad request exceptions.
     *
     * @param exception the exception to handle
     * @return a ResponseEntity containing the problem detail
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            InvalidBucketNameException.class,
            MissingServletRequestPartException.class
    })
    public ResponseEntity<ProblemDetail> badRequest(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * Handles not found exceptions.
     *
     * @param exception the exception to handle
     * @return a ResponseEntity containing the problem detail
     */
    @ExceptionHandler(StorageObjectNotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(Exception exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    /**
     * Handles storage operation failure exceptions.
     *
     * @param exception the exception to handle
     * @return a ResponseEntity containing the problem detail
     */
    @ExceptionHandler(StorageOperationException.class)
    public ResponseEntity<ProblemDetail> storageFailure(Exception exception) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    /**
     * Helper method to create a ProblemDetail response.
     *
     * @param status the HTTP status
     * @param detail the error detail message
     * @return a ResponseEntity containing the problem detail
     */
    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        return ResponseEntity.status(status).body(problemDetail);
    }
}
