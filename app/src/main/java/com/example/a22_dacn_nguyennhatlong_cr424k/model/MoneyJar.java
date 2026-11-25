package com.example.a22_dacn_nguyennhatlong_cr424k.model;

import java.io.Serializable;

public class MoneyJar implements Serializable {
    private int id;
    private String name;          // Tên hũ (VD: Tiền trọ, Ăn uống, Tiết kiệm)
    private double targetAmount;  // Mục tiêu / Hạn mức của hũ
    private double currentAmount; // Số dư hiện tại trong hũ

    public MoneyJar(int id, String name, double targetAmount, double currentAmount) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    @Override
    public String toString() {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return name + " (Còn: " + df.format(currentAmount) + " đ)";
    }
}