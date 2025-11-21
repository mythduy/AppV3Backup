package com.example.ecommerceapp.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ecommerceapp.models.User;

/**
 * UserDao - Handles all User-related database operations
 */
public class UserDao extends BaseDao {
    private static final String TABLE_USERS = "users";

    public UserDao(SQLiteDatabase database) {
        super(database);
    }

    /**
     * Register new user
     */
    public long register(User user) {
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

    /**
     * Login user with username/email and password
     */
    public User login(String usernameOrEmail, String password) {
        android.util.Log.d("DB_LOGIN", "Login attempt - usernameOrEmail: '" + usernameOrEmail + "', password: '" + password + "'");
        
        Cursor cursor = db.query(TABLE_USERS, null,
                "(username=? OR email=?) AND password=?",
                new String[]{usernameOrEmail, usernameOrEmail, password},
                null, null, null);

        android.util.Log.d("DB_LOGIN", "Query result count: " + (cursor != null ? cursor.getCount() : 0));

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = extractUserFromCursor(cursor);
            android.util.Log.d("DB_LOGIN", "✅ Login successful - Found user: " + user.getUsername());
        } else {
            android.util.Log.d("DB_LOGIN", "❌ Login failed - No matching user found");
        }
        
        closeCursor(cursor);
        return user;
    }

    /**
     * Get user by ID
     */
    public User getById(int userId) {
        Cursor cursor = db.query(TABLE_USERS, null, "id=?",
                new String[]{String.valueOf(userId)}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = extractUserFromCursor(cursor);
        }
        closeCursor(cursor);
        return user;
    }

    /**
     * Get user by email
     */
    public User getByEmail(String email) {
        Cursor cursor = db.query(TABLE_USERS, null, "email=?",
                new String[]{email}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = extractUserFromCursor(cursor);
        }
        closeCursor(cursor);
        return user;
    }

    /**
     * Update user information
     */
    public boolean update(User user) {
        ContentValues values = new ContentValues();
        values.put("full_name", user.getFullName());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("address", user.getAddress());
        values.put("avatar_url", user.getAvatarUrl());
        values.put("latitude", user.getLatitude());
        values.put("longitude", user.getLongitude());
        values.put("bio", user.getBio());
        values.put("gender", user.getGender());
        values.put("date_of_birth", user.getDateOfBirth());
        
        int rows = db.update(TABLE_USERS, values, "id=?",
                new String[]{String.valueOf(user.getId())});
        return rows > 0;
    }

    /**
     * Update user password
     */
    public boolean updatePassword(int userId, String newPassword) {
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        
        int rows = db.update(TABLE_USERS, values, "id=?",
                new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    /**
     * Get all users
     */
    public java.util.List<User> getAll() {
        java.util.List<User> users = new java.util.ArrayList<>();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, "id ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(extractUserFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        closeCursor(cursor);
        return users;
    }

    /**
     * Delete user
     */
    public boolean delete(int userId) {
        // Delete user's related data first
        db.delete("cart", "user_id = ?", new String[]{String.valueOf(userId)});
        
        // Delete user's orders and order items
        Cursor cursor = db.query("orders", new String[]{"id"}, "user_id = ?", 
                new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int orderId = cursor.getInt(0);
                db.delete("order_items", "order_id = ?", new String[]{String.valueOf(orderId)});
            } while (cursor.moveToNext());
        }
        closeCursor(cursor);
        
        db.delete("orders", "user_id = ?", new String[]{String.valueOf(userId)});
        db.delete("wishlist", "user_id = ?", new String[]{String.valueOf(userId)});
        db.delete("reviews", "user_id = ?", new String[]{String.valueOf(userId)});
        db.delete("shipping_addresses", "user_id = ?", new String[]{String.valueOf(userId)});
        
        // Delete user
        int rows = db.delete(TABLE_USERS, "id = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    /**
     * Extract User object from cursor
     */
    private User extractUserFromCursor(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
        
        // Optional fields
        int avatarIndex = cursor.getColumnIndex("avatar_url");
        if (avatarIndex >= 0) user.setAvatarUrl(cursor.getString(avatarIndex));
        
        int latIndex = cursor.getColumnIndex("latitude");
        if (latIndex >= 0) user.setLatitude(cursor.getDouble(latIndex));
        
        int lonIndex = cursor.getColumnIndex("longitude");
        if (lonIndex >= 0) user.setLongitude(cursor.getDouble(lonIndex));
        
        int bioIndex = cursor.getColumnIndex("bio");
        if (bioIndex >= 0) user.setBio(cursor.getString(bioIndex));
        
        int genderIndex = cursor.getColumnIndex("gender");
        if (genderIndex >= 0) user.setGender(cursor.getString(genderIndex));
        
        int dobIndex = cursor.getColumnIndex("date_of_birth");
        if (dobIndex >= 0) user.setDateOfBirth(cursor.getString(dobIndex));
        
        return user;
    }
}
