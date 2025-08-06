package com.eventmanagement.api.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Used for duplicate user emails, event names, etc.
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}