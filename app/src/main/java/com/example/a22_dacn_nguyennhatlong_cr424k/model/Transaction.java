package com.example.a22_dacn_nguyennhatlong_cr424k.model;

import java.io.Serializable;
public class Transaction implements Serializable {
    // Khai báo các thuộc tính (Fields)
    private int id;             // Mã định danh giao dịch (Khóa chính tự tăng trong SQLite)
    private String title;       // Nội dung / Tên giao dịch (VD: Ăn trưa, Tiền lương)
    private double amount;      // Số tiền của giao dịch
    private String type;        // Loại giao dịch ("thu" hoặc "chi")
    private String category;    // Danh mục phân loại (VD: Ăn uống, Di chuyển, Lương...)
    private String date;        // Ngày thực hiện giao dịch (định dạng d/M/yyyy)
    private String imagePath;   // Đường dẫn lưu file ảnh hóa đơn chụp trong bộ nhớ máy
    private int jarId;          // Mã của Hũ chi tiêu liên kết (Khóa ngoại trỏ tới bảng money_jars)
    private String jarName;     // Tên của Hũ chi tiêu để hiển thị nhanh lên giao diện

    // Hàm khởi tạo
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

    // Các phương thức Getter và Setter để truy xuất và cập nhật dữ liệu
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getJarId() {
        return jarId;
    }

    public void setJarId(int jarId) {
        this.jarId = jarId;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }
}