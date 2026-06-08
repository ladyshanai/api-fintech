package com.fintech.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Solicitud para crear o actualizar una cuenta")
public record AccountRequest(
        @Schema(description = "ID del cliente propietario de la cuenta", example = "1")
        Long clientId,
        @Schema(description = "Número de cuenta", example = "123456789")
        String accountNumber,
        @Schema(description = "Moneda de la cuenta (USD, ARS, BRL, EUR)", example = "USD")
        String currency,
        @Schema(description = "Saldo inicial de la cuenta", example = "1000.00")
        BigDecimal balance) {
}
