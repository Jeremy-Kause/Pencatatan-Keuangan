package com.app.uangku.model;

import java.time.YearMonth;

public class Budget {
    private int idBudget;
    private int idUser;
    private int idCategory;
    private double limitAmount;
    private YearMonth monthYear;
    private String categoryName;
    private double usedAmount;

    public Budget() {
    }

    public Budget(int idUser, int idCategory, double limitAmount, YearMonth monthYear) {
        this(0, idUser, idCategory, limitAmount, monthYear, null, 0);
    }

    public Budget(
            int idBudget,
            int idUser,
            int idCategory,
            double limitAmount,
            YearMonth monthYear
    ) {
        this(idBudget, idUser, idCategory, limitAmount, monthYear, null, 0);
    }

    public Budget(
            int idBudget,
            int idUser,
            int idCategory,
            double limitAmount,
            YearMonth monthYear,
            String categoryName,
            double usedAmount
    ) {
        this.idBudget = idBudget;
        this.idUser = idUser;
        this.idCategory = idCategory;
        this.limitAmount = limitAmount;
        this.monthYear = monthYear;
        this.categoryName = categoryName;
        this.usedAmount = usedAmount;
    }

    public int getIdBudget() {
        return idBudget;
    }

    public void setIdBudget(int idBudget) {
        this.idBudget = idBudget;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public YearMonth getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(YearMonth monthYear) {
        this.monthYear = monthYear;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getUsedAmount() {
        return usedAmount;
    }

    public void setUsedAmount(double usedAmount) {
        this.usedAmount = usedAmount;
    }

    public double getRemainingAmount() {
        return limitAmount - usedAmount;
    }

    public double getUsagePercentage() {
        if (limitAmount <= 0) {
            return usedAmount > 0 ? 100 : 0;
        }

        return (usedAmount / limitAmount) * 100;
    }

    public BudgetStatus getStatus() {
        if (usedAmount > limitAmount) {
            return BudgetStatus.MELEBIHI_LIMIT;
        }

        if (getUsagePercentage() >= 80) {
            return BudgetStatus.MENDEKATI_LIMIT;
        }

        return BudgetStatus.AMAN;
    }
}
