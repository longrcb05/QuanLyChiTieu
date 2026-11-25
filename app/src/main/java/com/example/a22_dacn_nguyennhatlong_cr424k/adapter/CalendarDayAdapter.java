package com.example.a22_dacn_nguyennhatlong_cr424k.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.a22_dacn_nguyennhatlong_cr424k.R;
import com.example.a22_dacn_nguyennhatlong_cr424k.model.CalendarDayItem;
import com.example.a22_dacn_nguyennhatlong_cr424k.model.Transaction;

import java.io.File;
import java.util.List;

public class CalendarDayAdapter extends BaseAdapter {
    private Context context;
    private List<CalendarDayItem> dayList;
    private String selectedDate;

    public CalendarDayAdapter(Context context, List<CalendarDayItem> dayList, String selectedDate) {
        this.context = context;
        this.dayList = dayList;
        this.selectedDate = selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return dayList.size(); }

    @Override
    public Object getItem(int position) { return dayList.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        }

        CalendarDayItem item = dayList.get(position);
        CardView cardBox = convertView.findViewById(R.id.cardDayBox);
        ImageView imgPhoto = convertView.findViewById(R.id.imgDayPhoto);
        TextView tvBadge = convertView.findViewById(R.id.tvDayBadge);
        TextView tvDayNumber = convertView.findViewById(R.id.tvDayNumber);

        if (item.getDayNumber() == 0) {
            cardBox.setVisibility(View.INVISIBLE);
            tvBadge.setVisibility(View.GONE);
            tvDayNumber.setVisibility(View.INVISIBLE);
            return convertView;
        }

        cardBox.setVisibility(View.VISIBLE);
        tvDayNumber.setVisibility(View.VISIBLE);
        tvDayNumber.setText(String.valueOf(item.getDayNumber()));

        if (item.getFullDate().equals(selectedDate)) {
            tvDayNumber.setTextColor(Color.parseColor("#1565C0"));
            tvDayNumber.setTypeface(null, Typeface.BOLD);
        } else {
            tvDayNumber.setTextColor(Color.parseColor("#424242"));
            tvDayNumber.setTypeface(null, Typeface.NORMAL);
        }

        List<Transaction> transList = item.getTransactions();
        if (transList == null || transList.isEmpty()) {
            cardBox.setCardBackgroundColor(Color.parseColor("#E2E8F0"));
            imgPhoto.setImageDrawable(null);
            tvBadge.setVisibility(View.GONE);
        } else {
            // Ưu tiên tìm ảnh chụp Locket trong ngày đó
            String foundPhotoPath = "";
            for (int i = 0; i < transList.size(); i++) {
                String path = transList.get(i).getImagePath();
                if (path != null && !path.isEmpty() && new File(path).exists()) {
                    foundPhotoPath = path;
                    break;
                }
            }

            if (!foundPhotoPath.isEmpty()) {
                imgPhoto.setPadding(0, 0, 0, 0);
                imgPhoto.setColorFilter(null);
                imgPhoto.setImageBitmap(BitmapFactory.decodeFile(foundPhotoPath));
                cardBox.setCardBackgroundColor(Color.WHITE);
            } else {
                // Không có ảnh -> hiển thị icon & màu sắc theo danh mục của giao dịch
                Transaction first = transList.get(0);
                imgPhoto.setPadding(18, 18, 18, 18);
                imgPhoto.setColorFilter(Color.WHITE);
                applyCategoryStyle(first, cardBox, imgPhoto);
            }

            if (transList.size() > 1) {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText("+" + (transList.size() - 1));
            } else {
                tvBadge.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private void applyCategoryStyle(Transaction t, CardView cardBox, ImageView imgPhoto) {
        if (t.getType().equals("THU")) {
            cardBox.setCardBackgroundColor(Color.parseColor("#2E7D32")); // Xanh lá
            imgPhoto.setImageResource(android.R.drawable.ic_input_add);
            return;
        }
        String cat = t.getCategory() != null ? t.getCategory() : "";
        switch (cat) {
            case "Ăn uống":
                cardBox.setCardBackgroundColor(Color.parseColor("#EF6C00")); // Cam
                imgPhoto.setImageResource(android.R.drawable.ic_menu_compass);
                break;
            case "Di chuyển":
                cardBox.setCardBackgroundColor(Color.parseColor("#1976D2")); // Xanh dương
                imgPhoto.setImageResource(android.R.drawable.ic_menu_directions);
                break;
            case "Mua sắm":
                cardBox.setCardBackgroundColor(Color.parseColor("#C2185B")); // Hồng đậm
                imgPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
                break;
            case "Học tập":
                cardBox.setCardBackgroundColor(Color.parseColor("#512DA8")); // Tím
                imgPhoto.setImageResource(android.R.drawable.ic_menu_edit);
                break;
            default:
                cardBox.setCardBackgroundColor(Color.parseColor("#C62828")); // Đỏ
                imgPhoto.setImageResource(android.R.drawable.ic_menu_agenda);
                break;
        }
    }
}