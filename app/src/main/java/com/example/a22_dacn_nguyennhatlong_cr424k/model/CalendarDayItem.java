package com.example.a22_dacn_nguyennhatlong_cr424k.model;

import java.util.ArrayList;
import java.util.List;

public class CalendarDayItem {
    private int dayNumber;         // 0 nếu là ô trống đầu tháng, 1..31 nếu là ngày hợp lệ
    private String fullDate;       // Định dạng d/M/yyyy (VD: 26/11/2026)
    private List<Transaction> transactions;

    public CalendarDayItem(int dayNumber, String fullDate) {
        this.dayNumber = dayNumber;
        this.fullDate = fullDate;
        this.transactions = new ArrayList<>();
    }

    public int getDayNumber() { return dayNumber; }
    public String getFullDate() { return fullDate; }
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
}