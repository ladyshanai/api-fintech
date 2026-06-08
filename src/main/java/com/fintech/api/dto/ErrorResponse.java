package com.fintech.api.dto;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de error consistentes desde ControllerAdvice
 */
public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
}

