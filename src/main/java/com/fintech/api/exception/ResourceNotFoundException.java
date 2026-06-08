package com.fintech.api.exception;

/**
 * Excepción lanzada cuando un recurso no es encontrado.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

