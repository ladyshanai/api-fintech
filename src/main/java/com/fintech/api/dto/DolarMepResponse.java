package com.fintech.api.dto;

public record DolarMepResponse( String moneda,
                                String casa,
                                String nombre,
                                Double compra,
                                Double venta,
                                String fechaActualizacion) {
}
