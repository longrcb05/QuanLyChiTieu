# 📱 Meme App - Ứng Dụng Quản Lý Chi Tiêu Cá Nhân

**Đồ án cá nhân môn học:** Lập trình ứng dụng dành cho thiết bị di động  
**Lớp:** CR424K  
**SVTH:** Nguyễn Nhật Long  
**GVHD:** ThS. Võ Minh Thông  

---

## 📌 Giới thiệu đề tài
**Meme App** là ứng dụng di động chạy trên nền tảng Android giúp người dùng theo dõi thu nhập, kiểm soát chi tiêu hằng ngày theo mô hình phân bổ **Hũ chi tiêu**, lưu trữ hình ảnh hóa đơn chứng từ trực quan theo lịch và thống kê tỷ lệ chi tiêu bằng biểu đồ tròn.

---

## ✨ Các chức năng chính của ứng dụng

1. **Tổng quan tài chính theo thời gian thực:**
   - Hiển thị **Số dư hiện tại**, **Tổng Thu**, **Tổng Chi** và thẻ nổi bật **Hôm nay chi** ngay trên màn hình chính.
   - Tự động tính toán và cập nhật số dư ngay khi phát sinh, chỉnh sửa hoặc xóa giao dịch.

2. **Quản lý chi tiêu trực quan theo Lịch (Trang chủ):**
   - Giao diện lịch tháng (7 cột từ Thứ 2 đến Chủ nhật) hỗ trợ chuyển đổi linh hoạt giữa các tháng.
   - Hiển thị trực tiếp hình ảnh hóa đơn đã chụp hoặc biểu tượng màu sắc phân loại danh mục lên từng ô ngày.
   - Chạm vào từng ngày trên lịch để xem chi tiết danh sách giao dịch phát sinh trong ngày đó.

3. **Quản lý Giao dịch Thu / Chi & Lưu trữ Hóa đơn:**
   - Thêm mới, chỉnh sửa, tìm kiếm (theo nội dung, danh mục, tên hũ) và lọc giao dịch theo loại (`Tất cả`, `Khoản Thu`, `Khoản Chi`).
   - Nhấn giữ vào giao dịch để xóa và tự động hoàn trả lại số tiền vào hũ tương ứng.
   - Tích hợp **Camera** chụp ảnh hóa đơn đính kèm vào từng giao dịch; hỗ trợ chạm vào ảnh thu nhỏ hoặc khung ảnh để **xem phóng to hóa đơn cũ** và chụp lại khi cần.

4. **Quản lý Hũ Chi Tiêu (Mô hình phân bổ ngân sách):**
   - Khởi tạo sẵn các hũ mẫu (*Ví chính*, *Ăn uống & Sinh hoạt*, *Quỹ tiết kiệm*) và cho phép tạo thêm hũ mới kèm hạn mức mục tiêu.
   - Chạm trực tiếp vào hũ để nạp thêm tiền; hiển thị thanh tiến trình (`ProgressBar`) và phần trăm số dư hiện tại so với mục tiêu.
   - Cảnh báo khi chi tiêu vượt quá số dư hiện có trong hũ.

5. **Thống kê trực quan bằng Biểu đồ tròn (Donut Chart):**
   - Tự động gom nhóm các khoản chi theo từng danh mục (*Ăn uống*, *Di chuyển*, *Học tập*, *Mua sắm*, *Hóa đơn*, *Khác*).
   - Vẽ biểu đồ tròn chia tỷ lệ phần trăm màu sắc rõ nét kèm tổng chi ở tâm biểu đồ và bảng chú thích chi tiết bên dưới.

---

## 🛠️ Công nghệ & Môi trường phát triển
- **Ngôn ngữ lập trình:** Java
- **Giao diện người dùng:** XML (`RelativeLayout`, `LinearLayout`, `CardView`, `GridView`, `ListView`, `BottomNavigationView`, `FloatingActionButton`, Custom `Canvas` View)
- **Cơ sở dữ liệu cục bộ:** SQLite (`SQLiteOpenHelper`)
- **Công cụ phát triển:** Android Studio, Gradle Kotlin DSL (`build.gradle.kts`)

---

## 📂 Cấu trúc thư mục dự án

```text
22_DACN_NguyenNhatLong_CR424K/
├── app/
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml                  # Khai báo các Activity và quyền CAMERA
│           ├── java/com/example/a22_dacn_nguyennhatlong_cr424k/
│           │   ├── MainActivity.java                # Màn hình chính quản lý 4 tab (Lịch, Giao dịch, Hũ, Thống kê)
│           │   ├── AddTransactionActivity.java      # Màn hình Thêm / Sửa giao dịch, chụp và xem phóng to hóa đơn
│           │   ├── adapter/
│           │   │   ├── CalendarDayAdapter.java      # Adapter đổ dữ liệu từng ô ngày lên lưới lịch (GridView)
│           │   │   ├── TransactionAdapter.java      # Adapter hiển thị danh sách giao dịch Thu/Chi và ảnh thu nhỏ
│           │   │   └── JarAdapter.java              # Adapter hiển thị danh sách Hũ chi tiêu và thanh tiến trình
│           │   ├── database/
│           │   │   └── DatabaseHelper.java          # Quản lý CSDL SQLite (bảng transactions & money_jars)
│           │   ├── model/
│           │   │   ├── Transaction.java             # Đối tượng Giao dịch (Thu/Chi, danh mục, đường dẫn ảnh, mã hũ)
│           │   │   ├── MoneyJar.java                # Đối tượng Hũ chi tiêu (tên hũ, mục tiêu, số dư hiện tại)
│           │   │   └── CalendarDayItem.java         # Đối tượng ô ngày trên lịch (số ngày, chuỗi ngày, list giao dịch)
│           │   └── view/
│           │       └── PieChartView.java            # Custom View tự vẽ biểu đồ tròn (Donut Chart) bằng Canvas
│           └── res/
│               ├── layout/
│               │   ├── activity_main.xml            # Giao diện màn hình chính và 4 khung tab chức năng
│               │   ├── activity_add_transaction.xml # Giao diện biểu mẫu nhập / sửa giao dịch và khung hóa đơn
│               │   ├── item_calendar_day.xml        # Giao diện từng ô ngày trên lưới lịch
│               │   ├── item_transaction.xml         # Giao diện từng dòng giao dịch trong ListView
│               │   └── item_jar.xml                 # Giao diện từng thẻ Hũ chi tiêu trong ListView
│               └── menu/
│                   ├── bottom_nav_menu.xml          # Menu thanh điều hướng 4 tab ở đáy màn hình
│                   └── main_menu.xml                # Menu tùy chọn lọc nhanh trên thanh tiêu đề (ActionBar)
├── gradle/                                          # Cấu hình Gradle Wrapper & Version Catalog
├── build.gradle.kts                                 # Cấu hình build cấp dự án
└── settings.gradle.kts                              # Khai báo tên dự án và module app
