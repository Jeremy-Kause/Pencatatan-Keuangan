package com.app.uangku.model;

import java.time.LocalDate;

public class SavingsGoal {
    private int idGoal;
    private int idUser;
    private String name;
    private double targetAmount;
    private LocalDate targetDate;
    private SavingsGoalSource progressSource;
    private String description;
    private String createdAt;
    private double currentAmount;

    public SavingsGoal() {
    }

    public SavingsGoal(int idUser, String name, double targetAmount, LocalDate targetDate, SavingsGoalSource progressSource, String description) {
        this(0, idUser, name, targetAmount, targetDate, progressSource, description, null, 0);
    }

    public SavingsGoal(
            int idGoal,
            int idUser,
            String name,
            double targetAmount,
            LocalDate targetDate,
            SavingsGoalSource progressSource,
            String description,
            String createdAt,
            double currentAmount
    ) {
        this.idGoal = idGoal;
        this.idUser = idUser;
        this.name = name;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.progressSource = progressSource;
        this.description = description;
        this.createdAt = createdAt;
        this.currentAmount = currentAmount;
    }

    public int getIdGoal() {
        return idGoal;
    }

    public void setIdGoal(int idGoal) {
        this.idGoal = idGoal;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public SavingsGoalSource getProgressSource() {
        return progressSource;
    }

    public void setProgressSource(SavingsGoalSource progressSource) {
        this.progressSource = progressSource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public double getProgressPercentage() {
        if (targetAmount <= 0) {
            return currentAmount > 0 ? 100 : 0;
        }
        return Math.min(100, (currentAmount / targetAmount) * 100);
    }

    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }

    public SavingsGoalStatus getStatus() {
        if (currentAmount >= targetAmount && targetAmount > 0) {
            return SavingsGoalStatus.ACHIEVED;
        }
        if (getProgressPercentage() >= 70) {
            return SavingsGoalStatus.NEAR_TARGET;
        }
        return SavingsGoalStatus.ON_TRACK;
    }
}
