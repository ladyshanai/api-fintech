package com.fintech.api.exception;

/**
 * Excepción lanzada cuando un servicio externo falla (ej: API de dólares)
 */
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

