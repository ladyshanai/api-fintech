package com.fintech.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClientResponse(Long clientId,
                             String firstName,
                             String lastNameOrCompanyName,
                             String documentNumber,
                             String address,
                             String phoneNumber,
                             String email,
                             String clientType,
                             Boolean active,
                             BigDecimal outstandingBalance,
                             LocalDate registrationDate) {
}
