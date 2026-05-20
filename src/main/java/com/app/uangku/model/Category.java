package com.app.uangku.model;

public class Category {
    private int idCategory;
    private int idUser;
    private String name;
    private TransactionType type;

    public Category() {
    }

    public Category(int idUser, String name, TransactionType type) {
        this(0, idUser, name, type);
    }

    public Category(int idCategory, int idUser, String name, TransactionType type) {
        this.idCategory = idCategory;
        this.idUser = idUser;
        this.name = name;
        this.type = type;
    }

    public int getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
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

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}
