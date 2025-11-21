package com.example.ecommerceapp.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ecommerceapp.models.CartItem;
import com.example.ecommerceapp.models.Order;
import com.example.ecommerceapp.models.OrderItem;
import com.example.ecommerceapp.models.Product;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * OrderDao - Handles all Order-related database operations
 */
public class OrderDao extends BaseDao {
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String TABLE_PRODUCTS = "products";

    public OrderDao(SQLiteDatabase database) {
        super(database);
    }

    /**
     * Create new order with items
     */
    public long create(Order order, List<CartItem> cartItems, ProductDao productDao) {
        beginTransaction();

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
                    // Check stock before creating order item
                    Product product = productDao.getById(item.getProductId());
                    if (product.getStock() < item.getQuantity()) {
                        // Insufficient stock - rollback transaction
                        return -1;
                    }
                    
                    ContentValues itemValues = new ContentValues();
                    itemValues.put("order_id", orderId);
                    itemValues.put("product_id", item.getProductId());
                    itemValues.put("quantity", item.getQuantity());
                    itemValues.put("price", item.getProductPrice());
                    db.insert(TABLE_ORDER_ITEMS, null, itemValues);

                    // Update product stock
                    int newStock = Math.max(0, product.getStock() - item.getQuantity());
                    productDao.updateStock(item.getProductId(), newStock);
                }

                setTransactionSuccessful();
            }

            return orderId;
        } finally {
            endTransaction();
        }
    }

    /**
     * Get order by ID
     */
    public Order getById(int orderId) {
        Cursor cursor = db.query(TABLE_ORDERS, null, "id=?",
                new String[]{String.valueOf(orderId)}, null, null, null);

        Order order = null;
        if (cursor != null && cursor.moveToFirst()) {
            order = extractOrderFromCursor(cursor);
        }
        closeCursor(cursor);
        return order;
    }

    /**
     * Get order history for user
     */
    public List<Order> getOrderHistory(int userId) {
        List<Order> orders = new ArrayList<>();
        Cursor cursor = db.query(TABLE_ORDERS, null, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, "id DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                orders.add(extractOrderFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        closeCursor(cursor);
        return orders;
    }

    /**
     * Get all orders
     */
    public List<Order> getAll() {
        List<Order> orders = new ArrayList<>();
        Cursor cursor = db.query(TABLE_ORDERS, null, null, null, null, null, "id DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                orders.add(extractOrderFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        closeCursor(cursor);
        return orders;
    }

    /**
     * Get orders by status
     */
    public List<Order> getByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        Cursor cursor = db.query(TABLE_ORDERS, null, "status=?", 
                new String[]{status}, null, null, "id DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                orders.add(extractOrderFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        closeCursor(cursor);
        return orders;
    }

    /**
     * Get order items for an order
     */
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        String query = "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.price, " +
                       "p.name, p.image_url FROM " + TABLE_ORDER_ITEMS + " oi " +
                       "INNER JOIN " + TABLE_PRODUCTS + " p ON oi.product_id = p.id " +
                       "WHERE oi.order_id = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                OrderItem item = new OrderItem();
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
        closeCursor(cursor);
        return orderItems;
    }

    /**
     * Update order status
     */
    public boolean updateStatus(int orderId, String newStatus) {
        ContentValues values = new ContentValues();
        values.put("status", newStatus);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        
        // Add timestamp based on status
        switch (newStatus) {
            case "confirmed":
                values.put("confirmed_at", currentTime);
                break;
            case "shipping":
                values.put("shipped_at", currentTime);
                break;
            case "completed":
                values.put("completed_at", currentTime);
                break;
            case "cancelled":
                values.put("cancelled_at", currentTime);
                break;
        }
        
        int rows = db.update(TABLE_ORDERS, values, "id=?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    /**
     * Update shipping info
     */
    public boolean updateShippingInfo(int orderId, String shipperName, String shipperPhone, String trackingCode) {
        ContentValues values = new ContentValues();
        values.put("shipper_name", shipperName);
        values.put("shipper_phone", shipperPhone);
        values.put("tracking_code", trackingCode);
        
        int rows = db.update(TABLE_ORDERS, values, "id=?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    /**
     * Cancel order
     */
    public boolean cancel(int orderId, String reason) {
        ContentValues values = new ContentValues();
        values.put("status", "cancelled");
        values.put("cancelled_reason", reason);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put("cancelled_at", sdf.format(new Date()));
        
        int rows = db.update(TABLE_ORDERS, values, "id=?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    /**
     * Update admin notes
     */
    public boolean updateAdminNotes(int orderId, String notes) {
        ContentValues values = new ContentValues();
        values.put("admin_notes", notes);
        
        int rows = db.update(TABLE_ORDERS, values, "id=?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    /**
     * Get order count by status
     */
    public int getCountByStatus(String status) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE status=?", 
                new String[]{status});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        closeCursor(cursor);
        return count;
    }

    /**
     * Get total revenue
     */
    public double getTotalRevenue() {
        Cursor cursor = db.rawQuery("SELECT SUM(total_amount) FROM " + TABLE_ORDERS + 
                " WHERE status=?", new String[]{"completed"});
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        closeCursor(cursor);
        return total;
    }

    /**
     * Get today's revenue
     */
    public double getTodayRevenue() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        
        Cursor cursor = db.rawQuery("SELECT SUM(total_amount) FROM " + TABLE_ORDERS + 
                " WHERE status=? AND DATE(order_date)=?", 
                new String[]{"completed", today});
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        closeCursor(cursor);
        return total;
    }

    /**
     * Extract Order from cursor
     */
    private Order extractOrderFromCursor(Cursor cursor) {
        Order order = new Order();
        order.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        order.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow("order_date")));
        order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        order.setShippingAddress(cursor.getString(cursor.getColumnIndexOrThrow("shipping_address")));
        order.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow("payment_method")));
        
        // Extended fields (optional)
        int shipperNameIndex = cursor.getColumnIndex("shipper_name");
        if (shipperNameIndex >= 0) order.setShipperName(cursor.getString(shipperNameIndex));
        
        int shipperPhoneIndex = cursor.getColumnIndex("shipper_phone");
        if (shipperPhoneIndex >= 0) order.setShipperPhone(cursor.getString(shipperPhoneIndex));
        
        int trackingCodeIndex = cursor.getColumnIndex("tracking_code");
        if (trackingCodeIndex >= 0) order.setTrackingCode(cursor.getString(trackingCodeIndex));
        
        int cancelledReasonIndex = cursor.getColumnIndex("cancelled_reason");
        if (cancelledReasonIndex >= 0) order.setCancelledReason(cursor.getString(cancelledReasonIndex));
        
        int adminNotesIndex = cursor.getColumnIndex("admin_notes");
        if (adminNotesIndex >= 0) order.setAdminNotes(cursor.getString(adminNotesIndex));
        
        int confirmedAtIndex = cursor.getColumnIndex("confirmed_at");
        if (confirmedAtIndex >= 0) order.setConfirmedAt(cursor.getString(confirmedAtIndex));
        
        int shippedAtIndex = cursor.getColumnIndex("shipped_at");
        if (shippedAtIndex >= 0) order.setShippedAt(cursor.getString(shippedAtIndex));
        
        int completedAtIndex = cursor.getColumnIndex("completed_at");
        if (completedAtIndex >= 0) order.setCompletedAt(cursor.getString(completedAtIndex));
        
        int cancelledAtIndex = cursor.getColumnIndex("cancelled_at");
        if (cancelledAtIndex >= 0) order.setCancelledAt(cursor.getString(cancelledAtIndex));
        
        return order;
    }
}
