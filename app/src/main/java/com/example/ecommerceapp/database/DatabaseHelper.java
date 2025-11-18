package com.example.ecommerceapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.ecommerceapp.models.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ecommerce.db";
    private static final int DATABASE_VERSION = 5; // Increased version for wishlist

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_CART = "cart";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String TABLE_WISHLIST = "wishlist";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "email TEXT, " +
                "password TEXT, " +
                "full_name TEXT, " +
                "phone TEXT, " +
                "address TEXT, " +
                "role TEXT DEFAULT 'user', " +
                "avatar_url TEXT, " +
                "latitude REAL DEFAULT 0, " +
                "longitude REAL DEFAULT 0)";
        db.execSQL(createUsersTable);

        // Create Products table
        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "description TEXT, " +
                "price REAL, " +
                "category TEXT, " +
                "stock INTEGER, " +
                "image_url TEXT)";
        db.execSQL(createProductsTable);

        // Create Cart table
        String createCartTable = "CREATE TABLE " + TABLE_CART + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "product_id INTEGER, " +
                "quantity INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(id), " +
                "FOREIGN KEY(product_id) REFERENCES products(id))";
        db.execSQL(createCartTable);

        // Create Orders table
        String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "order_date TEXT, " +
                "total_amount REAL, " +
                "status TEXT, " +
                "shipping_address TEXT, " +
                "payment_method TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))";
        db.execSQL(createOrdersTable);

        // Create Order Items table
        String createOrderItemsTable = "CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER, " +
                "product_id INTEGER, " +
                "quantity INTEGER, " +
                "price REAL, " +
                "FOREIGN KEY(order_id) REFERENCES orders(id), " +
                "FOREIGN KEY(product_id) REFERENCES products(id))";
        db.execSQL(createOrderItemsTable);

        // Create Wishlist table
        String createWishlistTable = "CREATE TABLE " + TABLE_WISHLIST + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "product_id INTEGER, " +
                "added_date TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id), " +
                "FOREIGN KEY(product_id) REFERENCES products(id))";
        db.execSQL(createWishlistTable);

        // Insert sample products
        insertSampleProducts(db);
        
        // Insert default user
        insertDefaultUser(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WISHLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void insertSampleProducts(SQLiteDatabase db) {
        String[][] products = {
                {"Arduino Uno R3", "Vi điều khiển Arduino Uno R3 chính hãng", "250000", "Vi điều khiển", "50"},
                {"Raspberry Pi 4", "Máy tính nhúng Raspberry Pi 4 Model B 4GB", "1500000", "Máy tính nhúng", "30"},
                {"ESP32 DevKit", "Module ESP32 WiFi Bluetooth", "150000", "Module WiFi", "100"},
                {"Cảm biến DHT22", "Cảm biến nhiệt độ độ ẩm DHT22", "85000", "Cảm biến", "200"},
                {"Servo Motor SG90", "Động cơ Servo SG90 9g", "35000", "Động cơ", "150"},
                {"LED RGB 5mm", "LED RGB 5mm cathode chung", "5000", "LED", "500"},
                {"Màn hình LCD 16x2", "Màn hình LCD 16x2 xanh dương", "65000", "Màn hình", "80"},
                {"Cảm biến siêu âm HC-SR04", "Cảm biến khoảng cách siêu âm", "45000", "Cảm biến", "120"},
                {"Module Relay 4 kênh", "Module Relay 4 kênh 5V", "95000", "Module", "60"},
                {"Breadboard 830", "Breadboard 830 lỗ", "25000", "Linh kiện", "200"},
                {"Jumper Wire", "Dây jumper 40 sợi đực-cái", "15000", "Linh kiện", "300"},
                {"Nguồn 5V 2A", "Adapter nguồn 5V 2A", "55000", "Nguồn", "100"},
                {"STM32 Blue Pill", "Vi điều khiển STM32F103C8T6", "120000", "Vi điều khiển", "70"},
                {"OLED 0.96 inch", "Màn hình OLED 0.96 inch I2C", "75000", "Màn hình", "90"},
                {"Module RFID RC522", "Module đọc thẻ RFID RC522", "65000", "Module", "85"}
        };

        for (String[] product : products) {
            ContentValues values = new ContentValues();
            values.put("name", product[0]);
            values.put("description", product[1]);
            values.put("price", Double.parseDouble(product[2]));
            values.put("category", product[3]);
            values.put("stock", Integer.parseInt(product[4]));
            values.put("image_url", "product_" + product[0].toLowerCase().replace(" ", "_"));
            db.insert(TABLE_PRODUCTS, null, values);
        }
    }

    private void insertDefaultUser(SQLiteDatabase db) {
        // Tạo tài khoản mặc định
        // Username: admin
        // Password: admin123
        ContentValues values = new ContentValues();
        values.put("username", "admin");
        values.put("email", "admin@shop.com");
        values.put("password", "admin123");
        values.put("full_name", "Administrator");
        values.put("phone", "0123456789");
        values.put("address", "123 Đường ABC, Quận 1, TP.HCM");
        values.put("role", "admin");
        db.insert(TABLE_USERS, null, values);
        
        // Tạo tài khoản user thường
        // Username: user
        // Password: user123
        values = new ContentValues();
        values.put("username", "user");
        values.put("email", "user@shop.com");
        values.put("password", "user123");
        values.put("full_name", "Nguyễn Văn A");
        values.put("phone", "0987654321");
        values.put("address", "456 Đường XYZ, Quận 2, TP.HCM");
        values.put("role", "user");
        db.insert(TABLE_USERS, null, values);
    }

    // Update product images - chỉ gọi 1 lần khi cần update
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

    // User operations
    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("email", user.getEmail());
        values.put("password", user.getPassword());
        values.put("full_name", user.getFullName());
        values.put("phone", user.getPhone());
        values.put("address", user.getAddress());
        values.put("role", user.getRole() != null ? user.getRole() : "user");
        return db.insert(TABLE_USERS, null, values);
    }

    public User loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                "username=? AND password=?",
                new String[]{username, password},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
            user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, "id=?",
                new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
            user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
            user.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow("avatar_url")));
            user.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
            user.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", user.getFullName());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("address", user.getAddress());
        values.put("avatar_url", user.getAvatarUrl());
        values.put("latitude", user.getLatitude());
        values.put("longitude", user.getLongitude());
        int rows = db.update(TABLE_USERS, values, "id=?",
                new String[]{String.valueOf(user.getId())});
        return rows > 0;
    }

    // Product operations
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    public Product getProductById(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, "id=?",
                new String[]{String.valueOf(productId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Product product = new Product();
            product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
            product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
            product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
            cursor.close();
            return product;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null,
                "name LIKE ? OR description LIKE ? OR category LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT category FROM " + TABLE_PRODUCTS, null);

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, "category=?",
                new String[]{category}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    // Cart operations
    public long addToCart(int userId, int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if product already in cart
        Cursor cursor = db.query(TABLE_CART, null,
                "user_id=? AND product_id=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            int newQty = currentQty + quantity;

            ContentValues values = new ContentValues();
            values.put("quantity", newQty);
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            cursor.close();

            db.update(TABLE_CART, values, "id=?", new String[]{String.valueOf(id)});
            return id;
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("product_id", productId);
        values.put("quantity", quantity);
        return db.insert(TABLE_CART, null, values);
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> cartItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT c.id, c.user_id, c.product_id, c.quantity, " +
                "p.name, p.price, p.image_url " +
                "FROM " + TABLE_CART + " c " +
                "INNER JOIN " + TABLE_PRODUCTS + " p ON c.product_id = p.id " +
                "WHERE c.user_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                CartItem item = new CartItem();
                item.setId(cursor.getInt(0));
                item.setUserId(cursor.getInt(1));
                item.setProductId(cursor.getInt(2));
                item.setQuantity(cursor.getInt(3));
                item.setProductName(cursor.getString(4));
                item.setProductPrice(cursor.getDouble(5));
                item.setImageUrl(cursor.getString(6));
                cartItems.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cartItems;
    }

    public boolean updateCartItemQuantity(int cartItemId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", quantity);
        int rows = db.update(TABLE_CART, values, "id=?",
                new String[]{String.valueOf(cartItemId)});
        return rows > 0;
    }

    public boolean removeFromCart(int cartItemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_CART, "id=?", new String[]{String.valueOf(cartItemId)});
        return rows > 0;
    }

    public void clearCart(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, "user_id=?", new String[]{String.valueOf(userId)});
    }

    // Order operations
    public long createOrder(Order order, List<CartItem> cartItems) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues orderValues = new ContentValues();
            orderValues.put("user_id", order.getUserId());
            orderValues.put("order_date", order.getOrderDate());
            orderValues.put("total_amount", order.getTotalAmount());
            orderValues.put("status", order.getStatus());
            orderValues.put("shipping_address", order.getShippingAddress());
            orderValues.put("payment_method", order.getPaymentMethod());

            long orderId = db.insert(TABLE_ORDERS, null, orderValues);

            if (orderId != -1) {
                for (CartItem item : cartItems) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put("order_id", orderId);
                    itemValues.put("product_id", item.getProductId());
                    itemValues.put("quantity", item.getQuantity());
                    itemValues.put("price", item.getProductPrice());
                    db.insert(TABLE_ORDER_ITEMS, null, itemValues);

                    // Update product stock
                    Product product = getProductById(item.getProductId());
                    int newStock = product.getStock() - item.getQuantity();
                    ContentValues stockValues = new ContentValues();
                    stockValues.put("stock", newStock);
                    db.update(TABLE_PRODUCTS, stockValues, "id=?",
                            new String[]{String.valueOf(item.getProductId())});
                }

                clearCart(order.getUserId());
                db.setTransactionSuccessful();
            }

            return orderId;
        } finally {
            db.endTransaction();
        }
    }

    public List<Order> getOrderHistory(int userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, "id DESC");

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                order.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow("order_date")));
                order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")));
                order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                order.setShippingAddress(cursor.getString(cursor.getColumnIndexOrThrow("shipping_address")));
                order.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow("payment_method")));
                orders.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public List<Product> getLatestProducts(int limit) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null,
                "id DESC", String.valueOf(limit));

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    public List<Product> getFeaturedProducts(int limit) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, "stock > ?",
                new String[]{"10"}, null, null, "price DESC", String.valueOf(limit));

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    public List<Product> getBestsellerProducts(int limit) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null,
                "RANDOM()", String.valueOf(limit));

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    public int getProductCountByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS + " WHERE category=?",
                new String[]{category});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public Order getOrderById(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null, "id=?",
                new String[]{String.valueOf(orderId)}, null, null, null);

        Order order = null;
        if (cursor.moveToFirst()) {
            order = new Order();
            order.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            order.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow("order_date")));
            order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")));
            order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
            order.setShippingAddress(cursor.getString(cursor.getColumnIndexOrThrow("shipping_address")));
            order.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow("payment_method")));
        }
        cursor.close();
        return order;
    }

    public List<com.example.ecommerceapp.models.OrderItem> getOrderItems(int orderId) {
        List<com.example.ecommerceapp.models.OrderItem> orderItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.price, " +
                       "p.name, p.image_url FROM " + TABLE_ORDER_ITEMS + " oi " +
                       "INNER JOIN " + TABLE_PRODUCTS + " p ON oi.product_id = p.id " +
                       "WHERE oi.order_id = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

        if (cursor.moveToFirst()) {
            do {
                com.example.ecommerceapp.models.OrderItem item = new com.example.ecommerceapp.models.OrderItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                item.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow("order_id")));
                item.setProductId(cursor.getInt(cursor.getColumnIndexOrThrow("product_id")));
                item.setProductName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
                item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                item.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                orderItems.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orderItems;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null, null, null, null, null, "id DESC");

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                order.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow("order_date")));
                order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")));
                order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                order.setShippingAddress(cursor.getString(cursor.getColumnIndexOrThrow("shipping_address")));
                order.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow("payment_method")));
                orders.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, "id ASC");

        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
                user.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow("avatar_url")));
                user.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
                user.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
                users.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    // Add Product
    public long addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("description", product.getDescription());
        values.put("price", product.getPrice());
        values.put("category", product.getCategory());
        values.put("stock", product.getStock());
        values.put("image_url", product.getImageUrl());
        
        long id = db.insert(TABLE_PRODUCTS, null, values);
        return id;
    }

    // Update Product
    public boolean updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("description", product.getDescription());
        values.put("price", product.getPrice());
        values.put("category", product.getCategory());
        values.put("stock", product.getStock());
        values.put("image_url", product.getImageUrl());
        
        int rows = db.update(TABLE_PRODUCTS, values, "id = ?", 
                new String[]{String.valueOf(product.getId())});
        return rows > 0;
    }

    // Delete Product
    public boolean deleteProduct(int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Delete from cart first (foreign key constraint)
        db.delete(TABLE_CART, "product_id = ?", new String[]{String.valueOf(productId)});
        
        // Delete from order_items
        db.delete(TABLE_ORDER_ITEMS, "product_id = ?", new String[]{String.valueOf(productId)});
        
        // Delete product
        int rows = db.delete(TABLE_PRODUCTS, "id = ?", new String[]{String.valueOf(productId)});
        return rows > 0;
    }

    // Delete User
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Delete user's cart items
        db.delete(TABLE_CART, "user_id = ?", new String[]{String.valueOf(userId)});
        
        // Delete user's orders and order items
        Cursor cursor = db.query(TABLE_ORDERS, new String[]{"id"}, "user_id = ?", 
                new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int orderId = cursor.getInt(0);
                db.delete(TABLE_ORDER_ITEMS, "order_id = ?", new String[]{String.valueOf(orderId)});
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        db.delete(TABLE_ORDERS, "user_id = ?", new String[]{String.valueOf(userId)});
        
        // Delete user
        int rows = db.delete(TABLE_USERS, "id = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    // Add Category
    public boolean addCategory(String categoryName) {
        List<String> categories = getAllCategories();
        if (categories.contains(categoryName)) {
            return false; // Category already exists
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", categoryName);
        values.put("description", "");
        values.put("price", 0);
        values.put("category", categoryName);
        values.put("stock", 0);
        values.put("image_url", "");
        
        // Insert a placeholder product to create the category
        long id = db.insert(TABLE_PRODUCTS, null, values);
        
        // Delete the placeholder immediately
        if (id != -1) {
            db.delete(TABLE_PRODUCTS, "id = ?", new String[]{String.valueOf(id)});
        }
        
        return true;
    }

    // Update Category
    public boolean updateCategory(String oldCategory, String newCategory) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category", newCategory);
        
        int rows = db.update(TABLE_PRODUCTS, values, "category = ?", new String[]{oldCategory});
        return rows >= 0; // Return true even if no products updated
    }

    // Delete Category
    public boolean deleteCategory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Delete all products in this category
        db.delete(TABLE_PRODUCTS, "category = ?", new String[]{category});
        
        return true;
    }

    // ==================== WISHLIST OPERATIONS ====================
    
    // Add to Wishlist
    public long addToWishlist(int userId, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Check if already in wishlist
        Cursor cursor = db.query(TABLE_WISHLIST, null,
                "user_id=? AND product_id=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)},
                null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return -1; // Already exists
        }
        if (cursor != null) cursor.close();
        
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("product_id", productId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put("added_date", sdf.format(new Date()));
        
        return db.insert(TABLE_WISHLIST, null, values);
    }
    
    // Remove from Wishlist
    public boolean removeFromWishlist(int userId, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_WISHLIST, "user_id=? AND product_id=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
        return rows > 0;
    }
    
    // Check if product is in wishlist
    public boolean isInWishlist(int userId, int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WISHLIST, null,
                "user_id=? AND product_id=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)},
                null, null, null);
        
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        return exists;
    }
    
    // Get all wishlist items for user
    public List<Product> getWishlistProducts(int userId) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT p.* FROM " + TABLE_PRODUCTS + " p " +
                "INNER JOIN " + TABLE_WISHLIST + " w ON p.id = w.product_id " +
                "WHERE w.user_id = ? " +
                "ORDER BY w.added_date DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }
    
    // Get wishlist count
    public int getWishlistCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WISHLIST + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    // Get cart item count (số lượng sản phẩm, không phải tổng quantity)
    public int getCartItemCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CART + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
