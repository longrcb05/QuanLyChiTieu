package com.example.a22_dacn_nguyennhatlong_cr424k.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.a22_dacn_nguyennhatlong_cr424k.model.MoneyJar;
import com.example.a22_dacn_nguyennhatlong_cr424k.model.Transaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Lớp quản lý cơ sở dữ liệu SQLite của ứng dụng
public class DatabaseHelper extends SQLiteOpenHelper {
    // Khai báo tên file CSDL và phiên bản
    private static final String DB_NAME = "MemeAppExpense.db";
    private static final int DB_VERSION = 3;

    // Khai báo tên các bảng trong CSDL
    private static final String TABLE_TRANS = "transactions";
    private static final String TABLE_JARS = "money_jars";

    // Hàm khởi tạo
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Phương thức tự động chạy khi CSDL được tạo lần đầu tiên
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Tạo bảng lưu trữ các Hũ chi tiêu
        String sqlJars = "CREATE TABLE " + TABLE_JARS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "targetAmount REAL, " +
                "currentAmount REAL)";
        db.execSQL(sqlJars);

        // Thêm sẵn 3 hũ chi tiêu mẫu ban đầu
        db.execSQL("INSERT INTO " + TABLE_JARS + " (name, targetAmount, currentAmount) VALUES ('Ví chính', 10000000, 0)");
        db.execSQL("INSERT INTO " + TABLE_JARS + " (name, targetAmount, currentAmount) VALUES ('Ăn uống & Sinh hoạt', 3000000, 0)");
        db.execSQL("INSERT INTO " + TABLE_JARS + " (name, targetAmount, currentAmount) VALUES ('Quỹ tiết kiệm', 5000000, 0)");

