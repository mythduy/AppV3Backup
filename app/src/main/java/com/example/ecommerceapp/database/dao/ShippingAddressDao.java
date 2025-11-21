package com.example.ecommerceapp.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ecommerceapp.models.ShippingAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * ShippingAddressDao - Handles all Shipping Address-related database operations
 */
public class ShippingAddressDao extends BaseDao {
    private static final String TABLE_SHIPPING_ADDRESSES = "shipping_addresses";

    public ShippingAddressDao(SQLiteDatabase database) {
        super(database);
    }

    /**
     * Add new shipping address
     */
    public long add(int userId, String fullName, String phone, 
                    String province, String district, String ward, 
                    String addressDetail, boolean isDefault) {
        // Check if user has any existing addresses
        Cursor cursor = db.query(TABLE_SHIPPING_ADDRESSES, new String[]{"COUNT(*)"}, 
                                "user_id=?", new String[]{String.valueOf(userId)}, 
                                null, null, null);
        boolean hasExistingAddresses = false;
        if (cursor != null && cursor.moveToFirst()) {
            hasExistingAddresses = cursor.getInt(0) > 0;
        }
        closeCursor(cursor);
        
        // If this is the first address, automatically set as default
        if (!hasExistingAddresses) {
            isDefault = true;
        }
        
        // If this is default address, unset all other default addresses
        if (isDefault) {
            ContentValues updateValues = new ContentValues();
            updateValues.put("is_default", 0);
            db.update(TABLE_SHIPPING_ADDRESSES, updateValues, "user_id=?", 
                     new String[]{String.valueOf(userId)});
        }
        
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("full_name", fullName);
        values.put("phone", phone);
        values.put("province", province);
        values.put("district", district);
        values.put("ward", ward);
        values.put("address_detail", addressDetail);
        values.put("is_default", isDefault ? 1 : 0);
        
        return db.insert(TABLE_SHIPPING_ADDRESSES, null, values);
    }

    /**
     * Get all shipping addresses for user
     */
    public List<ShippingAddress> getAll(int userId) {
        List<ShippingAddress> addresses = new ArrayList<>();
        
        Cursor cursor = db.query(TABLE_SHIPPING_ADDRESSES, null, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, "is_default DESC, id DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                addresses.add(extractAddressFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        closeCursor(cursor);
        return addresses;
    }

    /**
     * Get default shipping address for user
     */
    public ShippingAddress getDefault(int userId) {
        // First try to get default address
        Cursor cursor = db.query(TABLE_SHIPPING_ADDRESSES, null, 
                "user_id=? AND is_default=1",
                new String[]{String.valueOf(userId)}, null, null, null);
        
        ShippingAddress address = null;
        if (cursor != null && cursor.moveToFirst()) {
            address = extractAddressFromCursor(cursor);
        }
        closeCursor(cursor);
        
        // If no default address found, get the most recent address
        if (address == null) {
            cursor = db.query(TABLE_SHIPPING_ADDRESSES, null, 
                    "user_id=?",
                    new String[]{String.valueOf(userId)}, null, null, "id DESC", "1");
            
            if (cursor != null && cursor.moveToFirst()) {
                address = extractAddressFromCursor(cursor);
            }
            closeCursor(cursor);
        }
        
        return address;
    }

    /**
     * Get address by ID
     */
    public ShippingAddress getById(int addressId) {
        Cursor cursor = db.query(TABLE_SHIPPING_ADDRESSES, null, "id=?",
                new String[]{String.valueOf(addressId)}, null, null, null);
        
        ShippingAddress address = null;
        if (cursor != null && cursor.moveToFirst()) {
            address = extractAddressFromCursor(cursor);
        }
        closeCursor(cursor);
        return address;
    }

    /**
     * Update shipping address
     */
    public boolean update(int addressId, String fullName, String phone,
                         String province, String district, String ward,
                         String addressDetail, boolean isDefault) {
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("phone", phone);
        values.put("province", province);
        values.put("district", district);
        values.put("ward", ward);
        values.put("address_detail", addressDetail);
        values.put("is_default", isDefault ? 1 : 0);
        
        int rows = db.update(TABLE_SHIPPING_ADDRESSES, values, "id=?",
                            new String[]{String.valueOf(addressId)});
        return rows > 0;
    }

    /**
     * Set address as default
     */
    public boolean setDefault(int userId, int addressId) {
        beginTransaction();
        
        try {
            // Unset all default addresses for this user
            ContentValues updateValues = new ContentValues();
            updateValues.put("is_default", 0);
            db.update(TABLE_SHIPPING_ADDRESSES, updateValues, "user_id=?", 
                     new String[]{String.valueOf(userId)});
            
            // Set the selected address as default
            updateValues.put("is_default", 1);
            int rows = db.update(TABLE_SHIPPING_ADDRESSES, updateValues, "id=?", 
                                new String[]{String.valueOf(addressId)});
            
            setTransactionSuccessful();
            return rows > 0;
        } finally {
            endTransaction();
        }
    }

    /**
     * Delete shipping address
     */
    public boolean delete(int addressId) {
        int rows = db.delete(TABLE_SHIPPING_ADDRESSES, "id=?", 
                            new String[]{String.valueOf(addressId)});
        return rows > 0;
    }

    /**
     * Get address count for user
     */
    public int getCount(int userId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SHIPPING_ADDRESSES + 
                " WHERE user_id = ?", new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        closeCursor(cursor);
        return count;
    }

    /**
     * Extract ShippingAddress from cursor
     */
    private ShippingAddress extractAddressFromCursor(Cursor cursor) {
        ShippingAddress address = new ShippingAddress();
        address.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        address.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        address.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        address.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        address.setProvince(cursor.getString(cursor.getColumnIndexOrThrow("province")));
        address.setDistrict(cursor.getString(cursor.getColumnIndexOrThrow("district")));
        address.setWard(cursor.getString(cursor.getColumnIndexOrThrow("ward")));
        address.setAddressDetail(cursor.getString(cursor.getColumnIndexOrThrow("address_detail")));
        address.setDefault(cursor.getInt(cursor.getColumnIndexOrThrow("is_default")) == 1);
        return address;
    }
}
