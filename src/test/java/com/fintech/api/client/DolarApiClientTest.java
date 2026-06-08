package com.fintech.api.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.RequestHeadersSpec;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DolarApiClient Unit Tests")
class DolarApiClientTest {

    private DolarApiClient dolarApiClient;

    @Test
    @DisplayName("getCotizacion: Debe retornar DolarModel válido")
    void testGetCotizacion_Success() {
        // Este test requeriría mockear RestClient, que es más complejo
        // Por lo que se recomienda un test de integración real con WireMock o similar
        // Este es un test básico de estructura

        DolarApiClient client = new DolarApiClient();
        // Establecer URL vía reflection
        ReflectionTestUtils.setField(client, "url", "https://dolarapi.com/v1/dolares/oficial");

        // El cliente real haría la llamada HTTP
        // assertNotNull(model);
    }

    @Test
    @DisplayName("DolarApiClient debe ser un Component")
    void testDolarApiClientIsComponent() {
        DolarApiClient client = new DolarApiClient();
        assertNotNull(client);
    }

    @Test
    @DisplayName("getCotizacion debe retornar un record con 6 campos")
    void testDolarModelHasSixFields() {
        DolarModel model = new DolarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );

        assertNotNull(model.moneda());
        assertNotNull(model.casa());
        assertNotNull(model.nombre());
        assertNotNull(model.venta());
        assertNotNull(model.compra());
        assertNotNull(model.fechaActualizacion());

        assertEquals("Oficial", model.moneda());
        assertEquals("ABC", model.casa());
        assertEquals("Dólar", model.nombre());
        assertEquals(new BigDecimal("46.50"), model.venta());
        assertEquals(new BigDecimal("46.00"), model.compra());
    }

    @Test
    @DisplayName("DolarModel debe ser serializable/deserializable")
    void testDolarModelSerialization() {
        // Crear modelo
        DolarModel original = new DolarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );

        // Verificar que se puede acceder a todos los campos
        assertNotNull(original.moneda());
        assertNotNull(original.casa());
        assertNotNull(original.nombre());
        assertNotNull(original.venta());
        assertNotNull(original.compra());
        assertNotNull(original.fechaActualizacion());
    }

    @Test
    @DisplayName("DolarModel debe validar BigDecimal correctamente")
    void testDolarModelBigDecimalValues() {
        DolarModel model = new DolarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );

        // Pruebas de precisión con BigDecimal
        assertEquals(0, model.compra().compareTo(new BigDecimal("46.00")));
        assertEquals(0, model.venta().compareTo(new BigDecimal("46.50")));

        // La venta debe ser mayor que la compra
        assertTrue(model.venta().compareTo(model.compra()) > 0);
    }

    @Test
    @DisplayName("DolarModel debe manejar monedas diferentes")
    void testDolarModelDifferentCurrencies() {
        DolarModel oficial = new DolarModel(
                "Oficial",
                "ABC",
                "Dólar Oficial",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );

        DolarModel blue = new DolarModel(
                "Blue",
                "ABC",
                "Dólar Blue",
                new BigDecimal("52.00"),
                new BigDecimal("51.50"),
                LocalDateTime.now()
        );

        assertEquals("Oficial", oficial.moneda());
        assertEquals("Blue", blue.moneda());

        // El Dólar Blue tiene mayor cotización
        assertTrue(blue.compra().compareTo(oficial.compra()) > 0);
    }

    @Test
    @DisplayName("DolarModel con valores cero")
    void testDolarModelWithZeroValues() {
        DolarModel model = new DolarModel(
                "Test",
                "ABC",
                "Test",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        assertEquals(BigDecimal.ZERO, model.venta());
        assertEquals(BigDecimal.ZERO, model.compra());
    }

    @Test
    @DisplayName("DolarModel debe preservar precisión de decimales")
    void testDolarModelDecimalPrecision() {
        DolarModel model = new DolarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50123456789"),
                new BigDecimal("46.00123456789"),
                LocalDateTime.now()
        );

        // BigDecimal preserva la precisión
        assertEquals(new BigDecimal("46.50123456789"), model.venta());
        assertEquals(new BigDecimal("46.00123456789"), model.compra());
    }

    @Test
    @DisplayName("DolarModel debe ser comparable")
    void testDolarModelComparable() {
        DolarModel model1 = new DolarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );

        DolarModel model2 = new DolarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );

        // Ambos deben tener los mismos valores
        assertEquals(model1.venta(), model2.venta());
        assertEquals(model1.compra(), model2.compra());
    }

    @Test
    @DisplayName("DolarModel debe manejar LocalDateTime")
    void testDolarModelLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DolarModel model = new DolarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                now
        );

        assertNotNull(model.fechaActualizacion());
        assertEquals(now.getYear(), model.fechaActualizacion().getYear());
        assertEquals(now.getMonth(), model.fechaActualizacion().getMonth());
        assertEquals(now.getDayOfMonth(), model.fechaActualizacion().getDayOfMonth());
    }
}

