package com.example.a22_dacn_nguyennhatlong_cr424k;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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
    // Khai báo các thành phần giao diện
    private RadioGroup rgType;
    private RadioButton rbExpense, rbIncome;
    private TextView tvJarLabel, tvCameraHint, tvCameraSubHint;
    private EditText edtTitle, edtAmount, edtDate;
    private Spinner spJar, spCategory;
    private Button btnSave, btnBack;
    private LinearLayout boxLocketCamera;
    private ImageView imgLocketPreview;

    // Khai báo đối tượng quản lý CSDL và các biến lưu dữ liệu
    private DatabaseHelper dbHelper;
    private List<MoneyJar> jarList;
    private Transaction editingTransaction = null;
    private String savedImagePath = "";

    // Mảng danh mục mẫu cho Khoản Chi và Khoản Thu
    private final String[] expenseCategories = {"Ăn uống", "Di chuyển", "Học tập", "Mua sắm", "Hóa đơn", "Khác"};
    private final String[] incomeCategories = {"Lương", "Thưởng", "Phụ cấp", "Đầu tư", "Khác"};

    // kết quả trả về sau khi mở Camera chụp ảnh hóa đơn
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            if (imageBitmap != null) {
                                // Hiển thị ảnh vừa chụp lên khung xem trước
                                imgLocketPreview.setPadding(0, 0, 0, 0);
                                imgLocketPreview.setImageBitmap(imageBitmap);
                                tvCameraHint.setText("Đã đính kèm ảnh hóa đơn ✓");
                                tvCameraHint.setTextColor(Color.parseColor("#2E7D32"));
                                tvCameraSubHint.setText("Chạm vào đây để xem phóng to hoặc chụp lại");

                                // Lưu file ảnh vào bộ nhớ trong của ứng dụng
                                savedImagePath = saveBitmapToInternalStorage(imageBitmap);
                            }
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Hiển thị nút mũi tên quay lại trên thanh tiêu đề
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Ánh xạ các thành phần giao diện từ file activity_add_transaction.xml
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

        // Khởi tạo đối tượng DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Bắt sự kiện nút Quay lại -> đóng màn hình hiện tại
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Lấy danh sách Hũ chi tiêu từ SQLite và đổ lên Spinner chọn hũ
        jarList = dbHelper.getAllJars();
        ArrayAdapter<MoneyJar> jarAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, jarList);
        spJar.setAdapter(jarAdapter);

        // Bắt sự kiện khi chạm vào khung ảnh hóa đơn:
        // - Nếu đã có ảnh hóa đơn cũ -> mở hộp thoại phóng to để xem lại (kèm nút chụp lại)
        // - Nếu chưa có ảnh -> mở Camera để chụp ảnh mới
        boxLocketCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasImage = false;
                if (!savedImagePath.isEmpty()) {
                    File imgFile = new File(savedImagePath);
                    if (imgFile.exists()) {
                        hasImage = true;
                    }
                }

                if (hasImage) {
                    showFullImageDialog(savedImagePath);
                } else {
                    openCamera();
                }
            }
        });

        // Thiết lập giao diện mặc định ban đầu là Khoản Chi
        updateUIByType(false);

        // Lắng nghe sự kiện khi người dùng chuyển đổi giữa Khoản Chi và Khoản Thu
        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbIncome) {
                    updateUIByType(true);
                } else {
                    updateUIByType(false);
                }
            }
        });

        // Kiểm tra xem Intent có gửi kèm đối tượng EDIT_TRANSACTION hay không
        Intent intent = getIntent();
        if (intent.hasExtra("EDIT_TRANSACTION")) {
            editingTransaction = (Transaction) intent.getSerializableExtra("EDIT_TRANSACTION");
            if (editingTransaction != null) {
                setTitle("Cập Nhật Giao Dịch");
                edtTitle.setText(editingTransaction.getTitle());
                edtAmount.setText(String.format(Locale.US, "%.0f", editingTransaction.getAmount()));
                edtDate.setText(editingTransaction.getDate());

                if (editingTransaction.getImagePath() != null) {
                    savedImagePath = editingTransaction.getImagePath();
                } else {
                    savedImagePath = "";
                }

                // Nếu giao dịch cũ có ảnh hóa đơn thì hiển thị lại lên khung xem trước
                if (!savedImagePath.isEmpty()) {
                    File imgFile = new File(savedImagePath);
                    if (imgFile.exists()) {
                        imgLocketPreview.setPadding(0, 0, 0, 0);
                        imgLocketPreview.setImageBitmap(BitmapFactory.decodeFile(savedImagePath));
                        tvCameraHint.setText("Đã đính kèm ảnh hóa đơn ✓");
                        tvCameraHint.setTextColor(Color.parseColor("#2E7D32"));
                        tvCameraSubHint.setText("Chạm vào đây để xem phóng to ảnh cũ");
                    }
                }

                // Cập nhật lại trạng thái RadioButton Thu/Chi và nút Lưu
                boolean isIncome = editingTransaction.getType().equals("THU");
                rbIncome.setChecked(isIncome);
                rbExpense.setChecked(!isIncome);
                updateUIByType(isIncome);
                btnSave.setText("CẬP NHẬT GIAO DỊCH");

                // Chọn lại đúng danh mục của giao dịch cũ trên Spinner
                String[] currentArr = isIncome ? incomeCategories : expenseCategories;
                for (int i = 0; i < currentArr.length; i++) {
                    if (currentArr[i].equals(editingTransaction.getCategory())) {
                        spCategory.setSelection(i);
                        break;
                    }
                }

                // Chọn lại đúng Hũ chi tiêu của giao dịch cũ trên Spinner
                for (int i = 0; i < jarList.size(); i++) {
                    if (jarList.get(i).getId() == editingTransaction.getJarId()) {
                        spJar.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            // Chế độ Thêm giao dịch mới: lấy ngày hiện tại của hệ thống làm mặc định
            setTitle("Thêm Giao Dịch Mới");
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            edtDate.setText(day + "/" + month + "/" + year);
        }

        // Bắt sự kiện chạm vào ô Ngày giao dịch -> mở hộp thoại chọn ngày
        edtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                DatePickerDialog datePicker = new DatePickerDialog(
                        AddTransactionActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                edtDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                            }
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                );
                datePicker.show();
            }
        });

        // Bắt sự kiện nút Lưu / Xác nhận giao dịch
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSave();
            }
        });
    }

    // Phương thức hiển thị hộp thoại phóng to ảnh hóa đơn đã lưu (kèm tùy chọn chụp lại)
    private void showFullImageDialog(String imagePath) {
        ImageView fullImageView = new ImageView(this);
        fullImageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        fullImageView.setAdjustViewBounds(true);
        fullImageView.setPadding(24, 24, 24, 12);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ảnh hóa đơn đã lưu");
        builder.setView(fullImageView);
        builder.setPositiveButton("Đóng", null);
        builder.setNeutralButton("Chụp lại ảnh mới", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openCamera();
            }
        });
        builder.show();
    }

    // Xử lý khi người dùng bấm nút mũi tên quay lại trên ActionBar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Cập nhật danh sách danh mục và màu sắc giao diện theo loại Thu hoặc Chi
    private void updateUIByType(boolean isIncome) {
        String[] data = isIncome ? incomeCategories : expenseCategories;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, data);
        spCategory.setAdapter(adapter);

        if (isIncome) {
            tvJarLabel.setText("Chọn Hũ để cộng thêm tiền:");
            tvJarLabel.setTextColor(Color.parseColor("#2E7D32"));
            btnSave.setBackgroundColor(Color.parseColor("#2E7D32"));
            if (editingTransaction == null) {
                btnSave.setText("XÁC NHẬN NẠP TIỀN HŨ");
            }
        } else {
            tvJarLabel.setText("Chọn Hũ để trừ tiền:");
            tvJarLabel.setTextColor(Color.parseColor("#C62828"));
            btnSave.setBackgroundColor(Color.parseColor("#C62828"));
            if (editingTransaction == null) {
                btnSave.setText("XÁC NHẬN TRỪ TIỀN HŨ");
            }
        }
    }

    // Kiểm tra quyền CAMERA và mở ứng dụng chụp ảnh của máy
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(takePictureIntent);
        }
    }

    // Nhận kết quả sau khi người dùng cấp quyền CAMERA
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "Cần cấp quyền Camera để chụp ảnh!", Toast.LENGTH_SHORT).show();
        }
    }

    // Lưu ảnh Bitmap vào thư mục nội bộ của ứng dụng và trả về đường dẫn file
    private String saveBitmapToInternalStorage(Bitmap bitmap) {
        try {
            String fileName = "receipt_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Kiểm tra tính hợp lệ của dữ liệu nhập vào trước khi lưu xuống CSDL
    private void validateAndSave() {
        String title = edtTitle.getText().toString().trim();
        String amountStr = edtAmount.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        boolean isIncome = rbIncome.isChecked();
        String type = isIncome ? "THU" : "CHI";
        MoneyJar selectedJar = (MoneyJar) spJar.getSelectedItem();

        // Kiểm tra rỗng
        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền và nội dung!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra định dạng số tiền hợp lệ
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

        // Nếu là Khoản Chi và số tiền lớn hơn số dư hiện tại của hũ -> hiện cảnh báo xác nhận
        if (!isIncome && editingTransaction == null && amount > selectedJar.getCurrentAmount()) {
            DecimalFormat df = new DecimalFormat("#,###");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Số dư hũ không đủ");
            builder.setMessage("Hũ '" + selectedJar.getName() + "' hiện chỉ còn " + df.format(selectedJar.getCurrentAmount()) + " đ.\nBạn vẫn muốn trừ tiền (hũ sẽ bị âm) chứ?");
            builder.setPositiveButton("Vẫn trừ tiền", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    executeSave(title, amount, type, category, date, selectedJar);
                }
            });
            builder.setNegativeButton("Chọn hũ khác", null);
            builder.show();
            return;
        }

        // Thực hiện lưu giao dịch
        executeSave(title, amount, type, category, date, selectedJar);
    }

    // Thực hiện thêm mới hoặc cập nhật giao dịch vào SQLite
    private void executeSave(String title, double amount, String type, String category, String date, MoneyJar selectedJar) {
        DecimalFormat df = new DecimalFormat("#,###");
        if (editingTransaction == null) {
            // Thêm giao dịch mới
            Transaction newTrans = new Transaction(
                    0, title, amount, type, category, date,
                    savedImagePath, selectedJar.getId(), selectedJar.getName()
            );
            dbHelper.addTransaction(newTrans);

            String actionMsg;
            if (type.equals("CHI")) {
                actionMsg = "Đã trừ " + df.format(amount) + " đ từ hũ " + selectedJar.getName() + "!";
            } else {
                actionMsg = "Đã cộng " + df.format(amount) + " đ vào hũ " + selectedJar.getName() + "!";
            }
            Toast.makeText(this, actionMsg, Toast.LENGTH_LONG).show();
        } else {
            // Cập nhật giao dịch đang sửa
            Transaction updatedTrans = new Transaction(
                    editingTransaction.getId(), title, amount, type, category, date,
                    savedImagePath, selectedJar.getId(), selectedJar.getName()
            );
            dbHelper.updateTransaction(editingTransaction, updatedTrans);
            Toast.makeText(this, "Đã cập nhật giao dịch!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}