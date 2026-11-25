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

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "MemeAppExpense.db";
    private static final int DB_VERSION = 3;

    private static final String TABLE_TRANS = "transactions";
    private static final String TABLE_JARS = "money_jars";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng 1: Hũ tiền phân bổ (Timo Money Jars)
        String sqlJars = "CREATE TABLE " + TABLE_JARS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "targetAmount REAL, " +
                "currentAmount REAL)";
        db.execSQL(sqlJars);

        // Tạo sẵn 3 hũ mẫu ban đầu
        db.execSQL("INSERT INTO " + TABLE_JARS + " (name, targetAmount, currentAmount) VALUES ('Ví chính', 10000000, 0)");
        db.execSQL("INSERT INTO " + TABLE_JARS + " (name, targetAmount, currentAmount) VALUES ('Ăn uống & Sinh hoạt', 3000000, 0)");
        db.execSQL("INSERT INTO " + TABLE_JARS + " (name, targetAmount, currentAmount) VALUES ('Quỹ tiết kiệm', 5000000, 0)");

        // Bảng 2: Sổ giao dịch (liên kết jarId)
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JARS);
        onCreate(db);
    }

    // ================= QUẢN LÝ HŨ TIỀN (TIMO) =================

    public long addJar(String name, double targetAmount, double initialAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("targetAmount", targetAmount);
        values.put("currentAmount", initialAmount);
        long res = db.insert(TABLE_JARS, null, values);
        db.close();
        return res;
    }

    public List<MoneyJar> getAllJars() {
        List<MoneyJar> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_JARS + " ORDER BY id ASC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new MoneyJar(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getDouble(3)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void updateJarBalance(int jarId, double deltaAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_JARS + " SET currentAmount = currentAmount + ? WHERE id = ?",
                new Object[]{deltaAmount, jarId});
        db.close();
    }

    public void deleteJar(int jarId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_JARS, "id = ?", new String[]{String.valueOf(jarId)});
        db.close();
    }

    // ================= QUẢN LÝ GIAO DỊCH =================

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

        // Tự động cộng/trừ số dư của hũ tương ứng
        double delta = t.getType().equals("THU") ? t.getAmount() : -t.getAmount();
        updateJarBalance(t.getJarId(), delta);
        return result;
    }

    public int updateTransaction(Transaction oldT, Transaction newT) {
        // Hoàn tác số dư ở hũ cũ
        double revertDelta = oldT.getType().equals("THU") ? -oldT.getAmount() : oldT.getAmount();
        updateJarBalance(oldT.getJarId(), revertDelta);

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

        // Cập nhật số dư vào hũ mới
        double applyDelta = newT.getType().equals("THU") ? newT.getAmount() : -newT.getAmount();
        updateJarBalance(newT.getJarId(), applyDelta);
        return rows;
    }

    public List<Transaction> getTransactions(String filterType, String keyword) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_TRANS + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (!filterType.equals("ALL")) {
            query.append(" AND type = ?");
            args.add(filterType);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND (title LIKE ? OR category LIKE ? OR jarName LIKE ?)");
            args.add("%" + keyword.trim() + "%");
            args.add("%" + keyword.trim() + "%");
            args.add("%" + keyword.trim() + "%");
        }
        query.append(" ORDER BY id DESC");

        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                list.add(new Transaction(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getString(8)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void deleteTransaction(Transaction t) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANS, "id = ?", new String[]{String.valueOf(t.getId())});
        db.close();

        // Hoàn trả lại số dư cho hũ khi xóa giao dịch
        double revertDelta = t.getType().equals("THU") ? -t.getAmount() : t.getAmount();
        updateJarBalance(t.getJarId(), revertDelta);
    }

    public double getTotalByType(String type) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_TRANS + " WHERE type = ?", new String[]{type});
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    public Map<String, Double> getExpenseByCategory() {
        Map<String, Double> map = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT category, SUM(amount) as total FROM " + TABLE_TRANS +
                        " WHERE type = 'CHI' GROUP BY category ORDER BY total DESC", null);
        if (cursor.moveToFirst()) {
            do {
                map.put(cursor.getString(0), cursor.getDouble(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return map;
    }
}