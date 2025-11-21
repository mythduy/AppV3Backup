package com.example.ecommerceapp.database.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Base Data Access Object
 * Provides common database operations for all DAOs
 */
public abstract class BaseDao {
    protected SQLiteDatabase db;

    public BaseDao(SQLiteDatabase database) {
        this.db = database;
    }

    /**
     * Safely close cursor to prevent memory leaks
     */
    protected void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    /**
     * Check if string is null or empty
     */
    protected boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Begin database transaction
     */
    protected void beginTransaction() {
        db.beginTransaction();
    }

    /**
     * Set transaction as successful
     */
    protected void setTransactionSuccessful() {
        db.setTransactionSuccessful();
    }

    /**
     * End database transaction
     */
    protected void endTransaction() {
        db.endTransaction();
    }
}
