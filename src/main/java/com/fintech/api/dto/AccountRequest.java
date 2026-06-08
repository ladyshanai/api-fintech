package com.fintech.api.dto;

import java.math.BigDecimal;

public record AccountRequest(Long clientId,
                             String accountNumber,
                             String currency,
                             BigDecimal balance) {
}
