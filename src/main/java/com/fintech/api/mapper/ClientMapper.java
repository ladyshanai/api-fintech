package com.fintech.api.mapper;

import com.fintech.api.dto.ClientRequest;
import com.fintech.api.dto.ClientResponse;
import com.fintech.api.entity.ClientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userType", source = "clientRequest.clientType")
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "outstandingBalance", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    ClientEntity toEntity(ClientRequest clientRequest);

    @Mapping(target = "clientId", source = "id")
    @Mapping(target = "clientType", source = "userType")
    @Mapping(target = "registrationDate", source = "registrationDate")
    ClientResponse toResponse(ClientEntity clientEntity);

    default LocalDate map(LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }
}
