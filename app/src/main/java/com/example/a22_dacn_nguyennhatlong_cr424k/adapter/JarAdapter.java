package com.example.a22_dacn_nguyennhatlong_cr424k.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.a22_dacn_nguyennhatlong_cr424k.R;
import com.example.a22_dacn_nguyennhatlong_cr424k.model.MoneyJar;

import java.text.DecimalFormat;
import java.util.List;

public class JarAdapter extends BaseAdapter {
    public interface OnJarDeleteListener {
        void onDeleteJar(MoneyJar jar);
    }

    private Context context;
    private List<MoneyJar> list;
    private OnJarDeleteListener deleteListener;

    public JarAdapter(Context context, List<MoneyJar> list, OnJarDeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.deleteListener = deleteListener;
    }

    @Override
    public int getCount() { return list.size(); }

    @Override
    public Object getItem(int position) { return list.get(position); }

    @Override
    public long getItemId(int position) { return list.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_jar, parent, false);
        }

        MoneyJar jar = list.get(position);
        TextView tvName = convertView.findViewById(R.id.tvJarName);
        TextView tvPercent = convertView.findViewById(R.id.tvJarPercent);
        TextView tvBalance = convertView.findViewById(R.id.tvJarBalance);
        ProgressBar pbProgress = convertView.findViewById(R.id.pbJarProgress);
        ImageButton btnDelete = convertView.findViewById(R.id.btnDeleteJar);

        DecimalFormat df = new DecimalFormat("#,###");
        tvName.setText("📂 " + jar.getName());

        double current = jar.getCurrentAmount();
        double target = jar.getTargetAmount();
        int percent = (target > 0) ? (int) Math.max(0, Math.min((current / target) * 100, 100)) : 0;

        tvBalance.setText("Số dư hũ: " + df.format(current) + " đ  |  Mục tiêu: " + df.format(target) + " đ");
        tvPercent.setText(percent + "%");
        pbProgress.setProgress(percent);

        if (current < 0) {
            tvBalance.setTextColor(Color.parseColor("#C62828"));
        } else {
            tvBalance.setTextColor(Color.parseColor("#424242"));
        }

        // Ẩn nút xóa đối với Ví chính (id = 1)
        if (jar.getId() == 1) {
            btnDelete.setVisibility(View.INVISIBLE);
        } else {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa hũ")
                        .setMessage("Bạn có chắc muốn xóa hũ '" + jar.getName() + "' không?")
                        .setPositiveButton("Xóa", (d, w) -> {
                            if (deleteListener != null) {
                                deleteListener.onDeleteJar(jar);
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        return convertView;
    }
}