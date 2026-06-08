package com.fintech.api.client;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Unitarios para DolarModel
 */
@DisplayName("DolarModel Unit Tests")
class DollarModelTest {

    @Test
    @DisplayName("DolarModel: Debe tener 6 campos")
    void testDolarModelHasSixFields() {
        DollarModel model = new DollarModel(
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
    @DisplayName("DolarModel: Debe validar BigDecimal correctamente")
    void testDolarModelBigDecimalValues() {
        DollarModel model = new DollarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );

        assertEquals(0, model.compra().compareTo(new BigDecimal("46.00")));
        assertEquals(0, model.venta().compareTo(new BigDecimal("46.50")));

        assertTrue(model.venta().compareTo(model.compra()) > 0);
    }

    @Test
    @DisplayName("DolarModel: Debe manejar monedas diferentes")
    void testDolarModelDifferentCurrencies() {
        DollarModel oficial = new DollarModel(
                "Oficial",
                "ABC",
                "Dólar Oficial",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );

        DollarModel blue = new DollarModel(
                "Blue",
                "ABC",
                "Dólar Blue",
                new BigDecimal("52.00"),
                new BigDecimal("51.50"),
                LocalDateTime.now()
        );

        assertEquals("Oficial", oficial.moneda());
        assertEquals("Blue", blue.moneda());
        assertTrue(blue.compra().compareTo(oficial.compra()) > 0);
    }

    @Test
    @DisplayName("DolarModel: Debe preservar precisión de decimales")
    void testDolarModelDecimalPrecision() {
        DollarModel model = new DollarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50123456789"),
                new BigDecimal("46.00123456789"),
                LocalDateTime.now()
        );

        assertEquals(new BigDecimal("46.50123456789"), model.venta());
        assertEquals(new BigDecimal("46.00123456789"), model.compra());
    }

    @Test
    @DisplayName("DolarModel: Debe ser comparable")
    void testDolarModelComparable() {
        DollarModel model1 = new DollarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );

        DollarModel model2 = new DollarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );

        assertEquals(model1.venta(), model2.venta());
        assertEquals(model1.compra(), model2.compra());
    }

    @Test
    @DisplayName("DolarModel: Debe retornar valores correctos")
    void testDolarModelValues() {
        LocalDateTime now = LocalDateTime.now();
        DollarModel model = new DollarModel(
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

