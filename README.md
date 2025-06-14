<h1 align="center">🥗 WEBSITE BÁN THỰC PHẨM SẠCH</h1>
<p align="center"><strong>Đồ án Khóa luận tốt nghiệp - Đại học</strong></p>

<p align="center">
  <img src="https://img.shields.io/badge/SpringBoot-Backend-brightgreen?style=flat-square&logo=springboot" />
  <img src="https://img.shields.io/badge/MySQL-Database-blue?style=flat-square&logo=mysql" />
  <img src="https://img.shields.io/badge/Bootstrap-Frontend-purple?style=flat-square&logo=bootstrap" />
  <img src="https://img.shields.io/badge/HTML5-Frontend-orange?style=flat-square&logo=html5" />
  <img src="https://img.shields.io/badge/CSS3-Frontend-blue?style=flat-square&logo=css3" />
  <img src="https://img.shields.io/badge/JavaScript-Frontend-yellow?style=flat-square&logo=javascript" />
  <img src="https://img.shields.io/badge/Thymeleaf-Templating-green?style=flat-square&logo=thymeleaf" />
  <img src="https://img.shields.io/badge/Firebase-Realtime-yellow?style=flat-square&logo=firebase" />
  <img src="https://img.shields.io/badge/GoogleMaps-Location-red?style=flat-square&logo=googlemaps" />
  <img src="https://img.shields.io/badge/OpenAI-AI_Chatbot-orange?style=flat-square&logo=openai" />
  <img src="https://img.shields.io/badge/Chart.js-Charts-blue?style=flat-square&logo=chartdotjs" />
  <img src="https://img.shields.io/badge/WebSocket-Realtime-green?style=flat-square&logo=websocket" />
  <img src="https://img.shields.io/badge/VNPay-Payment-purple?style=flat-square&logo=vnpay" />
  <img src="https://img.shields.io/badge/ZaloPay-Payment-blue?style=flat-square&logo=zalopay" />
  <img src="https://img.shields.io/badge/DataTables-Tables-green?style=flat-square&logo=datatables" />
</p>

---

## 🎓 Thông tin khóa luận

- **Họ tên sinh viên:** Ngô Ngọc Thông  
- **Mã số sinh viên:** 21110312  
- **Lớp:** 21110CLST2B  
- **Khoa:** Công nghệ thông tin  
- **Giảng viên hướng dẫn:** TS. Lê Vĩnh Thịnh  

---

## 🧭 Kiến trúc hệ thống

### ✳️ Mô hình Spring Boot MVC

<p align="center">
  <img src="https://github.com/user-attachments/assets/7fd7e251-1748-45d8-b664-b65da6760095" alt="Mô hình Spring Boot MVC" width="600" />
</p>
<p align="center"><em>Mô hình kiến trúc Spring Boot MVC</em></p>

- **Browser**: Gửi yêu cầu HTTP đến ứng dụng, hiển thị kết quả trả về.
- **Controller**: Tiếp nhận request từ client, điều phối logic xử lý đến Service.
- **Service**: Xử lý nghiệp vụ chính, kiểm tra tính hợp lệ, điều phối đến Repository.
- **Repository**: Truy xuất cơ sở dữ liệu qua JPA (thường extends từ JpaRepository).
- **Entity**: Đại diện cho bảng dữ liệu trong cơ sở dữ liệu.
- **MySQL**: Nơi lưu trữ dữ liệu cho toàn bộ hệ thống.

---

## 🚀 Chức năng chính

### 🔐 Người dùng
- Đăng ký & xác thực qua OTP (email)
- Đăng nhập, đổi mật khẩu (xác thực OTP)
- Quản lý tài khoản cá nhân
- Đánh giá sản phẩm (có ảnh, nhận point)
- Điểm danh nhận point
- Xem và hủy đơn hàng

### 🛒 Mua sắm
- Trang chủ:
  - Sản phẩm theo mùa (Tháng 1-12)
  - Mới về, bán chạy, theo danh mục
- Chi tiết sản phẩm:
  - Mô tả, đánh giá, gợi ý dựa vào AI
  - Sản phẩm đã xem gần đây
- Tìm kiếm & lọc (tên, giá, loại)
- Giỏ hàng & Yêu thích

### 💳 Thanh toán
- ZaloPay, VNPay, COD
- Áp dụng Voucher + Point
- Email xác nhận đơn hàng
- Ước tính chi phí + chọn chi nhánh giao hàng phù hợp

### 🤖 Chatbot AI
- Gợi ý sản phẩm
- Trả lời thắc mắc thực phẩm
- Dẫn link đặt hàng

### 📝 Blog
- Bài viết hướng dẫn, chia sẻ
- Gắn tag sản phẩm (giỏ quà, combo,...)

---

## 🛠️ Chức năng quản trị (Admin)

- Quản lý sản phẩm (gồm ảnh phụ)
- Quản lý danh mục
- Quản lý người dùng (sửa, khóa)
- Quản lý đơn hàng, chi tiết đơn
- Quản lý đánh giá (ẩn/hiện, phản hồi)
- Quản lý voucher
- Quản lý bài blog
- Quản lý chi nhánh giao hàng
- Thống kê (biểu đồ, bảng)
- Thông báo realtime (đơn hàng mới, đánh giá mới) qua WebSocket

