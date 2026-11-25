package com.example.a22_dacn_nguyennhatlong_cr424k.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.a22_dacn_nguyennhatlong_cr424k.R;
import com.example.a22_dacn_nguyennhatlong_cr424k.model.Transaction;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

// Adapter đổ danh sách các giao dịch Thu/Chi lên ListView
public class TransactionAdapter extends BaseAdapter {
    private Context context;
    private List<Transaction> list;

    // Hàm khởi tạo
    public TransactionAdapter(Context context, List<Transaction> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getId();
    }

    // Tạo và gán dữ liệu cho từng dòng giao dịch
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_transaction, parent, false);
        }

        // Ánh xạ các thành phần giao diện từ item_transaction.xml
        TextView tvTitle = convertView.findViewById(R.id.tvItemTitle);
        TextView tvSub = convertView.findViewById(R.id.tvItemSub);
        TextView tvAmount = convertView.findViewById(R.id.tvItemAmount);
        ImageView imgReceipt = convertView.findViewById(R.id.imgItemLocket);

        // Lấy đối tượng giao dịch tại vị trí position
        Transaction t = list.get(position);

        // Gán tiêu đề và thông tin phụ (Danh mục, Tên hũ, Ngày tháng)
        tvTitle.setText(t.getTitle());
        String jarLabel = "Ví chính";
        if (t.getJarName() != null && !t.getJarName().isEmpty()) {
            jarLabel = t.getJarName();
        }
        tvSub.setText(t.getCategory() + " • [" + jarLabel + "] • " + t.getDate());

        // Định dạng số tiền và đổi màu chữ theo loại giao dịch (Thu: Xanh, Chi: Đỏ)
        DecimalFormat df = new DecimalFormat("#,###");
        if (t.getType().equals("THU")) {
            tvAmount.setText("+" + df.format(t.getAmount()) + " đ");
            tvAmount.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            tvAmount.setText("-" + df.format(t.getAmount()) + " đ");
            tvAmount.setTextColor(Color.parseColor("#C62828"));
        }

        // Kiểm tra đường dẫn ảnh hóa đơn để hiển thị ảnh thu nhỏ hoặc icon mặc định
        String imagePath = t.getImagePath();
        boolean hasValidImage = false;
        if (imagePath != null && !imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                hasValidImage = true;
            }
        }

        if (hasValidImage) {
            // Hiển thị ảnh hóa đơn đã chụp
            imgReceipt.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            imgReceipt.setPadding(0, 0, 0, 0);

            // Bắt sự kiện khi chạm vào ảnh thu nhỏ -> mở hộp thoại xem phóng to ảnh hóa đơn
            imgReceipt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView fullImg = new ImageView(context);
                    fullImg.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                    fullImg.setAdjustViewBounds(true);
                    fullImg.setPadding(24, 24, 24, 12);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Hóa đơn: " + t.getTitle());
                    builder.setView(fullImg);
                    builder.setPositiveButton("Đóng", null);
                    builder.show();
                }
            });
        } else {
            // Không có ảnh thì hiển thị icon camera mặc định
            imgReceipt.setImageResource(android.R.drawable.ic_menu_camera);
            imgReceipt.setPadding(20, 20, 20, 20);
            imgReceipt.setOnClickListener(null);
        }

        return convertView;
    }
}