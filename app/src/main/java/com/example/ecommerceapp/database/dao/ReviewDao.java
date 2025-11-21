package com.example.ecommerceapp.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ecommerceapp.models.Review;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ReviewDao - Handles all Review-related database operations
 */
public class ReviewDao extends BaseDao {
    private static final String TABLE_REVIEWS = "reviews";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ORDER_ITEMS = "order_items";

    public ReviewDao(SQLiteDatabase database) {
        super(database);
    }

    /**
     * Add review for product
     */
    public long add(int productId, int userId, float rating, String comment) {
        ContentValues values = new ContentValues();
        values.put("product_id", productId);
        values.put("user_id", userId);
        values.put("rating", rating);
        values.put("comment", comment);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        values.put("review_date", sdf.format(new Date()));
        
        long result = db.insert(TABLE_REVIEWS, null, values);
        
        // Update product average rating
        if (result != -1) {
            updateProductAverageRating(productId);
        }
        
        return result;
    }

    /**
     * Get all reviews for product
     */
    public List<Review> getProductReviews(int productId) {
        List<Review> reviews = new ArrayList<>();
        
        String query = "SELECT r.*, u.full_name, COALESCE(u.avatar_url, '') as avatar_url " +
                      "FROM " + TABLE_REVIEWS + " r " +
                      "INNER JOIN " + TABLE_USERS + " u ON r.user_id = u.id " +
                      "WHERE r.product_id = ? " +
                      "ORDER BY r.id DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(productId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Review review = new Review();
                review.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                review.setProductId(cursor.getInt(cursor.getColumnIndexOrThrow("product_id")));
                review.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                review.setRating(cursor.getFloat(cursor.getColumnIndexOrThrow("rating")));
                review.setComment(cursor.getString(cursor.getColumnIndexOrThrow("comment")));
                review.setReviewDate(cursor.getString(cursor.getColumnIndexOrThrow("review_date")));
                review.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
                
                // Get avatar with proper null handling
                int avatarIndex = cursor.getColumnIndex("avatar_url");
                if (avatarIndex >= 0) {
                    String avatarUrl = cursor.getString(avatarIndex);
                    review.setUserAvatar(avatarUrl != null && !avatarUrl.isEmpty() ? avatarUrl : null);
                }
                
                reviews.add(review);
            } while (cursor.moveToNext());
        }
        closeCursor(cursor);
        return reviews;
    }

    /**
     * Check if user has reviewed product
     */
    public boolean hasUserReviewed(int userId, int productId) {
        Cursor cursor = db.query(TABLE_REVIEWS, null,
                "user_id=? AND product_id=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)},
                null, null, null);
        
        boolean hasReviewed = cursor != null && cursor.getCount() > 0;
        closeCursor(cursor);
        return hasReviewed;
    }

    /**
     * Check if user has purchased product
     */
    public boolean hasUserPurchased(int userId, int productId) {
        String query = "SELECT COUNT(*) FROM " + TABLE_ORDER_ITEMS + " oi " +
                      "INNER JOIN " + TABLE_ORDERS + " o ON oi.order_id = o.id " +
                      "WHERE o.user_id = ? AND oi.product_id = ? AND o.status = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{
            String.valueOf(userId), 
            String.valueOf(productId),
            "completed"
        });
        
        boolean hasPurchased = false;
        if (cursor != null && cursor.moveToFirst()) {
            hasPurchased = cursor.getInt(0) > 0;
        }
        closeCursor(cursor);
        return hasPurchased;
    }

    /**
     * Get review count for product
     */
    public int getReviewCount(int productId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REVIEWS + " WHERE product_id = ?",
                new String[]{String.valueOf(productId)});
        
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        closeCursor(cursor);
        return count;
    }

    /**
     * Update product average rating
     */
    public void updateProductAverageRating(int productId) {
        String query = "SELECT AVG(rating) FROM " + TABLE_REVIEWS + " WHERE product_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(productId)});
        
        ContentValues values = new ContentValues();
        if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
            double avgRating = cursor.getDouble(0);
            values.put("rating", avgRating);
        } else {
            values.put("rating", 0.0);
        }
        db.update(TABLE_PRODUCTS, values, "id=?", new String[]{String.valueOf(productId)});
        closeCursor(cursor);
    }

    /**
     * Fix ratings for all products
     */
    public void fixAllProductRatings() {
        // Update all products: set rating = 0 if they have no reviews
        String query = "UPDATE " + TABLE_PRODUCTS + " " +
                      "SET rating = 0.0 " +
                      "WHERE id NOT IN (SELECT DISTINCT product_id FROM " + TABLE_REVIEWS + ")";
        db.execSQL(query);
        
        // Recalculate rating for products that have reviews
        Cursor cursor = db.rawQuery("SELECT DISTINCT product_id FROM " + TABLE_REVIEWS, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int productId = cursor.getInt(0);
                updateProductAverageRating(productId);
            }
        }
        closeCursor(cursor);
    }

    /**
     * Delete review
     */
    public boolean delete(int reviewId) {
        int rows = db.delete(TABLE_REVIEWS, "id=?", new String[]{String.valueOf(reviewId)});
        return rows > 0;
    }
}
