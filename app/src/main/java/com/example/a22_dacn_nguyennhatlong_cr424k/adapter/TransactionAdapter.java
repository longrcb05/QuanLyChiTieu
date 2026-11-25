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

public class TransactionAdapter extends BaseAdapter {
    private Context context;
    private List<Transaction> list;

    public TransactionAdapter(Context context, List<Transaction> list) {
        this.context = context;
        this.list = list;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        }

        Transaction t = list.get(position);
        TextView tvTitle = convertView.findViewById(R.id.tvItemTitle);
        TextView tvSub = convertView.findViewById(R.id.tvItemSub);
        TextView tvAmount = convertView.findViewById(R.id.tvItemAmount);
        ImageView imgLocket = convertView.findViewById(R.id.imgItemLocket);

        DecimalFormat df = new DecimalFormat("#,###");
        tvTitle.setText(t.getTitle());
        String jarLabel = (t.getJarName() != null && !t.getJarName().isEmpty()) ? t.getJarName() : "Ví chính";
        tvSub.setText(t.getCategory() + " • [" + jarLabel + "] • " + t.getDate());

        if (t.getType().equals("THU")) {
            tvAmount.setText("+" + df.format(t.getAmount()) + " đ");
            tvAmount.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            tvAmount.setText("-" + df.format(t.getAmount()) + " đ");
            tvAmount.setTextColor(Color.parseColor("#C62828"));
        }

        if (t.getImagePath() != null && !t.getImagePath().isEmpty() && new File(t.getImagePath()).exists()) {
            imgLocket.setImageBitmap(BitmapFactory.decodeFile(t.getImagePath()));
            imgLocket.setPadding(0, 0, 0, 0);
            imgLocket.setOnClickListener(v -> {
                ImageView fullImg = new ImageView(context);
                fullImg.setImageBitmap(BitmapFactory.decodeFile(t.getImagePath()));
                fullImg.setAdjustViewBounds(true);
                fullImg.setPadding(24, 24, 24, 12);

                new AlertDialog.Builder(context)
                        .setTitle("Hóa đơn: " + t.getTitle())
                        .setView(fullImg)
                        .setPositiveButton("Đóng", null)
                        .show();
            });
        } else {
            imgLocket.setImageResource(android.R.drawable.ic_menu_camera);
            imgLocket.setPadding(20, 20, 20, 20);
            imgLocket.setOnClickListener(null);
        }

        return convertView;
    }
}