package com.app.uangku.model;

import java.time.LocalDate;

public class Transaction {
    private int idTransaction;
    private int idUser;
    private int idCategory;
    private double amount;
    private LocalDate date;
    private String description;
    private TransactionType type;
    private String categoryName;

    public Transaction() {
    }

    public Transaction(
            int idUser,
            int idCategory,
            double amount,
            LocalDate date,
            String description,
            TransactionType type
    ) {
        this(0, idUser, idCategory, amount, date, description, type, null);
    }

    public Transaction(
            int idTransaction,
            int idUser,
            int idCategory,
            double amount,
            LocalDate date,
            String description,
            TransactionType type,
            String categoryName
    ) {
        this.idTransaction = idTransaction;
        this.idUser = idUser;
        this.idCategory = idCategory;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.type = type;
        this.categoryName = categoryName;
    }

    public int getIdTransaction() {
        return idTransaction;
    }

    public void setIdTransaction(int idTransaction) {
        this.idTransaction = idTransaction;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
