package com.fintech.api.dto;

import com.fintech.api.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(Long accountId,
                              Long clientId,
                              String clientName,
                              String accountNumber,
                              Currency currency,
                              BigDecimal balance,
                              BigDecimal balanceInPesos,
                              Boolean active,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
}
