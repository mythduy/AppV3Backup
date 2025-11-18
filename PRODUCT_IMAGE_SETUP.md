# 📸 HƯỚNG DẪN THÊM HÌNH ẢNH SẢN PHẨM - STEP BY STEP

## 📋 DANH SÁCH 15 SẢN PHẨM CẦN HÌNH ẢNH

Bạn cần chuẩn bị **15 hình ảnh** cho các sản phẩm sau:

| ID | Tên Sản Phẩm | Tên File Hình Ảnh | Mô Tả |
|----|--------------|-------------------|-------|
| 1  | Arduino Uno R3 | `product_1.jpg` | Board Arduino Uno R3 |
| 2  | Raspberry Pi 4 | `product_2.jpg` | Raspberry Pi 4 Model B |
| 3  | ESP32 DevKit | `product_3.jpg` | Module ESP32 |
| 4  | Cảm biến DHT22 | `product_4.jpg` | Cảm biến nhiệt độ độ ẩm |
| 5  | Servo Motor SG90 | `product_5.jpg` | Servo motor nhỏ |
| 6  | LED RGB 5mm | `product_6.jpg` | LED RGB |
| 7  | Màn hình LCD 16x2 | `product_7.jpg` | Màn hình LCD |
| 8  | Cảm biến siêu âm HC-SR04 | `product_8.jpg` | Cảm biến khoảng cách |
| 9  | Module Relay 4 kênh | `product_9.jpg` | Module relay |
| 10 | Breadboard 830 | `product_10.jpg` | Breadboard |
| 11 | Jumper Wire | `product_11.jpg` | Dây jumper |
| 12 | Nguồn 5V 2A | `product_12.jpg` | Adapter nguồn |
| 13 | STM32 Blue Pill | `product_13.jpg` | Board STM32 |
| 14 | OLED 0.96 inch | `product_14.jpg` | Màn hình OLED |
| 15 | Module RFID RC522 | `product_15.jpg` | Module RFID |

---

## 🎯 BƯỚC 1: TẢI HÌNH ẢNH SẢN PHẨM

### Cách 1: Tìm hình ảnh miễn phí (Khuyến nghị)

#### 📌 Nguồn ảnh chất lượng cao:

