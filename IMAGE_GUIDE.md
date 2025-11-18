# 📷 Hướng Dẫn Thêm Hình Ảnh Vào App

## 📁 Cấu Trúc Thư Mục Đã Tạo

```
app/src/main/
├── assets/
│   └── images/
│       ├── products/          # Hình ảnh sản phẩm
│       └── banners/           # Hình ảnh banner quảng cáo
└── res/
    ├── mipmap-xxxhdpi/        # Logo app (192x192px)
    ├── mipmap-xxhdpi/         # Logo app (144x144px)
    ├── mipmap-xhdpi/          # Logo app (96x96px)
    ├── mipmap-hdpi/           # Logo app (72x72px)
    └── mipmap-mdpi/           # Logo app (48x48px)
```

---

## 🎨 1. Logo App (Icon)

### Cách 1: Sử dụng Android Studio Image Asset Studio (Khuyến nghị)

1. **Mở Image Asset Studio:**
   - Trong Android Studio: `Right-click` vào thư mục `res`
   - Chọn: `New` → `Image Asset`

2. **Tạo Launcher Icon:**
   - **Icon Type**: Launcher Icons (Adaptive and Legacy)
   - **Name**: `ic_launcher`
   - **Foreground Layer**: 
     - Chọn `Image` 
     - Browse file logo của bạn (PNG, JPG)
     - Adjust padding nếu cần
   - **Background Layer**: 
     - Chọn màu background hoặc image
   - Click `Next` → `Finish`

