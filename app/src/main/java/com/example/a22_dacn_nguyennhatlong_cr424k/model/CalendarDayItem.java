package com.example.a22_dacn_nguyennhatlong_cr424k.model;

import java.util.ArrayList;
import java.util.List;

// Lớp đối tượng (Model) đại diện cho một ô ngày trên lưới lịch
public class CalendarDayItem {
    // Khai báo các thuộc tính (Fields)
    private int dayNumber;                  // Số ngày trong tháng (0 là ô trống đầu tháng, 1..31 là ngày hợp lệ)
    private String fullDate;                // Chuỗi ngày đầy đủ theo định dạng d/M/yyyy (VD: 20/11/2026)
    private List<Transaction> transactions; // Danh sách các giao dịch phát sinh trong ngày đó

    // Hàm khởi tạo (Constructor)
    public CalendarDayItem(int dayNumber, String fullDate) {
        this.dayNumber = dayNumber;
        this.fullDate = fullDate;
        this.transactions = new ArrayList<>();
    }

    // Các phương thức Getter và Setter để truy xuất và cập nhật dữ liệu
    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getFullDate() {
        return fullDate;
    }

    public void setFullDate(String fullDate) {
        this.fullDate = fullDate;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}