**1. Unsplash** (https://unsplash.com/)
```
Tìm kiếm:
- "Arduino Uno"
- "Raspberry Pi"
- "ESP32"
- "DHT22 sensor"
- "electronic components"
```

**2. Pexels** (https://www.pexels.com/)
```
Tìm kiếm:
- "microcontroller"
- "electronics"
- "circuit board"
```

**3. Pixabay** (https://pixabay.com/)
```
Tìm kiếm:
- "Arduino"
- "electronics parts"
- "sensors"
```

**4. Google Images** (Sử dụng bộ lọc)
```
1. Tìm kiếm tên sản phẩm (ví dụ: "Arduino Uno R3")
2. Tools → Usage Rights → Creative Commons licenses
3. Size → Large (> 1024x768)
```

### Cách 2: Chụp ảnh thật (Nếu bạn có sản phẩm)

**Yêu cầu:**
- Nền trắng hoặc đơn giản
- Ánh sáng tốt
- Lấy nét rõ
- Kích thước tối thiểu 500x500px

---

## 🎨 BƯỚC 2: CHUẨN BỊ HÌNH ẢNH

### 2.1. Đổi tên file

**Quy tắc đặt tên:**
```
product_[ID].jpg
```

**Ví dụ:**
```
Arduino Uno R3     → product_1.jpg
Raspberry Pi 4     → product_2.jpg
ESP32 DevKit       → product_3.jpg
...
```

### 2.2. Chỉnh sửa kích thước (Nếu cần)

**Kích thước khuyến nghị:**
- Width: 800px - 1000px
- Height: 800px - 1000px
- Tỷ lệ: 1:1 (vuông)

**Tool online miễn phí:**
- **Squoosh** (https://squoosh.app/) - Nén và resize
- **TinyPNG** (https://tinypng.com/) - Nén ảnh
- **iloveimg** (https://www.iloveimg.com/resize-image) - Resize

**Cách resize với Squoosh:**
```
1. Mở https://squoosh.app/
2. Kéo thả ảnh vào
3. Resize → Width: 800px (giữ tỷ lệ)
4. Compress → Quality: 80-85%
5. Download
```

### 2.3. Danh sách file cần có

Sau khi hoàn thành, bạn sẽ có **15 file:**

```
✅ product_1.jpg   (Arduino Uno R3)
✅ product_2.jpg   (Raspberry Pi 4)
✅ product_3.jpg   (ESP32 DevKit)
✅ product_4.jpg   (Cảm biến DHT22)
✅ product_5.jpg   (Servo Motor SG90)
✅ product_6.jpg   (LED RGB 5mm)
✅ product_7.jpg   (Màn hình LCD 16x2)
✅ product_8.jpg   (Cảm biến siêu âm HC-SR04)
✅ product_9.jpg   (Module Relay 4 kênh)
✅ product_10.jpg  (Breadboard 830)
✅ product_11.jpg  (Jumper Wire)
✅ product_12.jpg  (Nguồn 5V 2A)
✅ product_13.jpg  (STM32 Blue Pill)
✅ product_14.jpg  (OLED 0.96 inch)
✅ product_15.jpg  (Module RFID RC522)
```

---

## 📁 BƯỚC 3: COPY FILE VÀO PROJECT

### 3.1. Mở thư mục trong File Explorer

**Đường dẫn:**
```
C:\Users\MKhang\Desktop\BaiTapAndroid\AppV2backup\app\src\main\assets\images\products\
```

**Cách mở nhanh:**
1. Mở File Explorer (Windows + E)
2. Copy đường dẫn trên vào thanh địa chỉ
3. Enter

### 3.2. Copy tất cả 15 file vào thư mục

**Bước thực hiện:**
```
1. Chọn tất cả 15 file ảnh đã chuẩn bị
2. Copy (Ctrl + C)
3. Paste vào thư mục products (Ctrl + V)
```

**Kết quả:**
```
app/src/main/assets/images/products/
├── product_1.jpg   ✅
├── product_2.jpg   ✅
├── product_3.jpg   ✅
├── product_4.jpg   ✅
├── product_5.jpg   ✅
├── product_6.jpg   ✅
├── product_7.jpg   ✅
├── product_8.jpg   ✅
├── product_9.jpg   ✅
├── product_10.jpg  ✅
├── product_11.jpg  ✅
├── product_12.jpg  ✅
├── product_13.jpg  ✅
├── product_14.jpg  ✅
└── product_15.jpg  ✅
```

---

## 💻 BƯỚC 4: CẬP NHẬT DATABASE

### 4.1. Mở Android Studio

Mở file: `app/src/main/java/com/example/ecommerceapp/database/DatabaseHelper.java`

### 4.2. Thêm method update hình ảnh

**Tìm dòng code:** (khoảng dòng 160-170)
```java
private void insertSampleProducts(SQLiteDatabase db) {
```

**Thêm method mới NGAY SAU insertDefaultUser():**

```java
// Thêm method này sau insertDefaultUser()
public void updateProductImages() {
    SQLiteDatabase db = this.getWritableDatabase();
    
    // Cập nhật hình ảnh cho từng sản phẩm
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_1.jpg' WHERE id = 1");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_2.jpg' WHERE id = 2");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_3.jpg' WHERE id = 3");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_4.jpg' WHERE id = 4");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_5.jpg' WHERE id = 5");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_6.jpg' WHERE id = 6");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_7.jpg' WHERE id = 7");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_8.jpg' WHERE id = 8");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_9.jpg' WHERE id = 9");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_10.jpg' WHERE id = 10");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_11.jpg' WHERE id = 11");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_12.jpg' WHERE id = 12");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_13.jpg' WHERE id = 13");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_14.jpg' WHERE id = 14");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_15.jpg' WHERE id = 15");
    
    db.close();
}
```

### 4.3. Gọi method trong MainActivity

**Mở file:** `app/src/main/java/com/example/ecommerceapp/MainActivity.java`

**Tìm method onCreate()** và **thêm vào cuối onCreate():**

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    // ... code hiện tại ...
    
    // THÊM 3 DÒNG NÀY VÀO CUỐI onCreate()
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    dbHelper.updateProductImages();
    // CHỈ CHẠY 1 LẦN, SAU ĐÓ XÓA HOẶC COMMENT LẠI
}
```

**⚠️ LƯU Ý QUAN TRỌNG:**
- Code này CHỈ CHẠY **MỘT LẦN DUY NHẤT**
- Sau khi chạy app 1 lần, hãy **XÓA HOẶC COMMENT** 2 dòng code trên
- Nếu không xóa, mỗi lần mở app sẽ update database (tốn tài nguyên)

---

## 🚀 BƯỚC 5: BUILD VÀ CHẠY APP

### 5.1. Clean và Rebuild Project

**Trong Android Studio:**
```
1. Build → Clean Project
2. Build → Rebuild Project
```

### 5.2. Chạy app

**Bấm nút Run** hoặc **Shift + F10**

### 5.3. Kiểm tra kết quả

**Mở app và kiểm tra:**
- ✅ Trang Home: Xem sản phẩm có hiển thị hình ảnh không
- ✅ Danh mục: Click vào category, xem sản phẩm
- ✅ Chi tiết sản phẩm: Click vào 1 sản phẩm xem hình lớn

---

## 🔧 BƯỚC 6: XÓA CODE TẠM THỜI (SAU KHI CHẠY APP)

### 6.1. Mở lại MainActivity.java

### 6.2. Comment hoặc xóa code update

**TÌM:**
```java
DatabaseHelper dbHelper = new DatabaseHelper(this);
dbHelper.updateProductImages();
```

**ĐỔI THÀNH:**
```java
// DatabaseHelper dbHelper = new DatabaseHelper(this);
// dbHelper.updateProductImages();
// ĐÃ UPDATE XONG - XÓA CODE NÀY
```

### 6.3. Rebuild lại

```
Build → Rebuild Project
```

---

## ✅ CHECKLIST HOÀN THÀNH

### Chuẩn bị hình ảnh
- [ ] Tải/tìm 15 hình ảnh sản phẩm
- [ ] Đổi tên file theo format: product_1.jpg → product_15.jpg
- [ ] Resize ảnh về 800x800px
- [ ] Nén ảnh (dưới 500KB mỗi file)

### Copy vào project
- [ ] Mở thư mục: `app/src/main/assets/images/products/`
- [ ] Copy tất cả 15 file vào thư mục
- [ ] Kiểm tra lại tên file (product_1.jpg → product_15.jpg)

### Cập nhật code
- [ ] Thêm method `updateProductImages()` vào DatabaseHelper.java
- [ ] Thêm code gọi method trong MainActivity.onCreate()
- [ ] Clean và Rebuild project

### Chạy app
- [ ] Run app lần đầu
- [ ] Kiểm tra hình ảnh hiển thị đúng
- [ ] Comment/xóa code update trong MainActivity
- [ ] Rebuild lại project

---

## 🐛 TROUBLESHOOTING - XỬ LÝ LỖI

### Lỗi 1: Hình ảnh không hiển thị

**Nguyên nhân:** Tên file không đúng hoặc đường dẫn sai

**Giải pháp:**
```
1. Kiểm tra lại tên file: product_1.jpg (không phải Product_1.jpg)
2. Kiểm tra thư mục: app/src/main/assets/images/products/
3. Clean Project → Rebuild Project
```

### Lỗi 2: App bị crash khi mở

**Nguyên nhân:** Lỗi trong code update

**Giải pháp:**
```
1. Check Logcat trong Android Studio
2. Kiểm tra lại code updateProductImages()
3. Kiểm tra database có tồn tại không
```

### Lỗi 3: Chỉ một số ảnh hiển thị

**Nguyên nhân:** Thiếu file hoặc tên file sai

**Giải pháp:**
```
1. Kiểm tra lại 15 file trong thư mục products
2. Đảm bảo tất cả file có format: product_[ID].jpg
3. Kiểm tra ID sản phẩm trong database
```

### Lỗi 4: Ảnh bị vỡ hoặc mờ

**Nguyên nhân:** File ảnh kích thước quá nhỏ

**Giải pháp:**
```
1. Tìm ảnh có độ phân giải cao hơn (> 800x800px)
2. Sử dụng ảnh PNG thay vì JPG (chất lượng tốt hơn)
```

---

## 💡 TIPS & TRICKS

### 1. Nén ảnh hiệu quả

**TinyPNG** (https://tinypng.com/)
```
- Kéo thả tất cả 15 ảnh vào
- Chờ nén xong
- Download tất cả
- Tiết kiệm 60-70% dung lượng
```

### 2. Tìm ảnh nhanh với Google

**Mẹo tìm kiếm:**
```
1. Tìm: "Arduino Uno R3 site:unsplash.com"
2. Tìm: "ESP32 white background"
3. Tìm: "electronic component isolated"
```

### 3. Batch rename nhanh

**Windows:**
```
1. Chọn tất cả file
2. F2 (Rename)
3. Gõ: product_
4. Enter
5. File sẽ tự động đánh số: product_ (1), product_ (2)...
6. Thay dấu cách thành số thủ công
```

### 4. Kiểm tra nhanh

**Sau khi copy file, chạy lệnh trong Terminal:**
```powershell
# Đếm số file trong thư mục
(Get-ChildItem "app\src\main\assets\images\products\").Count
# Kết quả phải là: 15
```

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề, kiểm tra:

1. **File tồn tại chưa:**
   ```
   C:\Users\MKhang\Desktop\BaiTapAndroid\AppV2backup\app\src\main\assets\images\products\
   ```

2. **Tên file chính xác:**
   ```
   product_1.jpg, product_2.jpg, ..., product_15.jpg
   ```

3. **Code trong DatabaseHelper.java:**
   ```java
   public void updateProductImages() { ... }
   ```

4. **Code trong MainActivity.java:**
   ```java
   dbHelper.updateProductImages();
   ```

---

## 🎉 KẾT QUẢ MONG ĐỢI

Sau khi hoàn thành tất cả bước, bạn sẽ thấy:

✅ **Trang Home:** Tất cả sản phẩm có hình ảnh đẹp
✅ **Danh mục:** Sản phẩm trong mỗi category có ảnh
✅ **Chi tiết:** Hình ảnh sản phẩm hiển thị rõ nét
✅ **Giỏ hàng:** Sản phẩm trong cart có thumbnail
✅ **Đơn hàng:** Order history hiển thị ảnh sản phẩm

---

**Chúc bạn thành công! 🚀**

Nếu cần hỗ trợ thêm, hãy báo tôi biết!
