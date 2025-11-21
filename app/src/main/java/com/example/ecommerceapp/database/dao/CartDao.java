package com.example.ecommerceapp.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ecommerceapp.models.CartItem;
import java.util.ArrayList;
import java.util.List;

/**
 * CartDao - Handles all Cart-related database operations
 */
public class CartDao extends BaseDao {
    private static final String TABLE_CART = "cart";
    private static final String TABLE_PRODUCTS = "products";

    public CartDao(SQLiteDatabase database) {
        super(database);
    }

    /**
     * Add item to cart
     */
    public long addToCart(int userId, int productId, int quantity) {
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
            closeCursor(cursor);

            db.update(TABLE_CART, values, "id=?", new String[]{String.valueOf(id)});
            return id;
        }
        closeCursor(cursor);

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("product_id", productId);
        values.put("quantity", quantity);
        return db.insert(TABLE_CART, null, values);
    }

    /**
     * Get all cart items for user
     */
    public List<CartItem> getCartItems(int userId) {
        List<CartItem> cartItems = new ArrayList<>();

        String query = "SELECT c.id, c.user_id, c.product_id, c.quantity, " +
                "p.name, p.price, p.image_url, p.discount " +
                "FROM " + TABLE_CART + " c " +
                "INNER JOIN " + TABLE_PRODUCTS + " p ON c.product_id = p.id " +
                "WHERE c.user_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                CartItem item = new CartItem();
                item.setId(cursor.getInt(0));
                item.setUserId(cursor.getInt(1));
                item.setProductId(cursor.getInt(2));
                item.setQuantity(cursor.getInt(3));
                item.setProductName(cursor.getString(4));
                
                // Calculate final price with discount
                double price = cursor.getDouble(5);
                double discount = cursor.getDouble(7);
                double finalPrice = price * (1 - discount / 100);
                item.setProductPrice(finalPrice);
                
                item.setImageUrl(cursor.getString(6));
                cartItems.add(item);
            } while (cursor.moveToNext());
        }
        closeCursor(cursor);
        return cartItems;
    }

    /**
     * Get cart item by ID
     */
    public CartItem getById(int cartItemId) {
        String query = "SELECT c.id, c.user_id, c.product_id, c.quantity, " +
                "p.name, p.price, p.image_url, p.discount " +
                "FROM " + TABLE_CART + " c " +
                "INNER JOIN " + TABLE_PRODUCTS + " p ON c.product_id = p.id " +
                "WHERE c.id = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(cartItemId)});

        CartItem item = null;
        if (cursor != null && cursor.moveToFirst()) {
            item = new CartItem();
            item.setId(cursor.getInt(0));
            item.setUserId(cursor.getInt(1));
            item.setProductId(cursor.getInt(2));
            item.setQuantity(cursor.getInt(3));
            item.setProductName(cursor.getString(4));
            
            // Calculate final price with discount
            double price = cursor.getDouble(5);
            double discount = cursor.getDouble(7);
            double finalPrice = price * (1 - discount / 100);
            item.setProductPrice(finalPrice);
            
            item.setImageUrl(cursor.getString(6));
        }
        closeCursor(cursor);
        return item;
    }

    /**
     * Update cart item quantity
     */
    public boolean updateQuantity(int cartItemId, int quantity) {
        ContentValues values = new ContentValues();
        values.put("quantity", quantity);
        int rows = db.update(TABLE_CART, values, "id=?",
                new String[]{String.valueOf(cartItemId)});
        return rows > 0;
    }

    /**
     * Remove item from cart
     */
    public boolean remove(int cartItemId) {
        int rows = db.delete(TABLE_CART, "id=?", new String[]{String.valueOf(cartItemId)});
        return rows > 0;
    }

    /**
     * Clear all cart items for user
     */
    public void clear(int userId) {
        db.delete(TABLE_CART, "user_id=?", new String[]{String.valueOf(userId)});
    }

    /**
     * Get cart item count for user
     */
    public int getItemCount(int userId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CART + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        closeCursor(cursor);
        return count;
    }
}
