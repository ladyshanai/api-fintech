package com.fintech.api.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DolarModel(@JsonProperty("moneda") String moneda,
                         @JsonProperty("casa") String casa,
                         @JsonProperty("nombre") String nombre,
                         @JsonProperty("venta") BigDecimal venta,
                         @JsonProperty("compra") BigDecimal compra,
                         @JsonProperty("fechaactualizacion") LocalDateTime fechaActualizacion) {

}