        // 2. Tạo bảng lưu trữ danh sách Giao dịch (có khóa ngoại jarId liên kết tới bảng Hũ)
        String sqlTrans = "CREATE TABLE " + TABLE_TRANS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "amount REAL, " +
                "type TEXT, " +
                "category TEXT, " +
                "date TEXT, " +
                "imagePath TEXT, " +
                "jarId INTEGER, " +
                "jarName TEXT)";
        db.execSQL(sqlTrans);
    }

    // Phương thức chạy khi nâng cấp phiên bản DB_VERSION
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JARS);
        onCreate(db);
    }

    // PHẦN 1: QUẢN LÝ HŨ CHI TIÊU
    // Thêm một hũ chi tiêu mới vào bảng money_jars
    public long addJar(String name, double targetAmount, double initialAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("targetAmount", targetAmount);
        values.put("currentAmount", initialAmount);

        long result = db.insert(TABLE_JARS, null, values);
        db.close();
        return result;
    }

    // Lấy toàn bộ danh sách hũ chi tiêu hiện có
    public List<MoneyJar> getAllJars() {
        List<MoneyJar> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE_JARS + " ORDER BY id ASC";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                double targetAmount = cursor.getDouble(2);
                double currentAmount = cursor.getDouble(3);

                MoneyJar jar = new MoneyJar(id, name, targetAmount, currentAmount);
                list.add(jar);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    // Cập nhật cộng hoặc trừ số dư của hũ theo mã jarId
    public void updateJarBalance(int jarId, double deltaAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE " + TABLE_JARS + " SET currentAmount = currentAmount + ? WHERE id = ?";
        db.execSQL(sql, new Object[]{deltaAmount, jarId});
        db.close();
    }

    // Xóa một hũ chi tiêu khỏi CSDL
    public void deleteJar(int jarId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_JARS, "id = ?", new String[]{String.valueOf(jarId)});
        db.close();
    }

    // PHẦN 2: QUẢN LÝ GIAO DỊCH THU/CHI
    // Thêm giao dịch mới và tự động cập nhật lại tiền trong hũ tương ứng
    public long addTransaction(Transaction t) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", t.getTitle());
        values.put("amount", t.getAmount());
        values.put("type", t.getType());
        values.put("category", t.getCategory());
        values.put("date", t.getDate());
        values.put("imagePath", t.getImagePath());
        values.put("jarId", t.getJarId());
        values.put("jarName", t.getJarName());

        long result = db.insert(TABLE_TRANS, null, values);
        db.close();

        // Nếu là khoản thu thì cộng thêm vào hũ, khoản chi thì trừ đi
        double delta;
        if (t.getType().equals("THU")) {
            delta = t.getAmount();
        } else {
            delta = -t.getAmount();
        }
        updateJarBalance(t.getJarId(), delta);

        return result;
    }

    // Cập nhật thông tin giao dịch đã có và tính lại số dư cho hũ
    public int updateTransaction(Transaction oldT, Transaction newT) {
        // Bước 1: Hoàn trả lại số tiền của giao dịch cũ về hũ cũ
        double revertDelta;
        if (oldT.getType().equals("THU")) {
            revertDelta = -oldT.getAmount();
        } else {
            revertDelta = oldT.getAmount();
        }
        updateJarBalance(oldT.getJarId(), revertDelta);

        // Bước 2: Cập nhật dữ liệu mới vào bảng transactions
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", newT.getTitle());
        values.put("amount", newT.getAmount());
        values.put("type", newT.getType());
        values.put("category", newT.getCategory());
        values.put("date", newT.getDate());
        values.put("imagePath", newT.getImagePath());
        values.put("jarId", newT.getJarId());
        values.put("jarName", newT.getJarName());

        int rows = db.update(TABLE_TRANS, values, "id = ?", new String[]{String.valueOf(newT.getId())});
        db.close();

        // Bước 3: Áp dụng số tiền mới vào hũ mới được chọn
        double applyDelta;
        if (newT.getType().equals("THU")) {
            applyDelta = newT.getAmount();
        } else {
            applyDelta = -newT.getAmount();
        }
        updateJarBalance(newT.getJarId(), applyDelta);

        return rows;
    }

    // Lấy danh sách giao dịch kết hợp lọc theo loại (all/thu/chi) và từ khóa tìm kiếm
    public List<Transaction> getTransactions(String filterType, String keyword) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_TRANS + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        // Điều kiện lọc theo loại giao dịch
        if (!filterType.equals("ALL")) {
            query.append(" AND type = ?");
            args.add(filterType);
        }

        // Điều kiện tìm kiếm theo tiêu đề, danh mục hoặc tên hũ
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND (title LIKE ? OR category LIKE ? OR jarName LIKE ?)");
            String searchPattern = "%" + keyword.trim() + "%";
            args.add(searchPattern);
            args.add(searchPattern);
            args.add(searchPattern);
        }

        query.append(" ORDER BY id DESC");

        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                Transaction t = new Transaction(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getString(8)
                );
                list.add(t);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    // Xóa giao dịch và hoàn trả lại số tiền vào hũ tương ứng
    public void deleteTransaction(Transaction t) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANS, "id = ?", new String[]{String.valueOf(t.getId())});
        db.close();

        // Hoàn lại số dư cho hũ
        double revertDelta;
        if (t.getType().equals("THU")) {
            revertDelta = -t.getAmount();
        } else {
            revertDelta = t.getAmount();
        }
        updateJarBalance(t.getJarId(), revertDelta);
    }

    // PHẦN 3: TRUY VẤN THỐNG KÊ TÀI CHÍNH
    // Tính tổng tiền theo loại giao dịch ("thu" hoặc "chi")
    public double getTotalByType(String type) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT SUM(amount) FROM " + TABLE_TRANS + " WHERE type = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{type});

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return total;
    }

    // Gom nhóm và tính tổng tiền CHI theo từng danh mục để vẽ biểu đồ tròn
    public Map<String, Double> getExpenseByCategory() {
        Map<String, Double> map = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT category, SUM(amount) as total FROM " + TABLE_TRANS +
                " WHERE type = 'CHI' GROUP BY category ORDER BY total DESC";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                double sumAmount = cursor.getDouble(1);
                map.put(category, sumAmount);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return map;
    }
}