package com.example.ecommerceapp.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ecommerceapp.models.Product;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * WishlistDao - Handles all Wishlist-related database operations
 */
public class WishlistDao extends BaseDao {
    private static final String TABLE_WISHLIST = "wishlist";
    private static final String TABLE_PRODUCTS = "products";

    public WishlistDao(SQLiteDatabase database) {
        super(database);
    }

    /**
     * Add product to wishlist
     */
    public long add(int userId, int productId) {
        // Check if already in wishlist
        if (isInWishlist(userId, productId)) {
            return -1; // Already exists
        }
        
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("product_id", productId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put("added_date", sdf.format(new Date()));
        
        return db.insert(TABLE_WISHLIST, null, values);
    }

    /**
     * Remove product from wishlist
     */
    public boolean remove(int userId, int productId) {
        int rows = db.delete(TABLE_WISHLIST, "user_id=? AND product_id=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
        return rows > 0;
    }

    /**
     * Check if product is in wishlist
     */
    public boolean isInWishlist(int userId, int productId) {
        Cursor cursor = db.query(TABLE_WISHLIST, null,
                "user_id=? AND product_id=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)},
                null, null, null);
        
        boolean exists = cursor != null && cursor.moveToFirst();
        closeCursor(cursor);
        return exists;
    }

    /**
     * Get all wishlist products for user
     */
    public List<Product> getWishlistProducts(int userId) {
        String query = "SELECT p.* FROM " + TABLE_PRODUCTS + " p " +
                "INNER JOIN " + TABLE_WISHLIST + " w ON p.id = w.product_id " +
                "WHERE w.user_id = ? " +
                "ORDER BY w.added_date DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        return extractProductsFromCursor(cursor);
    }

    /**
     * Get wishlist count for user
     */
    public int getCount(int userId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WISHLIST + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        closeCursor(cursor);
        return count;
    }

    /**
     * Clear all wishlist items for user
     */
    public void clear(int userId) {
        db.delete(TABLE_WISHLIST, "user_id=?", new String[]{String.valueOf(userId)});
    }

    /**
     * Extract products from cursor
     */
    private List<Product> extractProductsFromCursor(Cursor cursor) {
        List<Product> products = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                product.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow("rating")));
                product.setSku(cursor.getString(cursor.getColumnIndexOrThrow("sku")));
                product.setWarranty(cursor.getString(cursor.getColumnIndexOrThrow("warranty")));
                product.setDiscount(cursor.getDouble(cursor.getColumnIndexOrThrow("discount")));
                product.setNew(cursor.getInt(cursor.getColumnIndexOrThrow("is_new")) == 1);
                product.setHot(cursor.getInt(cursor.getColumnIndexOrThrow("is_hot")) == 1);
                product.setFeatured(cursor.getInt(cursor.getColumnIndexOrThrow("is_featured")) == 1);
                products.add(product);
            } while (cursor.moveToNext());
        }
        closeCursor(cursor);
        return products;
    }
}
