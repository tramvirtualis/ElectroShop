## 🚀 Giới thiệu

**HomeTech** là một trang web thương mại điện tử bán đồ công nghệ, được xây dựng bằng Spring Boot và các công nghệ web hiện đại khác. Dự án này cung cấp một nền tảng đầy đủ tính năng cho cả người mua và người bán, với các vai trò người dùng khác nhau và các chức năng tương ứng.

---

## ✨ Tính năng nổi bật

### 🔐 Chung
- **Bảo mật:** Mật khẩu được mã hóa bằng Spring Security.
- **Tìm kiếm & Lọc:** Tìm kiếm và lọc sản phẩm nâng cao.
- **Xác thực tài khoản:** Đăng ký và quên mật khẩu với xác thực qua Email.
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
- **Đánh giá:** Đánh giá và bình luận (văn bản, hình ảnh/video) về các sản phẩm đã mua.



### 👑 Admin (Quản trị viên)
- **Quản lý người dùng:** Tìm kiếm và quản lý tài khoản người dùng.
- **Quản lý sản phẩm:** Quản lý sản phẩm của tất cả các cửa hàng.
- **Quản lý danh mục:** Thêm, sửa, xóa các danh mục sản phẩm.
- - **Quản lý đơn hàng:** cập nhật trạng thái đơn hàng.



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
