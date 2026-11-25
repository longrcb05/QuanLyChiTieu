package com.example.a22_dacn_nguyennhatlong_cr424k.model;

import java.io.Serializable;

public class Transaction implements Serializable {
    private int id;
    private String title;
    private double amount;
    private String type;       // "THU" hoặc "CHI"
    private String category;
    private String date;
    private String imagePath;
    private int jarId;         // ID của hũ tiền liên kết
    private String jarName;    // Tên hũ tiền hiển thị

    public Transaction(int id, String title, double amount, String type, String category, String date, String imagePath, int jarId, String jarName) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.imagePath = imagePath;
        this.jarId = jarId;
        this.jarName = jarName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public int getJarId() { return jarId; }
    public void setJarId(int jarId) { this.jarId = jarId; }
    public String getJarName() { return jarName; }
    public void setJarName(String jarName) { this.jarName = jarName; }
}