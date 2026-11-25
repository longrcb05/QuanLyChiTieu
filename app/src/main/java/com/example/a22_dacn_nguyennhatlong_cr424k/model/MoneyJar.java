package com.example.a22_dacn_nguyennhatlong_cr424k.model;

import java.io.Serializable;
import java.text.DecimalFormat;

public class MoneyJar implements Serializable {
    // Khai báo các thuộc tính (Fields)
    private int id;                 // Mã định danh của hũ (Khóa chính trong SQLite)
    private String name;            // Tên hũ chi tiêu (VD: Ví chính, Ăn uống, Tiết kiệm)
    private double targetAmount;    // Mục tiêu hoặc hạn mức tối đa của hũ
    private double currentAmount;   // Số dư tiền hiện tại đang có trong hũ

    // Hàm khởi tạo (Constructor) đầy đủ tham số
    public MoneyJar(int id, String name, double targetAmount, double currentAmount) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
    }

    // Các phương thức Getter và Setter để truy xuất và cập nhật dữ liệu
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    // Ghi đè phương thức toString() để hiển thị tên hũ kèm số dư lên Spinner
    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#,###");
        return name + " (Còn: " + df.format(currentAmount) + " đ)";
    }
}