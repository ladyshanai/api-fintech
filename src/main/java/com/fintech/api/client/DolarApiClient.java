package com.fintech.api.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@Component
public class DolarApiClient {
    private final RestClient dolarclient = RestClient.create();
    @Value("${dolar-api-client.api-dolar-url}")
    private String url;
    public DolarModel getCotizacion(){

        return dolarclient.get()
                .uri(url)
                .accept(APPLICATION_JSON)
                .retrieve()
                .body(DolarModel.class);
    }
}
