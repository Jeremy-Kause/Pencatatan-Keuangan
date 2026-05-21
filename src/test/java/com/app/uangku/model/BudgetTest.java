package com.app.uangku.model;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BudgetTest {
    @Test
    void usagePercentage_isCalculatedFromLimitAndUsage() {
        Budget budget = new Budget(1, 1, 500000, YearMonth.of(2026, 5));
        budget.setUsedAmount(125000);

        assertEquals(25.0, budget.getUsagePercentage(), 0.0001);
    }

    @Test
    void status_isSafeBelowEightyPercent() {
        Budget budget = new Budget(1, 1, 500000, YearMonth.of(2026, 5));
        budget.setUsedAmount(300000);

        assertEquals(BudgetStatus.AMAN, budget.getStatus());
    }

    @Test
    void status_isWarningAtEightyPercent() {
        Budget budget = new Budget(1, 1, 500000, YearMonth.of(2026, 5));
        budget.setUsedAmount(400000);

        assertEquals(BudgetStatus.MENDEKATI_LIMIT, budget.getStatus());
    }

    @Test
    void status_isDangerAboveLimit() {
        Budget budget = new Budget(1, 1, 500000, YearMonth.of(2026, 5));
        budget.setUsedAmount(650000);

        assertEquals(BudgetStatus.MELEBIHI_LIMIT, budget.getStatus());
    }
}
