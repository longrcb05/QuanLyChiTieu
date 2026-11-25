package com.example.a22_dacn_nguyennhatlong_cr424k.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.a22_dacn_nguyennhatlong_cr424k.R;
import com.example.a22_dacn_nguyennhatlong_cr424k.model.MoneyJar;

import java.text.DecimalFormat;
import java.util.List;

// Adapter đổ danh sách các Hũ chi tiêu lên ListView
public class JarAdapter extends BaseAdapter {

    // Interface dùng để truyền sự kiện xóa hũ về MainActivity xử lý
    public interface OnJarDeleteListener {
        void onDeleteJar(MoneyJar jar);
    }

    private Context context;
    private List<MoneyJar> list;
    private OnJarDeleteListener deleteListener;

    // Hàm khởi tạo
    public JarAdapter(Context context, List<MoneyJar> list, OnJarDeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.deleteListener = deleteListener;
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

    // Tạo và gán dữ liệu cho từng dòng hũ chi tiêu
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_jar, parent, false);
        }

        // Ánh xạ các thành phần giao diện từ item_jar.xml
        TextView tvName = convertView.findViewById(R.id.tvJarName);
        TextView tvPercent = convertView.findViewById(R.id.tvJarPercent);
        TextView tvBalance = convertView.findViewById(R.id.tvJarBalance);
        ProgressBar pbProgress = convertView.findViewById(R.id.pbJarProgress);
        ImageButton btnDelete = convertView.findViewById(R.id.btnDeleteJar);

        // Lấy đối tượng Hũ chi tiêu tại vị trí position
        MoneyJar jar = list.get(position);

        // Định dạng số tiền có dấu phẩy phân cách hàng nghìn
        DecimalFormat df = new DecimalFormat("#,###");
        tvName.setText("📂 " + jar.getName());

        // Tính phần trăm số dư hiện tại so với mục tiêu (giới hạn từ 0% đến 100%)
        double current = jar.getCurrentAmount();
        double target = jar.getTargetAmount();
        int percent = 0;
        if (target > 0) {
            double rawPercent = (current / target) * 100;
            if (rawPercent < 0) {
                percent = 0;
            } else if (rawPercent > 100) {
                percent = 100;
            } else {
                percent = (int) rawPercent;
            }
        }

        // Hiển thị thông tin số dư, mục tiêu và thanh tiến trình
        tvBalance.setText("Số dư hũ: " + df.format(current) + " đ  |  Mục tiêu: " + df.format(target) + " đ");
        tvPercent.setText(percent + "%");
        pbProgress.setProgress(percent);

        // Nếu số dư hũ bị âm thì đổi chữ sang màu đỏ để cảnh báo
        if (current < 0) {
            tvBalance.setTextColor(Color.parseColor("#C62828"));
        } else {
            tvBalance.setTextColor(Color.parseColor("#424242"));
        }

        // Ẩn nút xóa đối với "Ví chính" mặc định (id = 1) để tránh xóa nhầm
        if (jar.getId() == 1) {
            btnDelete.setVisibility(View.INVISIBLE);
        } else {
            btnDelete.setVisibility(View.VISIBLE);

            // Bắt sự kiện khi nhấn nút xóa hũ
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Xác nhận xóa hũ");
                    builder.setMessage("Bạn có chắc muốn xóa hũ '" + jar.getName() + "' không?");

                    builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (deleteListener != null) {
                                deleteListener.onDeleteJar(jar);
                            }
                        }
                    });

                    builder.setNegativeButton("Hủy", null);
                    builder.show();
                }
            });
        }

        return convertView;
    }
}