package com.example.a22_dacn_nguyennhatlong_cr424k.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PieChartView extends View {
    // Khai báo các đối tượng bút vẽ và khung hình chữ nhật bao quanh hình tròn
    private Paint slicePaint;
    private Paint holePaint;
    private Paint textTitlePaint;
    private Paint textAmountPaint;
    private RectF rectF;

    // Danh sách lưu góc quétvà mã màu tương ứng của từng danh mục
    private List<Float> sweepAngles = new ArrayList<>();
    private List<Integer> sliceColors = new ArrayList<>();
    private double totalExpense = 0;

    // Mảng hằng số chứa bảng màu phân biệt cho từng danh mục chi tiêu
    public static final int[] CHART_COLORS = {
            Color.parseColor("#1565C0"), // Xanh dương đậm
            Color.parseColor("#EF6C00"), // Cam
            Color.parseColor("#2E7D32"), // Xanh lá
            Color.parseColor("#C2185B"), // Hồng đậm
            Color.parseColor("#6A1B9A"), // Tím
            Color.parseColor("#00838F")  // Xanh ngọc
    };

    // Hàm khởi tạo 1 tham số
    public PieChartView(Context context) {
        super(context);
        init();
    }

    // Hàm khởi tạo 2 tham số
    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Phương thức khởi tạo cấu hình mặc định cho các bút vẽ
    private void init() {
        // Bút vẽ các miếng cung tròn của biểu đồ
        slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slicePaint.setStyle(Paint.Style.FILL);

        // Bút vẽ hình tròn màu trắng ở giữa để tạo lỗ rỗng
        holePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        holePaint.setColor(Color.WHITE);
        holePaint.setStyle(Paint.Style.FILL);

        // Bút vẽ dòng chữ tiêu đề "Tổng chi" ở tâm biểu đồ
        textTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textTitlePaint.setColor(Color.parseColor("#757575"));
        textTitlePaint.setTextAlign(Paint.Align.CENTER);
        textTitlePaint.setTextSize(32f);

        // Bút vẽ con số tổng tiền chi tiêu ở tâm biểu đồ
        textAmountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textAmountPaint.setColor(Color.parseColor("#212121"));
        textAmountPaint.setTextAlign(Paint.Align.CENTER);
        textAmountPaint.setTextSize(40f);
        textAmountPaint.setFakeBoldText(true);

        rectF = new RectF();
    }

    // Phương thức nhận dữ liệu thống kê từ MainActivity, tính toán góc quét và vẽ lại biểu đồ
    public void setData(Map<String, Double> stats, double total) {
        sweepAngles.clear();
        sliceColors.clear();
        this.totalExpense = total;

        if (stats != null && total > 0) {
            int colorIndex = 0;
            for (Map.Entry<String, Double> entry : stats.entrySet()) {
                double categoryAmount = entry.getValue();
                // Công thức tính góc quét: (Số tiền danh mục / Tổng chi) * 360 độ
                float angle = (float) ((categoryAmount / total) * 360.0);
                sweepAngles.add(angle);

                int color = CHART_COLORS[colorIndex % CHART_COLORS.length];
                sliceColors.add(color);
                colorIndex++;
            }
        }

        // Gọi invalidate() để hệ thống tự động gọi lại hàm onDraw() vẽ biểu đồ mới
        invalidate();
    }

    // Phương thức thực hiện vẽ toàn bộ biểu đồ lên màn hình
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Tính đường kính hình tròn sao cho vừa vặn khung hình và chừa viền 30px
        int size = Math.min(width, height) - 30;
        if (size <= 0) {
            return;
        }

        // Căn giữa khung hình chữ nhật bao quanh hình tròn
        float left = (width - size) / 2f;
        float top = (height - size) / 2f;
        rectF.set(left, top, left + size, top + size);

        if (sweepAngles.isEmpty() || totalExpense <= 0) {
            // Nếu chưa có dữ liệu chi tiêu thì vẽ vòng tròn màu xám mặc định
            slicePaint.setColor(Color.parseColor("#E0E0E0"));
            canvas.drawOval(rectF, slicePaint);
        } else {
            // Bắt đầu vẽ từ vị trí 12 giờ
            float startAngle = -90f;
            for (int i = 0; i < sweepAngles.size(); i++) {
                slicePaint.setColor(sliceColors.get(i));
                float sweep = sweepAngles.get(i);

                // Tạo khe hở 2 độ giữa các phần nếu biểu đồ có từ 2 danh mục trở lên
                float gap = 0f;
                if (sweepAngles.size() > 1 && sweep > 4f) {
                    gap = 2f;
                }

                canvas.drawArc(rectF, startAngle, sweep - gap, true, slicePaint);
                startAngle += sweep;
            }
        }

        // Vẽ hình tròn trắng đè lên tâm để tạo kiểu biểu đồ vành khuyên
        float centerX = width / 2f;
        float centerY = height / 2f;
        float holeRadius = size * 0.30f;
        canvas.drawCircle(centerX, centerY, holeRadius, holePaint);

        // Vẽ "Tổng chi" và số tiền tương ứng ngay chính giữa tâm biểu đồ
        DecimalFormat df = new DecimalFormat("#,###");
        canvas.drawText("Tổng chi", centerX, centerY - 12f, textTitlePaint);
        canvas.drawText(df.format(totalExpense) + " đ", centerX, centerY + 34f, textAmountPaint);
    }
}