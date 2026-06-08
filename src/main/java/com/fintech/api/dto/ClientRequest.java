package com.fintech.api.dto;

public record ClientRequest(String firstName,
                            String lastNameOrCompanyName,
                            String documentNumber,
                            String address,
                            String phoneNumber,
                            String email,
                            String clientType) {
}
