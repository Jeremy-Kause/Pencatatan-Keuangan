package com.app.uangku.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SavingsGoalTest {
    @Test
    void progressPercentage_isCalculatedFromTargetAndCurrentAmount() {
        SavingsGoal goal = new SavingsGoal(
                1,
                "Laptop",
                8_000_000,
                LocalDate.of(2026, 12, 31),
                SavingsGoalSource.BALANCE,
                null
        );
        goal.setCurrentAmount(2_000_000);

        assertEquals(25.0, goal.getProgressPercentage(), 0.0001);
    }

    @Test
    void status_isAchievedWhenProgressMeetsTarget() {
        SavingsGoal goal = new SavingsGoal(
                1,
                "Laptop",
                8_000_000,
                LocalDate.of(2026, 12, 31),
                SavingsGoalSource.BALANCE,
                null
        );
        goal.setCurrentAmount(8_500_000);

        assertEquals(SavingsGoalStatus.ACHIEVED, goal.getStatus());
    }

    @Test
    void status_isNearTargetWhenProgressReachesSeventyPercent() {
        SavingsGoal goal = new SavingsGoal(
                1,
                "Laptop",
                8_000_000,
                LocalDate.of(2026, 12, 31),
                SavingsGoalSource.BALANCE,
                null
        );
        goal.setCurrentAmount(6_000_000);

        assertEquals(SavingsGoalStatus.NEAR_TARGET, goal.getStatus());
    }
}
