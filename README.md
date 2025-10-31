## 🚀 Giới thiệu

**StarShop** là một trang web thương mại điện tử chuyên bán hoa, được xây dựng bằng Spring Boot và các công nghệ web hiện đại khác. Dự án này cung cấp một nền tảng đầy đủ tính năng cho cả người mua và người bán, với các vai trò người dùng khác nhau và các chức năng tương ứng.

---

## ✨ Tính năng nổi bật

### 🔐 Chung
- **Bảo mật:** Mật khẩu được mã hóa bằng Spring Security.
- **Tìm kiếm & Lọc:** Tìm kiếm và lọc sản phẩm nâng cao.
- **Xác thực tài khoản:** Đăng ký và quên mật khẩu với xác thực OTP qua Email.
- **Đăng nhập/Đăng xuất:** Chức năng đăng nhập, đăng xuất an toàn.

### 👤 Guest (Khách)
- **Trang chủ:** Giao diện trang chủ trực quan.
- **Sản phẩm nổi bật:** Hiển thị top 10 sản phẩm bán chạy nhất từ các cửa hàng.

### 🧑 User (Người dùng)
- **Trang sản phẩm:** Xem sản phẩm theo danh mục (sản phẩm mới, bán chạy, đánh giá cao, yêu thích) với phân trang hoặc lazy loading.
- **Trang cá nhân:** Quản lý thông tin cá nhân và nhiều địa chỉ giao hàng.
- **Giỏ hàng:** Giỏ hàng được lưu trữ trong cơ sở dữ liệu.
- **Thanh toán:** Hỗ trợ thanh toán khi nhận hàng (COD) và VNPAY.
- **Lịch sử mua hàng:** Theo dõi trạng thái đơn hàng (mới, đã xác nhận, đang giao, đã giao, hủy, trả hàng/hoàn tiền).
- **Tương tác:** Thích sản phẩm, xem lại các sản phẩm đã xem.
- **Đánh giá:** Đánh giá và bình luận (văn bản, hình ảnh/video) về các sản phẩm đã mua.
- **Mã giảm giá:** Áp dụng mã giảm giá khi thanh toán.

### 🏪 Vendor (Người bán)
- **Bao gồm tất cả các quyền của User.**
- **Đăng ký cửa hàng:** Dễ dàng đăng ký và quản lý cửa hàng của riêng bạn.
- **Quản lý sản phẩm:** Thêm, sửa, xóa sản phẩm của cửa hàng.
- **Quản lý đơn hàng:** Theo dõi và quản lý đơn hàng của cửa hàng theo trạng thái.
- **Khuyến mãi:** Tạo và quản lý các chương trình khuyến mãi.
- **Doanh thu:** Thống kê và quản lý doanh thu của cửa hàng.

### 👑 Admin (Quản trị viên)
- **Quản lý người dùng:** Tìm kiếm và quản lý tài khoản người dùng.
- **Quản lý sản phẩm:** Quản lý sản phẩm của tất cả các cửa hàng.
- **Quản lý danh mục:** Thêm, sửa, xóa các danh mục sản phẩm.
- **Chiết khấu:** Quản lý chiết khấu của ứng dụng cho các cửa hàng.
- **Quản lý khuyến mãi:** Quản lý các chương trình khuyến mãi chung (giảm giá sản phẩm, phí vận chuyển).
- **Quản lý nhà vận chuyển:** Quản lý thông tin và phí vận chuyển của các nhà vận chuyển.

### 🚚 Shipper (Người giao hàng)
- **Quản lý đơn hàng:** Xem và quản lý các đơn hàng được phân công.
- **Thống kê:** Thống kê các đơn hàng đã được giao.

---

## 🛠️ Công nghệ sử dụng

- **Backend:** Spring Boot, Spring Security, Spring Data JPA, Spring Validation, Thymeleaf, Java Mail Sender, Websocket và JWT 
- **Frontend:** HTML5, CSS3, Bootstrap, Google Fonts và JavaScript
- **Trang trí:** Decorator Sitemesh
- **Cơ sở dữ liệu:** SQL Server
- **Xác thực:** JWT (JSON Web Tokens), csrf token, spring security, Bcrypt
- **Giao tiếp thời gian thực:** Websocket

---

## 👥 Tác giả

Dự án này được thực hiện bởi:

- **Hoàng Thanh Trí**
- **Nguyễn Thị Ngọc Trâm**

---

## 📦 Cài đặt và Chạy thử

1. **Clone a repository:**
   ```
   git clone [https://your-repository-url.git](https://github.com/tramvirtualis/ElectroShop)
   ```
2. **Cấu hình cơ sở dữ liệu:**
   - Mở file `application.properties` và chỉnh sửa thông tin kết nối đến SQL Server của bạn.
3. **Chạy ứng dụng:**
   - Mở dự án bằng IDE yêu thích của bạn và chạy với Spring Boot.
