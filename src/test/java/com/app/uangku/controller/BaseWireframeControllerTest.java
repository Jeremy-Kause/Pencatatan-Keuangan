package com.app.uangku.controller;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BaseWireframeControllerTest {
    private final TestController controller = new TestController();

    @Test
    void parseAmount_acceptsIndonesianCurrencyFormat() {
        assertEquals(1500000.0, controller.parseAmount("Rp 1.500.000"), 0.0001);
        assertEquals(2500.75, controller.parseAmount("2.500,75"), 0.0001);
    }

    @Test
    void parseAmount_rejectsZeroAndNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> controller.parseAmount("0"));
        assertThrows(IllegalArgumentException.class, () -> controller.parseAmount("-12500"));
    }

    @Test
    void parseMonth_acceptsIsoAndIndonesianMonthName() {
        assertEquals(YearMonth.of(2026, 5), controller.parseMonth("2026-05"));
        assertEquals(YearMonth.of(2026, 5), controller.parseMonth("Mei 2026"));
    }

    @Test
    void parseMonth_rejectsUnsupportedFormat() {
        assertThrows(IllegalArgumentException.class, () -> controller.parseMonth("05/2026"));
    }

    private static final class TestController extends BaseWireframeController {
    }
}
