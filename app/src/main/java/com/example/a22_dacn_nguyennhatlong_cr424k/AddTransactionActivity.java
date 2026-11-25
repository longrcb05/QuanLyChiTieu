package com.example.a22_dacn_nguyennhatlong_cr424k;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.a22_dacn_nguyennhatlong_cr424k.database.DatabaseHelper;
import com.example.a22_dacn_nguyennhatlong_cr424k.model.MoneyJar;
import com.example.a22_dacn_nguyennhatlong_cr424k.model.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {
    private RadioGroup rgType;
    private RadioButton rbExpense, rbIncome;
    private TextView tvJarLabel, tvCameraHint, tvCameraSubHint;
    private EditText edtTitle, edtAmount, edtDate;
    private Spinner spJar, spCategory;
    private Button btnSave, btnBack;
    private LinearLayout boxLocketCamera;
    private ImageView imgLocketPreview;

    private DatabaseHelper dbHelper;
    private List<MoneyJar> jarList;
    private Transaction editingTransaction = null;
    private String savedImagePath = "";

    private final String[] expenseCategories = {"Ăn uống", "Di chuyển", "Học tập", "Mua sắm", "Hóa đơn", "Khác"};
    private final String[] incomeCategories = {"Lương", "Thưởng", "Phụ cấp", "Đầu tư", "Khác"};

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        imgLocketPreview.setPadding(0, 0, 0, 0);
                        imgLocketPreview.setImageBitmap(imageBitmap);
                        tvCameraHint.setText("Đã đính kèm ảnh hóa đơn ✓");
                        tvCameraHint.setTextColor(Color.parseColor("#2E7D32"));
                        tvCameraSubHint.setText("Chạm vào đây để xem phóng to hoặc chụp lại");
                        savedImagePath = saveBitmapToInternalStorage(imageBitmap);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rgType = findViewById(R.id.rgType);
        rbExpense = findViewById(R.id.rbExpense);
        rbIncome = findViewById(R.id.rbIncome);
        tvJarLabel = findViewById(R.id.tvJarLabel);
        tvCameraHint = findViewById(R.id.tvCameraHint);
        tvCameraSubHint = findViewById(R.id.tvCameraSubHint);
        edtTitle = findViewById(R.id.edtTitle);
        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        spJar = findViewById(R.id.spJar);
        spCategory = findViewById(R.id.spCategory);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        boxLocketCamera = findViewById(R.id.boxLocketCamera);
        imgLocketPreview = findViewById(R.id.imgLocketPreview);

        dbHelper = new DatabaseHelper(this);

        btnBack.setOnClickListener(v -> finish());

        jarList = dbHelper.getAllJars();
        ArrayAdapter<MoneyJar> jarAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, jarList);
        spJar.setAdapter(jarAdapter);

        // Nếu đã có ảnh hóa đơn -> mở hộp thoại xem ảnh cũ (kèm nút chụp lại)
        // Nếu chưa có ảnh -> mở camera chụp mới
        boxLocketCamera.setOnClickListener(v -> {
            if (!savedImagePath.isEmpty() && new File(savedImagePath).exists()) {
                showFullImageDialog(savedImagePath);
            } else {
                openCamera();
            }
        });

        updateUIByType(false);
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            updateUIByType(checkedId == R.id.rbIncome);
        });

        if (getIntent().hasExtra("EDIT_TRANSACTION")) {
            editingTransaction = (Transaction) getIntent().getSerializableExtra("EDIT_TRANSACTION");
            if (editingTransaction != null) {
                setTitle("Cập Nhật Giao Dịch");
                edtTitle.setText(editingTransaction.getTitle());
                edtAmount.setText(String.format(Locale.US, "%.0f", editingTransaction.getAmount()));
                edtDate.setText(editingTransaction.getDate());
                savedImagePath = editingTransaction.getImagePath() != null ? editingTransaction.getImagePath() : "";

                if (!savedImagePath.isEmpty() && new File(savedImagePath).exists()) {
                    imgLocketPreview.setPadding(0, 0, 0, 0);
                    imgLocketPreview.setImageBitmap(BitmapFactory.decodeFile(savedImagePath));
                    tvCameraHint.setText("Đã đính kèm ảnh hóa đơn ✓");
                    tvCameraHint.setTextColor(Color.parseColor("#2E7D32"));
                    tvCameraSubHint.setText("Chạm vào đây để xem phóng to ảnh cũ");
                }

                boolean isIncome = editingTransaction.getType().equals("THU");
                rbIncome.setChecked(isIncome);
                rbExpense.setChecked(!isIncome);
                updateUIByType(isIncome);
                btnSave.setText("CẬP NHẬT GIAO DỊCH");

                String[] currentArr = isIncome ? incomeCategories : expenseCategories;
                for (int i = 0; i < currentArr.length; i++) {
                    if (currentArr[i].equals(editingTransaction.getCategory())) {
                        spCategory.setSelection(i);
                        break;
                    }
                }

                for (int i = 0; i < jarList.size(); i++) {
                    if (jarList.get(i).getId() == editingTransaction.getJarId()) {
                        spJar.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            setTitle("Thêm Giao Dịch Mới");
            Calendar cal = Calendar.getInstance();
            edtDate.setText(cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
        }

        edtDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
                    edtDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    // Hộp thoại phóng to xem lại ảnh hóa đơn cũ + tùy chọn chụp lại
    private void showFullImageDialog(String imagePath) {
        ImageView fullImageView = new ImageView(this);
        fullImageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        fullImageView.setAdjustViewBounds(true);
        fullImageView.setPadding(24, 24, 24, 12);

        new AlertDialog.Builder(this)
                .setTitle("Ảnh hóa đơn đã lưu")
                .setView(fullImageView)
                .setPositiveButton("Đóng", null)
                .setNeutralButton("Chụp lại ảnh mới", (d, w) -> openCamera())
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void updateUIByType(boolean isIncome) {
        String[] data = isIncome ? incomeCategories : expenseCategories;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, data);
        spCategory.setAdapter(adapter);

        if (isIncome) {
            tvJarLabel.setText("Chọn Hũ để cộng thêm tiền:");
            tvJarLabel.setTextColor(Color.parseColor("#2E7D32"));
            btnSave.setBackgroundColor(Color.parseColor("#2E7D32"));
            if (editingTransaction == null) btnSave.setText("XÁC NHẬN NẠP TIỀN HŨ");
        } else {
            tvJarLabel.setText("Chọn Hũ để trừ tiền:");
            tvJarLabel.setTextColor(Color.parseColor("#C62828"));
            btnSave.setBackgroundColor(Color.parseColor("#C62828"));
            if (editingTransaction == null) btnSave.setText("XÁC NHẬN TRỪ TIỀN HŨ");
        }
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
        } else {
            Toast.makeText(this, "Cần cấp quyền Camera để chụp ảnh!", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveBitmapToInternalStorage(Bitmap bitmap) {
        try {
            File file = new File(getFilesDir(), "receipt_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
    }

    private void validateAndSave() {
        String title = edtTitle.getText().toString().trim();
        String amountStr = edtAmount.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        boolean isIncome = rbIncome.isChecked();
        String type = isIncome ? "THU" : "CHI";
        MoneyJar selectedJar = (MoneyJar) spJar.getSelectedItem();

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền và nội dung!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0 || amount > 999999999999.0) {
                Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isIncome && editingTransaction == null && amount > selectedJar.getCurrentAmount()) {
            DecimalFormat df = new DecimalFormat("#,###");
            new AlertDialog.Builder(this)
                    .setTitle("Số dư hũ không đủ")
                    .setMessage("Hũ '" + selectedJar.getName() + "' hiện chỉ còn " + df.format(selectedJar.getCurrentAmount()) + " đ.\nBạn vẫn muốn trừ tiền (hũ sẽ bị âm) chứ?")
                    .setPositiveButton("Vẫn trừ tiền", (d, w) -> executeSave(title, amount, type, category, date, selectedJar))
                    .setNegativeButton("Chọn hũ khác", null)
                    .show();
            return;
        }

        executeSave(title, amount, type, category, date, selectedJar);
    }

    private void executeSave(String title, double amount, String type, String category, String date, MoneyJar selectedJar) {
        DecimalFormat df = new DecimalFormat("#,###");
        if (editingTransaction == null) {
            dbHelper.addTransaction(new Transaction(0, title, amount, type, category, date, savedImagePath, selectedJar.getId(), selectedJar.getName()));
            String actionMsg = type.equals("CHI")
                    ? "Đã trừ " + df.format(amount) + " đ từ hũ " + selectedJar.getName() + "!"
                    : "Đã cộng " + df.format(amount) + " đ vào hũ " + selectedJar.getName() + "!";
            Toast.makeText(this, actionMsg, Toast.LENGTH_LONG).show();
        } else {
            Transaction updated = new Transaction(editingTransaction.getId(), title, amount, type, category, date, savedImagePath, selectedJar.getId(), selectedJar.getName());
            dbHelper.updateTransaction(editingTransaction, updated);
            Toast.makeText(this, "Đã cập nhật giao dịch!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}