---

## 🔧 Công nghệ sử dụng

| Layer       | Công nghệ |
|-------------|-----------|
| Backend     | Spring Boot, REST API, MVC Controller, Spring Security, JPA |
| Frontend    | HTML5, CSS3, Bootstrap, Thymeleaf |
| Database    | MySQL |
| Authentication | Email OTP, SMS OTP |
| Chatbot AI  | OpenAI API |
| Maps & định vị | Google Maps API |
| Realtime    | WebSocket |
| Payment     | ZaloPay, VNPay |
| Biểu đồ     | Chart.js |
| Giao diện bảng | DataTables |

---

### 📌 Logo các công nghệ sử dụng

<p align="center">
  <img src="https://www.vectorlogo.zone/logos/springio/springio-icon.svg" alt="Spring Boot" width="50" />
  <img src="https://www.vectorlogo.zone/logos/mysql/mysql-icon.svg" alt="MySQL" width="50" />
  <img src="https://www.vectorlogo.zone/logos/getbootstrap/getbootstrap-icon.svg" alt="Bootstrap" width="50" />
  <img src="https://www.vectorlogo.zone/logos/w3_html5/w3_html5-icon.svg" alt="HTML5" width="50" />
  <img src="https://www.vectorlogo.zone/logos/w3_css/w3_css-icon.svg" alt="CSS3" width="50" />
  <img src="https://www.vectorlogo.zone/logos/javascript/javascript-icon.svg" alt="JavaScript" width="50" />
  <img src="https://www.thymeleaf.org/images/thymeleaf.png" alt="Thymeleaf" width="50" />
  <img src="https://www.vectorlogo.zone/logos/firebase/firebase-icon.svg" alt="Firebase" width="50" />
  <img src="https://www.vectorlogo.zone/logos/google_maps/google_maps-icon.svg" alt="Google Maps" width="50" />
  <img src="https://forums.getdrafts.com/uploads/default/optimized/2X/e/e7b4d0c64bccff6787857f8d940e8193b9ab9a90_2_1024x710.jpeg" alt="OpenAI" width="50" />
  <img src="https://scontent.fsgn5-10.fna.fbcdn.net/v/t39.30808-6/202166185_2021396718013233_8499389898242103910_n.png?_nc_cat=107&ccb=1-7&_nc_sid=6ee11a&_nc_ohc=VyBV0V4GJy0Q7kNvwEHVhmn&_nc_oc=Adk68i-YXpWou_Opj7nmNLKHmZ7czQ09njXChKd48ab0sdv3PqY9qrl0y6uivbFnJwQ&_nc_zt=23&_nc_ht=scontent.fsgn5-10.fna&_nc_gid=-wejJMVUqI3G8KuAadE-kA&oh=00_AfMhAapx9As4zF0JRpW95keM1HK082enI5RZcCA4KoYHzQ&oe=68532CE9" alt="VNPay" width="50" />
  <img src="https://scontent.fsgn5-9.fna.fbcdn.net/v/t39.30808-6/484698273_1047207607559037_2831762024301684186_n.jpg?_nc_cat=1&ccb=1-7&_nc_sid=6ee11a&_nc_ohc=cAkuOzVk5r4Q7kNvwEpzXcS&_nc_oc=AdlvIWH6zR1l1gK5w7DRhnxtvbh2oJ8zZ37qmEaqXK9M9IQNCptmZZv9kq-JaDqgdUk&_nc_zt=23&_nc_ht=scontent.fsgn5-9.fna&_nc_gid=hD9-gWhWvg5455E49U7SXQ&oh=00_AfPeqMTgyJQbzevLRgmuqsx0sOeKuyaRCr_HEqZKIv8gRw&oe=68533D0E" alt="ZaloPay" width="50" />
</p>

---

## 📸 Giao diện minh họa *(tùy chọn)*

> ![image](https://github.com/user-attachments/assets/d42cccdc-0415-492c-bef5-4a45ca8f608f) ![image](https://github.com/user-attachments/assets/47ad68fd-2001-48ee-a76d-df2ef74e6317) ![image](https://github.com/user-attachments/assets/240726ac-2b55-423a-840f-7f9bbe84b8ba)
> ![image](https://github.com/user-attachments/assets/b4939e33-2a46-449d-9e93-9f3458060295) ![image](https://github.com/user-attachments/assets/ce24cf26-110a-4716-b071-36c903577210) ![image](https://github.com/user-attachments/assets/3e2d2c18-c576-4558-9fd6-257f151bdf10)
> ![image](https://github.com/user-attachments/assets/886bddfe-36d4-468b-96f1-741e6ab8af52) ![image](https://github.com/user-attachments/assets/c57a70a5-3387-4a86-9cee-b66781166631)


---

## 📬 Liên hệ

**Sinh viên thực hiện:** Ngô Ngọc Thông  
**Email:** 21110312@student.hcmute.edu.vn
**GitHub:** [github.com/KoS9999](#)

---

> © 2025 - Khóa luận tốt nghiệp ngành CNTT  
> *Thực phẩm sạch – Vì sức khỏe người Việt 🇻🇳*
