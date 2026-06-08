package com.fintech.api.dto;

import com.fintech.api.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Respuesta con los detalles de una cuenta")
public record AccountResponse(
        @Schema(description = "ID único de la cuenta", example = "1")
        Long accountId,
        @Schema(description = "ID del cliente propietario", example = "1")
        Long clientId,
        @Schema(description = "Nombre del cliente propietario", example = "Juan Pérez")
        String clientName,
        @Schema(description = "Número de cuenta", example = "123456789")
        String accountNumber,
        @Schema(description = "Moneda de la cuenta", example = "USD")
        Currency currency,
        @Schema(description = "Saldo en moneda original", example = "1000.00")
        BigDecimal balance,
        @Schema(description = "Saldo equivalente en pesos argentinos", example = "850000.00")
        BigDecimal balanceInPesos,
        @Schema(description = "Estado de la cuenta (activa o inactiva)", example = "true")
        Boolean active,
        @Schema(description = "Fecha de creación de la cuenta")
        LocalDateTime createdAt,
        @Schema(description = "Fecha de última actualización")
        LocalDateTime updatedAt) {
}
