# 📧 HƯỚNG DẪN CẤU HÌNH GMAIL ĐỂ GỬI EMAIL TỰ ĐỘNG

## ⚠️ QUAN TRỌNG - PHẢI LÀM TRƯỚC KHI CHẠY APP

### 🔑 Bước 1: Tạo Gmail App Password

1. **Đăng nhập Gmail** của bạn (email admin để gửi)

2. **Truy cập:** https://myaccount.google.com/security

3. **Bật xác thực 2 bước (2-Step Verification):**
   - Tìm "2-Step Verification"
   - Click "Get Started"
   - Follow hướng dẫn để bật

4. **Tạo App Password:**
   - Sau khi bật 2-Step, quay lại: https://myaccount.google.com/apppasswords
   - Chọn "Select app" → "Mail"
   - Chọn "Select device" → "Other (Custom name)"
   - Đặt tên: "Electronics Shop Android"
   - Click "Generate"
   - **Copy mật khẩu 16 ký tự** (dạng: xxxx xxxx xxxx xxxx)

---

### 📝 Bước 2: Cập nhật Code

Mở file: `ForgotPasswordActivity.java`

Tìm dòng này (khoảng dòng 85):

```java
final String fromEmail = "electronicshop.demo@gmail.com"; // Email admin của bạn
final String fromPassword = "your_app_password_here"; // App Password của Gmail
```

**Thay đổi thành:**

```java
final String fromEmail = "your_email@gmail.com"; // Email Gmail của bạn
final String fromPassword = "xxxx xxxx xxxx xxxx"; // App Password vừa tạo (16 ký tự)
```

**Ví dụ:**
```java
final String fromEmail = "electronicshop2025@gmail.com";
final String fromPassword = "abcd efgh ijkl mnop";
```

---

### 🚀 Bước 3: Build & Run

1. **Sync Gradle** (đã thêm JavaMail dependency)
2. **Build Project**
3. **Run App**

---

## ✅ Cách hoạt động:

1. User nhập email đã đăng ký
2. Hệ thống kiểm tra email có trong database
3. Generate mật khẩu tạm thời (8 ký tự random)
4. Cập nhật password vào database
5. **TỰ ĐỘNG GỬI EMAIL** từ admin Gmail → user email
6. User nhận email với mật khẩu mới
7. Đăng nhập bằng mật khẩu tạm thời

---

## 📧 Email sẽ có dạng:

```
From: electronicshop2025@gmail.com
To: user@example.com
Subject: Reset Password - Electronics Shop

Xin chào [Username],

Bạn đã yêu cầu reset mật khẩu cho tài khoản Electronics Shop.

Mật khẩu tạm thời của bạn là: Abc12345

Vui lòng đăng nhập và đổi mật khẩu mới trong phần Cài đặt tài khoản.

Nếu bạn không yêu cầu reset mật khẩu, vui lòng bỏ qua email này.

Trân trọng,
Electronics Shop Team
```

---

## 🛡️ BẢO MẬT:

### ❌ KHÔNG NÊN (cho production):
- Hardcode email/password trong code
- Push code có password lên Git

### ✅ NÊN LÀM (cho production thật):
- Lưu credentials trong `local.properties` (không push lên Git)
- Hoặc dùng backend server để gửi email
- Hoặc dùng Firebase Cloud Functions

### 📝 Cách bảo mật tốt hơn (optional):

**1. Tạo file:** `local.properties` (đã có sẵn)

**2. Thêm vào `local.properties`:**
```properties
admin.email=your_email@gmail.com
admin.password=your_app_password_here
```

**3. Trong `ForgotPasswordActivity.java`:**
```java
// Đọc từ BuildConfig
final String fromEmail = BuildConfig.ADMIN_EMAIL;
final String fromPassword = BuildConfig.ADMIN_PASSWORD;
```

---

## 🐛 Troubleshooting:

### Lỗi "Authentication failed":
- ✅ Kiểm tra đã bật 2-Step Verification chưa
- ✅ App Password phải là 16 ký tự (bỏ dấu cách cũng được)
- ✅ Email phải là Gmail, không phải email khác

### Email không gửi được:
- ✅ Kiểm tra Internet connection
- ✅ Kiểm tra email admin có đúng không
- ✅ Xem Logcat có lỗi gì không

### Email vào Spam:
- ✅ Bình thường, email tự động thường vào Spam
- ✅ User cần check cả Spam folder

---

## 📱 Test:

1. **Tạo tài khoản test** với email thật của bạn
2. **Bấm "Forgot Password?"**
3. **Nhập email** đã đăng ký
4. **Đợi 5-10 giây** (đang gửi email)
5. **Check email** (cả Inbox và Spam)
6. **Đăng nhập** bằng mật khẩu mới

---

## 🎯 Lưu ý quan trọng:

1. **JavaMail API** đã được thêm vào `build.gradle.kts`
2. **AsyncTask** chạy ở background thread (không block UI)
3. **Nếu gửi email lỗi** → Vẫn hiển thị password trong dialog (backup)
4. **Toast** thông báo trạng thái: Đang gửi → Thành công/Lỗi

---

**Chúc bạn thành công! 🚀**

Nếu gặp lỗi, hãy check Logcat để xem lỗi cụ thể là gì.
