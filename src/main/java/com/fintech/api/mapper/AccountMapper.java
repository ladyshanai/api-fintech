package com.fintech.api.mapper;

import com.fintech.api.dto.AccountRequest;
import com.fintech.api.dto.AccountResponse;
import com.fintech.api.entity.AccountEntity;
import com.fintech.api.enums.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "currency", expression = "java(mapCurrency(accountRequest.currency()))")
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AccountEntity toEntity(AccountRequest accountRequest);

    @Mapping(target = "clientId", source = "accountEntity.client.id")
    @Mapping(target = "clientName", source = "accountEntity.client.firstName")
    @Mapping(target = "balanceInPesos", source = "balanceInPesos")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    AccountResponse toResponse(AccountEntity accountEntity,
                               BigDecimal balanceInPesos,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt);

    default Currency mapCurrency(String currency) {
        return "USD".equalsIgnoreCase(currency) ? Currency.USD : Currency.ARS;
    }
}
