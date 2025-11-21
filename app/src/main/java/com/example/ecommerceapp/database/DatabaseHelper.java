package com.example.ecommerceapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.ecommerceapp.database.dao.*;
import com.example.ecommerceapp.models.*;
import java.util.List;

/**
 * DatabaseHelper - Main database class with DAO pattern
 * Refactored to use Data Access Objects (DAO) for better code organization
 * 
 * Usage:
 *   DatabaseHelper db = new DatabaseHelper(context);
 *   db.getUserDao().login(username, password);
 *   db.getProductDao().getAll();
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ecommerce.db";
    private static final int DATABASE_VERSION = 9;

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_CART = "cart";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String TABLE_WISHLIST = "wishlist";
    private static final String TABLE_REVIEWS = "reviews";
    private static final String TABLE_SHIPPING_ADDRESSES = "shipping_addresses";
    
    private Context context;
    
    // DAO instances (lazy initialization)
    private UserDao userDao;
    private ProductDao productDao;
    private CartDao cartDao;
    private OrderDao orderDao;
    private WishlistDao wishlistDao;
    private ReviewDao reviewDao;
    private ShippingAddressDao shippingAddressDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // ==================== DAO GETTERS ====================
    
    public UserDao getUserDao() {
        if (userDao == null) {
            userDao = new UserDao(getWritableDatabase());
        }
        return userDao;
    }
    
    public ProductDao getProductDao() {
        if (productDao == null) {
            productDao = new ProductDao(getWritableDatabase());
        }
        return productDao;
    }
    
    public CartDao getCartDao() {
        if (cartDao == null) {
            cartDao = new CartDao(getWritableDatabase());
        }
        return cartDao;
    }
    
    public OrderDao getOrderDao() {
        if (orderDao == null) {
            orderDao = new OrderDao(getWritableDatabase());
        }
        return orderDao;
    }
    
    public WishlistDao getWishlistDao() {
        if (wishlistDao == null) {
            wishlistDao = new WishlistDao(getWritableDatabase());
        }
        return wishlistDao;
    }
    
    public ReviewDao getReviewDao() {
        if (reviewDao == null) {
            reviewDao = new ReviewDao(getWritableDatabase());
        }
        return reviewDao;
    }
    
    public ShippingAddressDao getShippingAddressDao() {
        if (shippingAddressDao == null) {
            shippingAddressDao = new ShippingAddressDao(getWritableDatabase());
        }
        return shippingAddressDao;
    }

    // ==================== DATABASE SETUP ====================

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        insertSampleProducts(db);
        insertDefaultUser(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 9) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN bio TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN gender TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN date_of_birth TEXT");
        }
        
        if (oldVersion < 8) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHIPPING_ADDRESSES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WISHLIST);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }

    private void createTables(SQLiteDatabase db) {
        // Users table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
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
                "longitude REAL DEFAULT 0, " +
                "bio TEXT, " +
                "gender TEXT, " +
                "date_of_birth TEXT)");

        // Products table
        db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "description TEXT, " +
                "price REAL, " +
                "category TEXT, " +
                "stock INTEGER, " +
                "image_url TEXT, " +
                "rating REAL DEFAULT 0.0, " +
                "sku TEXT, " +
                "warranty TEXT DEFAULT '12 tháng', " +
                "discount REAL DEFAULT 0, " +
                "is_new INTEGER DEFAULT 0, " +
                "is_hot INTEGER DEFAULT 0, " +
                "is_featured INTEGER DEFAULT 0)");

        // Cart table
        db.execSQL("CREATE TABLE " + TABLE_CART + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "product_id INTEGER, " +
                "quantity INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(id), " +
                "FOREIGN KEY(product_id) REFERENCES products(id))");

        // Orders table
        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "order_date TEXT, " +
                "total_amount REAL, " +
                "status TEXT DEFAULT 'pending', " +
                "shipping_address TEXT, " +
                "payment_method TEXT, " +
                "shipper_name TEXT, " +
                "shipper_phone TEXT, " +
                "tracking_code TEXT, " +
                "cancelled_reason TEXT, " +
                "admin_notes TEXT, " +
                "confirmed_at TEXT, " +
                "shipped_at TEXT, " +
                "completed_at TEXT, " +
                "cancelled_at TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        // Order Items table
        db.execSQL("CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER, " +
                "product_id INTEGER, " +
                "quantity INTEGER, " +
                "price REAL, " +
                "FOREIGN KEY(order_id) REFERENCES orders(id), " +
                "FOREIGN KEY(product_id) REFERENCES products(id))");

        // Wishlist table
        db.execSQL("CREATE TABLE " + TABLE_WISHLIST + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "product_id INTEGER, " +
                "added_date TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id), " +
                "FOREIGN KEY(product_id) REFERENCES products(id))");

        // Reviews table
        db.execSQL("CREATE TABLE " + TABLE_REVIEWS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER, " +
                "user_id INTEGER, " +
                "rating REAL, " +
                "comment TEXT, " +
                "review_date TEXT, " +
                "FOREIGN KEY(product_id) REFERENCES products(id), " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        // Shipping Addresses table
        db.execSQL("CREATE TABLE " + TABLE_SHIPPING_ADDRESSES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "full_name TEXT, " +
                "phone TEXT, " +
                "province TEXT, " +
                "district TEXT, " +
                "ward TEXT, " +
                "address_detail TEXT, " +
                "is_default INTEGER DEFAULT 0, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");
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
        ContentValues values = new ContentValues();
        values.put("username", "admin");
        values.put("email", "admin@shop.com");
        values.put("password", "admin123");
        values.put("full_name", "Administrator");
        values.put("phone", "0123456789");
        values.put("address", "123 Đường ABC, Quận 1, TP.HCM");
        values.put("role", "admin");
        db.insert(TABLE_USERS, null, values);
        
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

    // ==================== BACKWARD COMPATIBILITY METHODS ====================
    // Legacy methods that delegate to DAOs - for existing code compatibility
    
    public long registerUser(User user) { return getUserDao().register(user); }
    public User loginUser(String u, String p) { return getUserDao().login(u, p); }
    public User getUserById(int id) { return getUserDao().getById(id); }
    public User getUserByEmail(String email) { return getUserDao().getByEmail(email); }
    public boolean updateUser(User user) { return getUserDao().update(user); }
    public boolean updateUserPassword(int id, String p) { return getUserDao().updatePassword(id, p); }
    public List<User> getAllUsers() { return getUserDao().getAll(); }
    public boolean deleteUser(int id) { return getUserDao().delete(id); }
    
    public List<Product> getAllProducts() { return getProductDao().getAll(); }
    public Product getProductById(int id) { return getProductDao().getById(id); }
    public List<Product> searchProducts(String q) { return getProductDao().search(q); }
    public List<String> getAllCategories() { return getProductDao().getAllCategories(); }
    public List<Product> getProductsByCategory(String c) { return getProductDao().getByCategory(c); }
    public List<Product> getLatestProducts(int limit) { return getProductDao().getLatest(limit); }
    public List<Product> getFeaturedProducts(int limit) { return getProductDao().getFeatured(limit); }
    public List<Product> getBestsellerProducts(int limit) { return getProductDao().getBestsellers(limit); }
    public int getProductCountByCategory(String c) { return getProductDao().getCountByCategory(c); }
    public List<Product> getFilteredProducts(String c, String pr, double r, boolean d, boolean h, boolean n, boolean s, String sort) {
        return getProductDao().getFiltered(c, pr, r, d, h, n, s, sort);
    }
    public long addProduct(Product p) { return getProductDao().add(p); }
    public boolean updateProduct(Product p) { return getProductDao().update(p); }
    public boolean deleteProduct(int id) { return getProductDao().delete(id); }
    public int getProductSoldCount(int id) { return getProductDao().getSoldCount(id); }
    
    public long addToCart(int u, int p, int q) { return getCartDao().addToCart(u, p, q); }
    public List<CartItem> getCartItems(int u) { return getCartDao().getCartItems(u); }
    public CartItem getCartItemById(int id) { return getCartDao().getById(id); }
    public boolean updateCartItemQuantity(int id, int q) { return getCartDao().updateQuantity(id, q); }
    public boolean removeFromCart(int id) { return getCartDao().remove(id); }
    public void clearCart(int u) { getCartDao().clear(u); }
    public int getCartItemCount(int u) { return getCartDao().getItemCount(u); }
    
    public long createOrder(Order o, List<CartItem> items) { return getOrderDao().create(o, items, getProductDao()); }
    public Order getOrderById(int id) { return getOrderDao().getById(id); }
    public List<Order> getOrderHistory(int u) { return getOrderDao().getOrderHistory(u); }
    public List<Order> getAllOrders() { return getOrderDao().getAll(); }
    public List<Order> getOrdersByStatus(String s) { return getOrderDao().getByStatus(s); }
    public List<OrderItem> getOrderItems(int id) { return getOrderDao().getOrderItems(id); }
    public boolean updateOrderStatus(int id, String s) { return getOrderDao().updateStatus(id, s); }
    public boolean updateOrderShippingInfo(int id, String n, String p, String t) { return getOrderDao().updateShippingInfo(id, n, p, t); }
    public boolean cancelOrder(int id, String r) { return getOrderDao().cancel(id, r); }
    public boolean updateAdminNotes(int id, String n) { return getOrderDao().updateAdminNotes(id, n); }
    public int getOrderCountByStatus(String s) { return getOrderDao().getCountByStatus(s); }
    public double getTotalRevenue() { return getOrderDao().getTotalRevenue(); }
    public double getTodayRevenue() { return getOrderDao().getTodayRevenue(); }
    
    public long addToWishlist(int u, int p) { return getWishlistDao().add(u, p); }
    public boolean removeFromWishlist(int u, int p) { return getWishlistDao().remove(u, p); }
    public boolean isInWishlist(int u, int p) { return getWishlistDao().isInWishlist(u, p); }
    public List<Product> getWishlistProducts(int u) { return getWishlistDao().getWishlistProducts(u); }
    public int getWishlistCount(int u) { return getWishlistDao().getCount(u); }
    
    public long addReview(int p, int u, float r, String c) { return getReviewDao().add(p, u, r, c); }
    public List<Review> getProductReviews(int p) { return getReviewDao().getProductReviews(p); }
    public boolean hasUserReviewedProduct(int u, int p) { return getReviewDao().hasUserReviewed(u, p); }
    public boolean hasUserPurchasedProduct(int u, int p) { return getReviewDao().hasUserPurchased(u, p); }
    public int getReviewCount(int p) { return getReviewDao().getReviewCount(p); }
    public void fixProductRatings() { getReviewDao().fixAllProductRatings(); }
    
    public long addShippingAddress(int u, String n, String p, String pr, String d, String w, String a, boolean def) {
        return getShippingAddressDao().add(u, n, p, pr, d, w, a, def);
    }
    public List<ShippingAddress> getShippingAddresses(int u) { return getShippingAddressDao().getAll(u); }
    public ShippingAddress getDefaultShippingAddress(int u) { return getShippingAddressDao().getDefault(u); }
    public boolean updateShippingAddress(int id, String n, String p, String pr, String d, String w, String a, boolean def) {
        return getShippingAddressDao().update(id, n, p, pr, d, w, a, def);
    }
    public boolean setDefaultAddress(int u, int id) { return getShippingAddressDao().setDefault(u, id); }
    public boolean deleteShippingAddress(int id) { return getShippingAddressDao().delete(id); }
    
    // Utility methods
    public void updateProductImages() {
        SQLiteDatabase db = getWritableDatabase();
        for (int i = 1; i <= 15; i++) {
            db.execSQL("UPDATE products SET image_url = 'file:///android_asset/images/products/product_" + i + ".jpg' WHERE id = " + i);
        }
        db.close();
    }
    
    public void migrateProductImagesFromAssets() {
        try {
            String[] assetImages = context.getAssets().list("images/products");
            if (assetImages == null || assetImages.length == 0) return;
            
            java.io.File directory = new java.io.File(context.getFilesDir(), "product_images");
            if (!directory.exists()) directory.mkdirs();
            
            List<Product> products = getAllProducts();
            for (Product product : products) {
                String currentImage = product.getImageUrl();
                if (currentImage != null && !currentImage.isEmpty() && 
                    !currentImage.startsWith("http") && 
                    new java.io.File(currentImage).exists()) {
                    continue;
                }
                
                String assetFileName = "product_" + product.getId() + ".jpg";
                boolean found = false;
                
                for (String asset : assetImages) {
                    if (asset.equals(assetFileName)) {
                        found = true;
                        break;
                    }
                }
                
                if (found) {
                    java.io.InputStream is = context.getAssets().open("images/products/" + assetFileName);
                    String newFileName = "product_" + product.getId() + "_" + System.currentTimeMillis() + ".jpg";
                    java.io.File outFile = new java.io.File(directory, newFileName);
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile);
                    
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    
                    fos.close();
                    is.close();
                    
                    product.setImageUrl(outFile.getAbsolutePath());
                    updateProduct(product);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error migrating product images", e);
        }
    }
    
    // Category operations
    public boolean addCategory(String categoryName) {
        if (getAllCategories().contains(categoryName)) return false;
        
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", categoryName);
        values.put("description", "");
        values.put("price", 0);
        values.put("category", categoryName);
        values.put("stock", 0);
        values.put("image_url", "");
        
        long id = db.insert(TABLE_PRODUCTS, null, values);
        if (id != -1) {
            db.delete(TABLE_PRODUCTS, "id = ?", new String[]{String.valueOf(id)});
        }
        return true;
    }
    
    public boolean updateCategory(String oldCategory, String newCategory) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category", newCategory);
        db.update(TABLE_PRODUCTS, values, "category = ?", new String[]{oldCategory});
        return true;
    }
    
    public boolean deleteCategory(String category) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PRODUCTS, "category = ?", new String[]{category});
        return true;
    }
}