3. **Kết quả:**
   - Logo sẽ tự động tạo cho tất cả density (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
   - File sẽ ở: `res/mipmap-*/ic_launcher.png`

### Cách 2: Thêm Logo Thủ Công

**Kích thước cần chuẩn bị:**

| Density  | Kích thước | Thư mục           |
|----------|------------|-------------------|
| mdpi     | 48x48px    | mipmap-mdpi       |
| hdpi     | 72x72px    | mipmap-hdpi       |
| xhdpi    | 96x96px    | mipmap-xhdpi      |
| xxhdpi   | 144x144px  | mipmap-xxhdpi     |
| xxxhdpi  | 192x192px  | mipmap-xxxhdpi    |

**Bước thực hiện:**

1. Chuẩn bị logo ở các kích thước trên
2. Copy file vào các thư mục tương ứng
3. Đặt tên file: `ic_launcher.png` hoặc `ic_launcher_round.png`
4. Refresh project trong Android Studio (F5)

### Tool Online Tạo Logo (Miễn phí)

- **Android Asset Studio**: https://romannurik.github.io/AndroidAssetStudio/
- **App Icon Generator**: https://appicon.co/
- **Canva**: https://www.canva.com/ (Thiết kế logo)

---

## 🖼️ 2. Hình Ảnh Sản Phẩm

### Đường Dẫn Lưu Trữ

```
app/src/main/assets/images/products/
```

### Quy Tắc Đặt Tên

**Format:** `product_[id].[extension]`

**Ví dụ:**
```
product_1.jpg    -> Sản phẩm ID 1
product_2.png    -> Sản phẩm ID 2
product_3.jpg    -> Sản phẩm ID 3
...
```

### Kích Thước Khuyến Nghị

- **Width**: 500px - 1000px
- **Height**: 500px - 1000px
- **Tỷ lệ**: 1:1 (vuông) hoặc 4:3
- **Format**: JPG (nhỏ gọn) hoặc PNG (chất lượng cao)
- **Dung lượng**: < 500KB mỗi ảnh

### Cách Thêm Hình Ảnh Sản Phẩm

#### Bước 1: Copy hình ảnh vào thư mục

```
1. Chuẩn bị ảnh sản phẩm (đặt tên theo quy tắc)
2. Copy vào: app/src/main/assets/images/products/
3. Ví dụ cấu trúc:
   products/
   ├── product_1.jpg
   ├── product_2.jpg
   ├── product_3.jpg
   └── ...
```

#### Bước 2: Cập nhật Database

Trong `DatabaseHelper.java`, thêm hình ảnh khi insert sản phẩm:

```java
// Ví dụ thêm sản phẩm với hình ảnh
ContentValues values = new ContentValues();
values.put("name", "Arduino Uno R3");
values.put("image_url", "file:///android_asset/images/products/product_1.jpg");
values.put("price", 150000);
values.put("category", "Microcontroller");
// ... các field khác
db.insert("products", null, values);
```

#### Bước 3: Load hình ảnh với Glide (Đã có sẵn trong ProductAdapter)

```java
// Code này đã có trong ProductAdapter.java
Glide.with(context)
    .load(product.getImageUrl())
    .placeholder(R.drawable.ic_product_placeholder)
    .error(R.drawable.ic_product_placeholder)
    .into(holder.ivProduct);
```

---

## 🎪 3. Hình Ảnh Banner

### Đường Dẫn Lưu Trữ

```
app/src/main/assets/images/banners/
```

### Quy Tắc Đặt Tên

**Format:** `banner_[số].[extension]`

**Ví dụ:**
```
banner_1.jpg
banner_2.jpg
banner_3.jpg
```

### Kích Thước Khuyến Nghị

- **Width**: 1200px - 1920px
- **Height**: 400px - 600px
- **Tỷ lệ**: 16:9 hoặc 3:1
- **Format**: JPG
- **Dung lượng**: < 300KB mỗi ảnh

### Cách Thêm Banner

#### Bước 1: Copy banner vào thư mục

```
app/src/main/assets/images/banners/
├── banner_1.jpg
├── banner_2.jpg
└── banner_3.jpg
```

#### Bước 2: Cập nhật BannerAdapter

Trong `MainActivity.java`:

```java
private void setupBanner() {
    List<String> bannerImages = new ArrayList<>();
    
    // Thêm banner từ assets
    bannerImages.add("file:///android_asset/images/banners/banner_1.jpg");
    bannerImages.add("file:///android_asset/images/banners/banner_2.jpg");
    bannerImages.add("file:///android_asset/images/banners/banner_3.jpg");

    BannerAdapter bannerAdapter = new BannerAdapter(bannerImages);
    vpBanner.setAdapter(bannerAdapter);
    
    // Auto scroll...
}
```

Hoặc giữ nguyên drawables hiện tại (banner_1.xml, banner_2.xml, banner_3.xml)

---

## 🔄 4. Cập Nhật Database Với Hình Ảnh

### Script SQL Cập Nhật Hàng Loạt

Tạo method trong `DatabaseHelper.java`:

```java
public void updateProductImages() {
    SQLiteDatabase db = this.getWritableDatabase();
    
    // Cập nhật từng sản phẩm
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_1.jpg' WHERE id = 1");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_2.jpg' WHERE id = 2");
    db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_3.jpg' WHERE id = 3");
    // ... thêm các sản phẩm khác
    
    db.close();
}
```

Gọi method này một lần trong `onCreate()` của MainActivity:

```java
// Chỉ chạy lần đầu hoặc khi cần update
// dbHelper.updateProductImages();
```

---

## 📝 5. Checklist Hoàn Thành

### Logo App
- [ ] Chuẩn bị logo file (PNG, tỷ lệ 1:1)
- [ ] Sử dụng Image Asset Studio tạo icon
- [ ] Kiểm tra logo hiển thị đúng trên Home screen
- [ ] Test logo trên nhiều màn hình khác nhau

### Hình Ảnh Sản Phẩm
- [ ] Chuẩn bị ảnh sản phẩm (500-1000px)
- [ ] Đặt tên file theo quy tắc: `product_[id].jpg`
- [ ] Copy vào: `assets/images/products/`
- [ ] Cập nhật database với đường dẫn ảnh
- [ ] Test hiển thị trong RecyclerView

### Banner
- [ ] Chuẩn bị ảnh banner (1200x400px)
- [ ] Đặt tên: `banner_1.jpg`, `banner_2.jpg`, ...
- [ ] Copy vào: `assets/images/banners/`
- [ ] Cập nhật BannerAdapter
- [ ] Test auto-scroll banner

---

## 💡 Tips & Best Practices

### Tối Ưu Hình Ảnh

**Online Tools:**
- **TinyPNG**: https://tinypng.com/ (Nén PNG/JPG)
- **Squoosh**: https://squoosh.app/ (Google)
- **Compressor.io**: https://compressor.io/

**Quy Tắc:**
- Nén ảnh trước khi thêm vào app
- Sử dụng JPG cho ảnh phức tạp
- Sử dụng PNG cho logo, icon
- Tránh ảnh > 1MB

### Placeholder Image

App đã có sẵn placeholder:
```xml
ic_product_placeholder.xml
ic_avatar_placeholder.xml
```

### Load Ảnh Từ URL (Future)

Nếu muốn load từ internet:

```java
Glide.with(context)
    .load("https://example.com/product.jpg")
    .placeholder(R.drawable.ic_product_placeholder)
    .error(R.drawable.ic_product_placeholder)
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .into(imageView);
```

---

## 🎨 Nguồn Ảnh Miễn Phí

### Ảnh Sản Phẩm Electronics/Arduino
- **Unsplash**: https://unsplash.com/s/photos/electronics
- **Pexels**: https://www.pexels.com/search/arduino/
- **Pixabay**: https://pixabay.com/images/search/microcontroller/

### Tạo Logo Miễn Phí
- **Canva**: https://www.canva.com/
- **LogoMakr**: https://logomakr.com/
- **Hatchful**: https://www.shopify.com/tools/logo-maker

### Icon Miễn Phí
- **Flaticon**: https://www.flaticon.com/
- **Icons8**: https://icons8.com/
- **Material Icons**: https://fonts.google.com/icons

---

## 🚀 Ví Dụ Hoàn Chỉnh

### 1. Thêm Logo App

```bash
# Bước 1: Chuẩn bị logo.png (1024x1024px)
# Bước 2: Android Studio → res → New → Image Asset
# Bước 3: Chọn logo.png → Next → Finish
# Kết quả: Logo tự động tạo cho mọi density
```

### 2. Thêm Sản Phẩm Với Ảnh

```java
// DatabaseHelper.java
private void insertSampleProducts(SQLiteDatabase db) {
    // Arduino Uno
    insertProduct(db, "Arduino Uno R3", 
        "file:///android_asset/images/products/product_1.jpg",
        150000, "Microcontroller", 
        "Board Arduino chính hãng", 50);
    
    // ESP32
    insertProduct(db, "ESP32 DevKit", 
        "file:///android_asset/images/products/product_2.jpg",
        120000, "Microcontroller", 
        "Vi điều khiển có WiFi", 30);
    
    // Sensor
    insertProduct(db, "DHT22 Temperature Sensor", 
        "file:///android_asset/images/products/product_3.jpg",
        85000, "Sensor", 
        "Cảm biến nhiệt độ độ ẩm", 100);
}

private void insertProduct(SQLiteDatabase db, String name, String imageUrl,
                          double price, String category, String desc, int stock) {
    ContentValues values = new ContentValues();
    values.put("name", name);
    values.put("image_url", imageUrl);
    values.put("price", price);
    values.put("category", category);
    values.put("description", desc);
    values.put("stock", stock);
    db.insert("products", null, values);
}
```

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề:
1. **Clean Project**: Build → Clean Project
2. **Rebuild**: Build → Rebuild Project
3. **Invalidate Caches**: File → Invalidate Caches / Restart
4. **Sync Gradle**: File → Sync Project with Gradle Files

---

**Lưu ý quan trọng:**
- Hình ảnh trong `assets/` được access bằng: `file:///android_asset/path/to/image.jpg`
- Hình ảnh trong `res/drawable/` được access bằng: `R.drawable.image_name`
- Glide đã được setup trong project, chỉ cần cung cấp đường dẫn đúng

**Chúc bạn thành công! 🎉**
