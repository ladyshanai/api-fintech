package com.fintech.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


@FeignClient(name = "dolar-api-client", url = "${dolar-api-client.api-dolar-url}")
public interface DollarApiClient {
    @GetMapping("/v1/dolares/oficial")
    DollarModel getCotizacion();
}
