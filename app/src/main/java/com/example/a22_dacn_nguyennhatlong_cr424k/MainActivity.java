package com.example.a22_dacn_nguyennhatlong_cr424k;

import android.content.DialogInterface;
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
import android.widget.AdapterView;
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
import com.google.android.material.navigation.NavigationBarView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Khai báo các thành phần giao diện thẻ tổng quan và tiêu đề
    private TextView tvBalance, tvTotalIncome, tvTotalExpense, tvTodayExpenseBadge, tvListHeader;

    // Khai báo các nút bấm điều hướng tháng và tạo hũ mới
    private Button btnAddJar, btnPrevMonth, btnNextMonth;

    // Khai báo các khung giao diện tương ứng với 4 tab chức năng
    private LinearLayout layoutTabCalendar, layoutTabTransactions, layoutTabJars, layoutStatsLegend;
    private View layoutTabStats;
    private PieChartView pieChartView;

    // Khai báo các thành phần tìm kiếm, danh sách (ListView) và lưới lịch (GridView)
    private EditText edtSearch;
    private ListView lvTransactions, lvJars, lvDayTransactions;
    private TextView tvCurrentMonthYear, tvSelectedDayTitle;
    private GridView gvCalendar;

    // Khai báo thanh điều hướng dưới cùng và nút nổi thêm giao dịch
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAddTransaction;

    // Khai báo các biến quản lý ngày tháng trên lịch
    private Calendar currentCalendar;
    private String selectedDateStr;
    private String todayDateStr;
    private CalendarDayAdapter calendarAdapter;

    // Khai báo đối tượng CSDL SQLite và danh sách dữ liệu
    private DatabaseHelper dbHelper;
    private List<Transaction> transactionList;
    private List<MoneyJar> jarList;
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Meme App");

        // 1. Ánh xạ các thành phần giao diện từ file activity_main.xml
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

        // 2. Khởi tạo đối tượng quản lý CSDL SQLite
        dbHelper = new DatabaseHelper(this);

        // 3. Lấy ngày hiện tại của hệ thống làm ngày mặc định khi mở ứng dụng
        currentCalendar = Calendar.getInstance();
        int todayDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
        int todayMonth = currentCalendar.get(Calendar.MONTH) + 1;
        int todayYear = currentCalendar.get(Calendar.YEAR);
        todayDateStr = todayDay + "/" + todayMonth + "/" + todayYear;
        selectedDateStr = todayDateStr;

        // 4. Bắt sự kiện nút + nổi  -> mở màn hình Thêm Giao Dịch Mới
        fabAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                startActivity(intent);
            }
        });

        // 5. Bắt sự kiện chuyển tab trên thanh menu dưới cùng
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            }
        });

        // Mặc định chọn tab Trang chủ ngay khi vừa mở app
        bottomNavigation.setSelectedItemId(R.id.nav_home_calendar);

        // 6. Bắt sự kiện nút "+ Tạo Hũ Mới"
        btnAddJar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateJarDialog();
            }
        });

        // 7. Bắt sự kiện nút lùi tháng (<) và tiến tháng (>) trên lịch
        btnPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCalendar.add(Calendar.MONTH, -1);
                loadCalendarData();
            }
        });

        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCalendar.add(Calendar.MONTH, 1);
                loadCalendarData();
            }
        });

        // 8. Bắt sự kiện khi người dùng chạm vào một ô ngày trên lưới lịch
        gvCalendar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CalendarDayItem clickedDay = (CalendarDayItem) calendarAdapter.getItem(position);
                if (clickedDay.getDayNumber() != 0) {
                    selectedDateStr = clickedDay.getFullDate();
                    calendarAdapter.setSelectedDate(selectedDateStr);
                    showTransactionsForSelectedDay(clickedDay);
                }
            }
        });

        // 9. Bắt sự kiện chạm vào giao dịch ở danh sách dưới lịch -> mở màn hình Sửa
        lvDayTransactions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Transaction selected = (Transaction) lvDayTransactions.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                intent.putExtra("EDIT_TRANSACTION", selected);
                startActivity(intent);
            }
        });

        // 10. Lắng nghe sự kiện gõ từ khóa vào ô tìm kiếm giao dịch
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadAllData();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 11. Bắt sự kiện chạm ngắn vào giao dịch trong tab Giao dịch -> mở màn hình Sửa
        lvTransactions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Transaction selected = transactionList.get(position);
                Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                intent.putExtra("EDIT_TRANSACTION", selected);
                startActivity(intent);
            }
        });

        // 12. Bắt sự kiện nhấn giữ lâu vào giao dịch -> hiện hộp thoại xác nhận Xóa
        lvTransactions.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Transaction selected = transactionList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Xác nhận xóa");
                builder.setMessage("Xóa giao dịch '" + selected.getTitle() + "' và hoàn lại tiền vào hũ?");

                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteTransaction(selected);
                        loadAllData();
                        Toast.makeText(MainActivity.this, "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Hủy", null);
                builder.show();
                return true;
            }
        });

        // 13. Bắt sự kiện chạm vào một Hũ chi tiêu -> mở hộp thoại nạp thêm tiền vào hũ
        lvJars.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MoneyJar jar = jarList.get(position);
                showTopUpJarDialog(jar);
            }
        });

        // 14. Xử lý nút Back vật lý: nếu đang ở tab phụ thì quay về Trang chủ
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

    // Phương thức ẩn/hiện các màn hình chức năng tương ứng với chỉ số tab (0..3)
    private void showScreen(int index) {
        if (index == 0) {
            layoutTabCalendar.setVisibility(View.VISIBLE);
        } else {
            layoutTabCalendar.setVisibility(View.GONE);
        }

        if (index == 1) {
            layoutTabTransactions.setVisibility(View.VISIBLE);
        } else {
            layoutTabTransactions.setVisibility(View.GONE);
        }

        if (index == 2) {
            layoutTabJars.setVisibility(View.VISIBLE);
        } else {
            layoutTabJars.setVisibility(View.GONE);
        }

        if (index == 3) {
            layoutTabStats.setVisibility(View.VISIBLE);
            fabAddTransaction.hide();
            updateStatsView();
        } else {
            layoutTabStats.setVisibility(View.GONE);
            fabAddTransaction.show();
        }
    }

    // Tự động tải lại dữ liệu mới nhất mỗi khi quay lại MainActivity
    @Override
    protected void onResume() {
        super.onResume();
        loadAllData();
    }

    // Phương thức nạp toàn bộ dữ liệu từ SQLite và cập nhật lên giao diện
    private void loadAllData() {
        // Nạp danh sách giao dịch theo bộ lọc và từ khóa
        String keyword = edtSearch.getText().toString();
        transactionList = dbHelper.getTransactions(currentFilter, keyword);
        TransactionAdapter transAdapter = new TransactionAdapter(this, transactionList);
        lvTransactions.setAdapter(transAdapter);

        // Nạp danh sách Hũ chi tiêu
        jarList = dbHelper.getAllJars();
        JarAdapter jarAdapter = new JarAdapter(this, jarList, new JarAdapter.OnJarDeleteListener() {
            @Override
            public void onDeleteJar(MoneyJar jar) {
                dbHelper.deleteJar(jar.getId());
                loadAllData();
                Toast.makeText(MainActivity.this, "Đã xóa hũ '" + jar.getName() + "'!", Toast.LENGTH_SHORT).show();
            }
        });
        lvJars.setAdapter(jarAdapter);

        // Tính Tổng Thu, Tổng Chi và Số dư hiện tại
        double income = dbHelper.getTotalByType("THU");
        double expense = dbHelper.getTotalByType("CHI");
        double balance = income - expense;

        DecimalFormat df = new DecimalFormat("#,###");
        tvTotalIncome.setText("Tổng Thu: " + df.format(income) + " đ");
        tvTotalExpense.setText("Tổng Chi: " + df.format(expense) + " đ");
        tvBalance.setText(df.format(balance) + " đ");

        // Tính tổng số tiền đã chi trong ngày hôm nay
        List<Transaction> allTrans = dbHelper.getTransactions("ALL", "");
        double todaySpent = 0;
        for (int i = 0; i < allTrans.size(); i++) {
            Transaction t = allTrans.get(i);
            if (t.getType().equals("CHI") && todayDateStr.equals(t.getDate())) {
                todaySpent += t.getAmount();
            }
        }
        tvTodayExpenseBadge.setText("🛒 Hôm nay chi: " + df.format(todaySpent) + " đ");

        // Cập nhật lại lưới lịch và biểu đồ thống kê
        loadCalendarData();
        updateStatsView();
    }

    // Phương thức tạo dữ liệu các ngày trong tháng và đổ lên lưới lịch
    private void loadCalendarData() {
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        tvCurrentMonthYear.setText("Tháng " + month + " / " + year);

        List<Transaction> allTransactions = dbHelper.getTransactions("ALL", "");
        List<CalendarDayItem> days = new ArrayList<>();

        // Đặt lịch tạm về ngày mùng 1 của tháng đang xem để tính thứ đầu tháng
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
        int emptyCells;
        if (firstDayOfWeek == Calendar.SUNDAY) {
            emptyCells = 6;
        } else {
            emptyCells = firstDayOfWeek - 2;
        }

        // Thêm các ô trống ở đầu tháng
        for (int i = 0; i < emptyCells; i++) {
            days.add(new CalendarDayItem(0, ""));
        }

        // Duyệt qua tất cả các ngày trong tháng và gom giao dịch tương ứng từng ngày
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

        // Hiển thị danh sách giao dịch của ngày đang được chọn bên dưới lịch
        if (defaultSelectedItem != null) {
            showTransactionsForSelectedDay(defaultSelectedItem);
        } else {
            tvSelectedDayTitle.setText("Ngày " + selectedDateStr + ": Không có giao dịch");
            lvDayTransactions.setAdapter(new TransactionAdapter(this, new ArrayList<Transaction>()));
        }
    }

    // Phương thức hiển thị danh sách giao dịch của một ngày cụ thể khi chạm trên lịch
    private void showTransactionsForSelectedDay(CalendarDayItem dayItem) {
        List<Transaction> list = dayItem.getTransactions();
        if (list.isEmpty()) {
            tvSelectedDayTitle.setText("Ngày " + dayItem.getFullDate() + ": Không có giao dịch");
        } else {
            tvSelectedDayTitle.setText("Ngày " + dayItem.getFullDate() + " (" + list.size() + " giao dịch):");
        }
        TransactionAdapter dayAdapter = new TransactionAdapter(this, list);
        lvDayTransactions.setAdapter(dayAdapter);
    }

    // Phương thức cập nhật dữ liệu cho Biểu đồ tròn và bảng chú thích ở tab Thống kê
    private void updateStatsView() {
        Map<String, Double> stats = dbHelper.getExpenseByCategory();
        double totalExpense = dbHelper.getTotalByType("CHI");

        // Truyền dữ liệu cho Custom View vẽ biểu đồ tròn
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

        // Duyệt từng danh mục để tạo dòng chú thích màu sắc, số tiền và phần trăm
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            double amount = entry.getValue();
            double pct = (amount / totalExpense) * 100.0;
            int sliceColor = colors[index % colors.length];
            index++;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(8, 12, 8, 12);

            // Tạo ô vuông màu đại diện cho miếng biểu đồ
            View colorDot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(28, 28);
            dotParams.setMarginEnd(20);
            colorDot.setLayoutParams(dotParams);
            colorDot.setBackgroundColor(sliceColor);
            row.addView(colorDot);

            // Tạo TextView hiển thị tên danh mục
            TextView tvCat = new TextView(this);
            LinearLayout.LayoutParams catParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvCat.setLayoutParams(catParams);
            tvCat.setText(entry.getKey());
            tvCat.setTextSize(14f);
            tvCat.setTextColor(Color.parseColor("#212121"));
            row.addView(tvCat);

            // Tạo TextView hiển thị số tiền và tỷ lệ phần trăm
            TextView tvVal = new TextView(this);
            tvVal.setText(df.format(amount) + " đ (" + String.format(Locale.US, "%.1f", pct) + "%)");
            tvVal.setTextSize(14f);
            tvVal.setTypeface(null, Typeface.BOLD);
            tvVal.setTextColor(sliceColor);
            row.addView(tvVal);

            layoutStatsLegend.addView(row);
        }
    }

    // Phương thức hiển thị hộp thoại nhập thông tin Tạo Hũ Chi Tiêu Mới
    private void showCreateJarDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        final EditText edtName = new EditText(this);
        edtName.setHint("Tên hũ (VD: Mua Laptop, Tiền trọ...)");
        layout.addView(edtName);

        final EditText edtTarget = new EditText(this);
        edtTarget.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtTarget.setHint("Mục tiêu / Hạn mức hũ (VNĐ)");
        layout.addView(edtTarget);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tạo Hũ Chi Tiêu Mới");
        builder.setView(layout);

        builder.setPositiveButton("Tạo Hũ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = edtName.getText().toString().trim();
                String targetStr = edtTarget.getText().toString().trim();
                if (!name.isEmpty() && !targetStr.isEmpty()) {
                    double target = Double.parseDouble(targetStr);
                    dbHelper.addJar(name, target, 0);
                    loadAllData();
                    Toast.makeText(MainActivity.this, "Đã tạo hũ '" + name + "'!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập đủ tên và mục tiêu!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // Phương thức hiển thị hộp thoại Nạp thêm tiền trực tiếp vào hũ
    private void showTopUpJarDialog(final MoneyJar jar) {
        final EditText edtAmount = new EditText(this);
        edtAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtAmount.setHint("Nhập số tiền muốn nạp thêm (VNĐ)");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nạp tiền vào hũ: " + jar.getName());
        builder.setView(edtAmount);

        builder.setPositiveButton("Nạp tiền", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String val = edtAmount.getText().toString().trim();
                if (!val.isEmpty()) {
                    double addVal = Double.parseDouble(val);
                    dbHelper.updateJarBalance(jar.getId(), addVal);
                    loadAllData();
                    Toast.makeText(MainActivity.this, "Đã nạp tiền vào hũ!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // Khởi tạo menu tùy chọn trên thanh tiêu đề
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Xử lý sự kiện khi người dùng chọn các mục lọc trên menu góc phải
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