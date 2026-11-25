package com.example.a22_dacn_nguyennhatlong_cr424k;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a22_dacn_nguyennhatlong_cr424k.adapter.CalendarDayAdapter;
import com.example.a22_dacn_nguyennhatlong_cr424k.adapter.JarAdapter;
import com.example.a22_dacn_nguyennhatlong_cr424k.adapter.TransactionAdapter;
import com.example.a22_dacn_nguyennhatlong_cr424k.database.DatabaseHelper;
import com.example.a22_dacn_nguyennhatlong_cr424k.model.CalendarDayItem;
import com.example.a22_dacn_nguyennhatlong_cr424k.model.MoneyJar;
import com.example.a22_dacn_nguyennhatlong_cr424k.model.Transaction;
import com.example.a22_dacn_nguyennhatlong_cr424k.view.PieChartView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView tvBalance, tvTotalIncome, tvTotalExpense, tvTodayExpenseBadge, tvListHeader;
    private Button btnAddJar, btnPrevMonth, btnNextMonth;
    private LinearLayout layoutTabCalendar, layoutTabTransactions, layoutTabJars, layoutStatsLegend;
    private View layoutTabStats;
    private PieChartView pieChartView;
    private EditText edtSearch;
    private ListView lvTransactions, lvJars, lvDayTransactions;
    private TextView tvCurrentMonthYear, tvSelectedDayTitle;
    private GridView gvCalendar;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAddTransaction;

    private Calendar currentCalendar;
    private String selectedDateStr;
    private String todayDateStr;
    private CalendarDayAdapter calendarAdapter;

    private DatabaseHelper dbHelper;
    private List<Transaction> transactionList;
    private List<MoneyJar> jarList;
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Meme App");

        tvBalance = findViewById(R.id.tvBalance);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvTodayExpenseBadge = findViewById(R.id.tvTodayExpenseBadge);
        tvListHeader = findViewById(R.id.tvListHeader);

        layoutTabCalendar = findViewById(R.id.layoutTabCalendar);
        layoutTabTransactions = findViewById(R.id.layoutTabTransactions);
        layoutTabJars = findViewById(R.id.layoutTabJars);
        layoutTabStats = findViewById(R.id.layoutTabStats);
        pieChartView = findViewById(R.id.pieChartView);
        layoutStatsLegend = findViewById(R.id.layoutStatsLegend);

        btnAddJar = findViewById(R.id.btnAddJar);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        edtSearch = findViewById(R.id.edtSearch);
        lvTransactions = findViewById(R.id.lvTransactions);
        lvJars = findViewById(R.id.lvJars);
        lvDayTransactions = findViewById(R.id.lvDayTransactions);
        tvCurrentMonthYear = findViewById(R.id.tvCurrentMonthYear);
        tvSelectedDayTitle = findViewById(R.id.tvSelectedDayTitle);
        gvCalendar = findViewById(R.id.gvCalendar);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);

        dbHelper = new DatabaseHelper(this);

        // Khởi tạo ngày hôm nay
        currentCalendar = Calendar.getInstance();
        todayDateStr = currentCalendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (currentCalendar.get(Calendar.MONTH) + 1) + "/" +
                currentCalendar.get(Calendar.YEAR);
        selectedDateStr = todayDateStr;

        // Nút + nổi mở màn hình Thêm Giao Dịch
        fabAddTransaction.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddTransactionActivity.class));
        });

        // Điều khiển thanh Menu dưới cùng
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home_calendar) {
                showScreen(0);
                return true;
            } else if (itemId == R.id.nav_transactions) {
                showScreen(1);
                return true;
            } else if (itemId == R.id.nav_jars) {
                showScreen(2);
                return true;
            } else if (itemId == R.id.nav_stats) {
                showScreen(3);
                return true;
            }
            return false;
        });

        // Mặc định mở Trang chủ (Lịch) ngay khi vào app
        bottomNavigation.setSelectedItemId(R.id.nav_home_calendar);

        btnAddJar.setOnClickListener(v -> showCreateJarDialog());

        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            loadCalendarData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            loadCalendarData();
        });

        gvCalendar.setOnItemClickListener((parent, view, position, id) -> {
            CalendarDayItem clickedDay = (CalendarDayItem) calendarAdapter.getItem(position);
            if (clickedDay.getDayNumber() != 0) {
                selectedDateStr = clickedDay.getFullDate();
                calendarAdapter.setSelectedDate(selectedDateStr);
                showTransactionsForSelectedDay(clickedDay);
            }
        });

        // Chạm giao dịch trong danh sách ngày trên lịch -> Sửa
        lvDayTransactions.setOnItemClickListener((parent, view, position, id) -> {
            Transaction selected = (Transaction) lvDayTransactions.getAdapter().getItem(position);
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            intent.putExtra("EDIT_TRANSACTION", selected);
            startActivity(intent);
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { loadAllData(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        lvTransactions.setOnItemClickListener((parent, view, position, id) -> {
            Transaction selected = transactionList.get(position);
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            intent.putExtra("EDIT_TRANSACTION", selected);
            startActivity(intent);
        });

        lvTransactions.setOnItemLongClickListener((parent, view, position, id) -> {
            Transaction selected = transactionList.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Xóa giao dịch '" + selected.getTitle() + "' và hoàn lại tiền vào hũ?")
                    .setPositiveButton("Xóa", (d, w) -> {
                        dbHelper.deleteTransaction(selected);
                        loadAllData();
                        Toast.makeText(this, "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return true;
        });

        lvJars.setOnItemClickListener((parent, view, position, id) -> {
            MoneyJar jar = jarList.get(position);
            showTopUpJarDialog(jar);
        });

        // Nút Back hệ thống: nếu đang ở tab khác thì quay về Trang chủ (Lịch)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNavigation.getSelectedItemId() != R.id.nav_home_calendar) {
                    bottomNavigation.setSelectedItemId(R.id.nav_home_calendar);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void showScreen(int index) {
        layoutTabCalendar.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        layoutTabTransactions.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        layoutTabJars.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        layoutTabStats.setVisibility(index == 3 ? View.VISIBLE : View.GONE);

        // Ẩn nút + nổi khi ở màn hình Thống kê để giao diện thoáng
        if (index == 3) {
            fabAddTransaction.hide();
            updateStatsView();
        } else {
            fabAddTransaction.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllData();
    }

    private void loadAllData() {
        String keyword = edtSearch.getText().toString();
        transactionList = dbHelper.getTransactions(currentFilter, keyword);
        lvTransactions.setAdapter(new TransactionAdapter(this, transactionList));

        jarList = dbHelper.getAllJars();
        lvJars.setAdapter(new JarAdapter(this, jarList, jar -> {
            dbHelper.deleteJar(jar.getId());
            loadAllData();
            Toast.makeText(this, "Đã xóa hũ '" + jar.getName() + "'!", Toast.LENGTH_SHORT).show();
        }));

        double income = dbHelper.getTotalByType("THU");
        double expense = dbHelper.getTotalByType("CHI");
        double balance = income - expense;

        DecimalFormat df = new DecimalFormat("#,###");
        tvTotalIncome.setText("Tổng Thu: " + df.format(income) + " đ");
        tvTotalExpense.setText("Tổng Chi: " + df.format(expense) + " đ");
        tvBalance.setText(df.format(balance) + " đ");

        // Tính tổng chi tiêu hôm nay hiển thị trên thẻ tổng quan
        List<Transaction> allTrans = dbHelper.getTransactions("ALL", "");
        double todaySpent = 0;
        for (int i = 0; i < allTrans.size(); i++) {
            Transaction t = allTrans.get(i);
            if (t.getType().equals("CHI") && todayDateStr.equals(t.getDate())) {
                todaySpent += t.getAmount();
            }
        }
        tvTodayExpenseBadge.setText("🛒 Hôm nay chi: " + df.format(todaySpent) + " đ");

        loadCalendarData();
        updateStatsView();
    }

    private void loadCalendarData() {
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        tvCurrentMonthYear.setText("Tháng " + month + " / " + year);

        List<Transaction> allTransactions = dbHelper.getTransactions("ALL", "");
        List<CalendarDayItem> days = new ArrayList<>();

        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
        int emptyCells = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : (firstDayOfWeek - 2);

        for (int i = 0; i < emptyCells; i++) {
            days.add(new CalendarDayItem(0, ""));
        }

        int maxDaysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        CalendarDayItem defaultSelectedItem = null;

        for (int day = 1; day <= maxDaysInMonth; day++) {
            String dateKey = day + "/" + month + "/" + year;
            CalendarDayItem dayItem = new CalendarDayItem(day, dateKey);

            List<Transaction> dayTrans = new ArrayList<>();
            for (int j = 0; j < allTransactions.size(); j++) {
                Transaction t = allTransactions.get(j);
                if (t.getDate() != null && t.getDate().equals(dateKey)) {
                    dayTrans.add(t);
                }
            }
            dayItem.setTransactions(dayTrans);
            days.add(dayItem);

            if (dateKey.equals(selectedDateStr)) {
                defaultSelectedItem = dayItem;
            }
        }

        calendarAdapter = new CalendarDayAdapter(this, days, selectedDateStr);
        gvCalendar.setAdapter(calendarAdapter);

        if (defaultSelectedItem != null) {
            showTransactionsForSelectedDay(defaultSelectedItem);
        } else {
            tvSelectedDayTitle.setText("Ngày " + selectedDateStr + ": Không có giao dịch");
            lvDayTransactions.setAdapter(new TransactionAdapter(this, new ArrayList<>()));
        }
    }

    private void showTransactionsForSelectedDay(CalendarDayItem dayItem) {
        List<Transaction> list = dayItem.getTransactions();
        if (list.isEmpty()) {
            tvSelectedDayTitle.setText("Ngày " + dayItem.getFullDate() + ": Không có giao dịch");
        } else {
            tvSelectedDayTitle.setText("Ngày " + dayItem.getFullDate() + " (" + list.size() + " giao dịch):");
        }
        lvDayTransactions.setAdapter(new TransactionAdapter(this, list));
    }

    private void updateStatsView() {
        Map<String, Double> stats = dbHelper.getExpenseByCategory();
        double totalExpense = dbHelper.getTotalByType("CHI");

        pieChartView.setData(stats, totalExpense);
        layoutStatsLegend.removeAllViews();

        if (stats.isEmpty() || totalExpense == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có dữ liệu khoản chi để thống kê.");
            tvEmpty.setTextSize(14f);
            tvEmpty.setTextColor(Color.parseColor("#757575"));
            tvEmpty.setGravity(Gravity.CENTER);
            layoutStatsLegend.addView(tvEmpty);
            return;
        }

        DecimalFormat df = new DecimalFormat("#,###");
        int[] colors = PieChartView.CHART_COLORS;
        int index = 0;

        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            double amount = entry.getValue();
            double pct = (amount / totalExpense) * 100.0;
            int sliceColor = colors[index % colors.length];
            index++;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(8, 12, 8, 12);

            View colorDot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(28, 28);
            dotParams.setMarginEnd(20);
            colorDot.setLayoutParams(dotParams);
            colorDot.setBackgroundColor(sliceColor);
            row.addView(colorDot);

            TextView tvCat = new TextView(this);
            LinearLayout.LayoutParams catParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvCat.setLayoutParams(catParams);
            tvCat.setText(entry.getKey());
            tvCat.setTextSize(14f);
            tvCat.setTextColor(Color.parseColor("#212121"));
            row.addView(tvCat);

            TextView tvVal = new TextView(this);
            tvVal.setText(df.format(amount) + " đ (" + String.format(Locale.US, "%.1f", pct) + "%)");
            tvVal.setTextSize(14f);
            tvVal.setTypeface(null, Typeface.BOLD);
            tvVal.setTextColor(sliceColor);
            row.addView(tvVal);

            layoutStatsLegend.addView(row);
        }
    }

    private void showCreateJarDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        EditText edtName = new EditText(this);
        edtName.setHint("Tên hũ (VD: Mua Laptop, Tiền trọ...)");
        layout.addView(edtName);

        EditText edtTarget = new EditText(this);
        edtTarget.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtTarget.setHint("Mục tiêu / Hạn mức hũ (VNĐ)");
        layout.addView(edtTarget);

        new AlertDialog.Builder(this)
                .setTitle("Tạo Hũ Chi Tiêu Mới")
                .setView(layout)
                .setPositiveButton("Tạo Hũ", (d, w) -> {
                    String name = edtName.getText().toString().trim();
                    String targetStr = edtTarget.getText().toString().trim();
                    if (!name.isEmpty() && !targetStr.isEmpty()) {
                        double target = Double.parseDouble(targetStr);
                        dbHelper.addJar(name, target, 0);
                        loadAllData();
                        Toast.makeText(this, "Đã tạo hũ '" + name + "'!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Vui lòng nhập đủ tên và mục tiêu!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showTopUpJarDialog(MoneyJar jar) {
        EditText edtAmount = new EditText(this);
        edtAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtAmount.setHint("Nhập số tiền muốn nạp thêm (VNĐ)");

        new AlertDialog.Builder(this)
                .setTitle("Nạp tiền vào hũ: " + jar.getName())
                .setView(edtAmount)
                .setPositiveButton("Nạp tiền", (d, w) -> {
                    String val = edtAmount.getText().toString().trim();
                    if (!val.isEmpty()) {
                        double addVal = Double.parseDouble(val);
                        dbHelper.updateJarBalance(jar.getId(), addVal);
                        loadAllData();
                        Toast.makeText(this, "Đã nạp tiền vào hũ!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_stats) {
            bottomNavigation.setSelectedItemId(R.id.nav_stats);
        } else if (id == R.id.menu_filter_all) {
            currentFilter = "ALL";
            tvListHeader.setText("Tất cả các giao dịch (Chạm để sửa, giữ để xóa)");
            bottomNavigation.setSelectedItemId(R.id.nav_transactions);
            loadAllData();
        } else if (id == R.id.menu_filter_income) {
            currentFilter = "THU";
            tvListHeader.setText("Danh sách KHOẢN THU");
            bottomNavigation.setSelectedItemId(R.id.nav_transactions);
            loadAllData();
        } else if (id == R.id.menu_filter_expense) {
            currentFilter = "CHI";
            tvListHeader.setText("Danh sách KHOẢN CHI");
            bottomNavigation.setSelectedItemId(R.id.nav_transactions);
            loadAllData();
        }
        return super.onOptionsItemSelected(item);
    }
}