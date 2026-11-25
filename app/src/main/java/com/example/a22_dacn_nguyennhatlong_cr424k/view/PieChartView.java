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
    private Paint slicePaint;
    private Paint holePaint;
    private Paint textTitlePaint;
    private Paint textAmountPaint;
    private RectF rectF;

    private List<Float> sweepAngles = new ArrayList<>();
    private List<Integer> sliceColors = new ArrayList<>();
    private double totalExpense = 0;

    // Bảng màu rõ nét tương ứng các danh mục
    public static final int[] CHART_COLORS = {
            Color.parseColor("#1565C0"), // Xanh dương đậm
            Color.parseColor("#EF6C00"), // Cam
            Color.parseColor("#2E7D32"), // Xanh lá
            Color.parseColor("#C2185B"), // Hồng đậm
            Color.parseColor("#6A1B9A"), // Tím
            Color.parseColor("#00838F")  // Xanh ngọc
    };

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slicePaint.setStyle(Paint.Style.FILL);

        holePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        holePaint.setColor(Color.WHITE);
        holePaint.setStyle(Paint.Style.FILL);

        textTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textTitlePaint.setColor(Color.parseColor("#757575"));
        textTitlePaint.setTextAlign(Paint.Align.CENTER);
        textTitlePaint.setTextSize(32f);

        textAmountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textAmountPaint.setColor(Color.parseColor("#212121"));
        textAmountPaint.setTextAlign(Paint.Align.CENTER);
        textAmountPaint.setTextSize(40f);
        textAmountPaint.setFakeBoldText(true);

        rectF = new RectF();
    }

    public void setData(Map<String, Double> stats, double total) {
        sweepAngles.clear();
        sliceColors.clear();
        this.totalExpense = total;

        if (stats != null && total > 0) {
            int colorIndex = 0;
            for (Map.Entry<String, Double> entry : stats.entrySet()) {
                float angle = (float) ((entry.getValue() / total) * 360.0);
                sweepAngles.add(angle);
                sliceColors.add(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                colorIndex++;
            }
        }
        invalidate(); // Yêu cầu vẽ lại biểu đồ
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height) - 30;
        if (size <= 0) return;

        float left = (width - size) / 2f;
        float top = (height - size) / 2f;
        rectF.set(left, top, left + size, top + size);

        if (sweepAngles.isEmpty() || totalExpense <= 0) {
            slicePaint.setColor(Color.parseColor("#E0E0E0"));
            canvas.drawOval(rectF, slicePaint);
        } else {
            float startAngle = -90f; // Bắt đầu từ đỉnh 12 giờ
            for (int i = 0; i < sweepAngles.size(); i++) {
                slicePaint.setColor(sliceColors.get(i));
                float sweep = sweepAngles.get(i);
                // Tạo khe hở 2 độ giữa các miếng nếu có nhiều hơn 1 danh mục
                float gap = (sweepAngles.size() > 1 && sweep > 4f) ? 2f : 0f;
                canvas.drawArc(rectF, startAngle, sweep - gap, true, slicePaint);
                startAngle += sweep;
            }
        }

        // Khoét lỗ tròn ở giữa tạo kiểu Donut Chart hiện đại
        float centerX = width / 2f;
        float centerY = height / 2f;
        float holeRadius = size * 0.30f;
        canvas.drawCircle(centerX, centerY, holeRadius, holePaint);

        // Vẽ chữ tổng chi ở giữa tâm biểu đồ
        DecimalFormat df = new DecimalFormat("#,###");
        canvas.drawText("Tổng chi", centerX, centerY - 12f, textTitlePaint);
        canvas.drawText(df.format(totalExpense) + " đ", centerX, centerY + 34f, textAmountPaint);
    }
}