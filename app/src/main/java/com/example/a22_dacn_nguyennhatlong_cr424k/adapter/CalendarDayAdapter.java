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

// Adapter đổ dữ liệu từng ngày lên lưới lịch
public class CalendarDayAdapter extends BaseAdapter {
    private Context context;
    private List<CalendarDayItem> dayList;
    private String selectedDate;

    // Hàm khởi tạo
    public CalendarDayAdapter(Context context, List<CalendarDayItem> dayList, String selectedDate) {
        this.context = context;
        this.dayList = dayList;
        this.selectedDate = selectedDate;
    }

    // Cập nhật ngày đang chọn và vẽ lại giao diện
    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dayList.size();
    }

    @Override
    public Object getItem(int position) {
        return dayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Tạo và gán dữ liệu cho từng ô ngày trên lịch
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_calendar_day, parent, false);
        }

        // Ánh xạ các thành phần giao diện từ item_calendar_day.xml
        CardView cardBox = convertView.findViewById(R.id.cardDayBox);
        ImageView imgPhoto = convertView.findViewById(R.id.imgDayPhoto);
        TextView tvBadge = convertView.findViewById(R.id.tvDayBadge);
        TextView tvDayNumber = convertView.findViewById(R.id.tvDayNumber);

        // Lấy đối tượng ngày tại vị trí position
        CalendarDayItem item = dayList.get(position);

        // Nếu là ô trống đầu tháng (dayNumber == 0) thì ẩn đi
        if (item.getDayNumber() == 0) {
            cardBox.setVisibility(View.INVISIBLE);
            tvBadge.setVisibility(View.GONE);
            tvDayNumber.setVisibility(View.INVISIBLE);
            return convertView;
        }

        // Hiển thị số ngày
        cardBox.setVisibility(View.VISIBLE);
        tvDayNumber.setVisibility(View.VISIBLE);
        tvDayNumber.setText(String.valueOf(item.getDayNumber()));

        // Đổi màu và in đậm nếu đây là ngày đang được người dùng chọn
        if (item.getFullDate().equals(selectedDate)) {
            tvDayNumber.setTextColor(Color.parseColor("#1565C0"));
            tvDayNumber.setTypeface(null, Typeface.BOLD);
        } else {
            tvDayNumber.setTextColor(Color.parseColor("#424242"));
            tvDayNumber.setTypeface(null, Typeface.NORMAL);
        }

        // Lấy danh sách giao dịch của ngày này
        List<Transaction> transList = item.getTransactions();

        if (transList == null || transList.isEmpty()) {
            // Trường hợp 1: Ngày chưa có giao dịch nào
            cardBox.setCardBackgroundColor(Color.parseColor("#E2E8F0"));
            imgPhoto.setImageDrawable(null);
            tvBadge.setVisibility(View.GONE);
        } else {
            // Trường hợp 2: Ngày đã có giao dịch -> Duyệt tìm xem có ảnh hóa đơn không
            String foundPhotoPath = "";
            for (int i = 0; i < transList.size(); i++) {
                String path = transList.get(i).getImagePath();
                if (path != null && !path.isEmpty()) {
                    File imgFile = new File(path);
                    if (imgFile.exists()) {
                        foundPhotoPath = path;
                        break;
                    }
                }
            }

            if (!foundPhotoPath.isEmpty()) {
                // Nếu có ảnh hóa đơn thì hiển thị ảnh lên ô ngày
                imgPhoto.setPadding(0, 0, 0, 0);
                imgPhoto.setColorFilter(null);
                imgPhoto.setImageBitmap(BitmapFactory.decodeFile(foundPhotoPath));
                cardBox.setCardBackgroundColor(Color.WHITE);
            } else {
                // Nếu không có ảnh thì hiển thị icon theo danh mục giao dịch đầu tiên
                Transaction firstTrans = transList.get(0);
                imgPhoto.setPadding(18, 18, 18, 18);
                imgPhoto.setColorFilter(Color.WHITE);
                setCategoryIconAndColor(firstTrans, cardBox, imgPhoto);
            }

            // Nếu ngày đó có từ 2 giao dịch trở lên thì hiện badge đếm số lượng (+1, +2...)
            if (transList.size() > 1) {
                tvBadge.setVisibility(View.VISIBLE);
                int extraCount = transList.size() - 1;
                tvBadge.setText("+" + extraCount);
            } else {
                tvBadge.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    // Hàm gán màu nền và icon tương ứng với loại Thu/Chi và Danh mục
    private void setCategoryIconAndColor(Transaction t, CardView cardBox, ImageView imgPhoto) {
        if (t.getType().equals("THU")) {
            cardBox.setCardBackgroundColor(Color.parseColor("#2E7D32"));
            imgPhoto.setImageResource(android.R.drawable.ic_input_add);
            return;
        }

        String category = t.getCategory();
        if (category == null) {
            category = "";
        }

        if (category.equals("Ăn uống")) {
            cardBox.setCardBackgroundColor(Color.parseColor("#EF6C00"));
            imgPhoto.setImageResource(android.R.drawable.ic_menu_compass);
        } else if (category.equals("Di chuyển")) {
            cardBox.setCardBackgroundColor(Color.parseColor("#1976D2"));
            imgPhoto.setImageResource(android.R.drawable.ic_menu_directions);
        } else if (category.equals("Mua sắm")) {
            cardBox.setCardBackgroundColor(Color.parseColor("#C2185B"));
            imgPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        } else if (category.equals("Học tập")) {
            cardBox.setCardBackgroundColor(Color.parseColor("#512DA8"));
            imgPhoto.setImageResource(android.R.drawable.ic_menu_edit);
        } else {
            cardBox.setCardBackgroundColor(Color.parseColor("#C62828"));
            imgPhoto.setImageResource(android.R.drawable.ic_menu_agenda);
        }
    }
}