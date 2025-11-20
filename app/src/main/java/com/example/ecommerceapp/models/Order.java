package com.example.ecommerceapp.models;

public class Order {
    // Order status constants
    public static final String STATUS_PENDING = "pending";        // Chờ xác nhận
    public static final String STATUS_CONFIRMED = "confirmed";    // Đã xác nhận
    public static final String STATUS_SHIPPING = "shipping";      // Đang giao
    public static final String STATUS_COMPLETED = "completed";    // Hoàn thành
    public static final String STATUS_CANCELLED = "cancelled";    // Đã hủy
    
    private int id;
    private int userId;
    private String orderDate;
    private double totalAmount;
    private String status;
    private String shippingAddress;
    private String paymentMethod;
    
    // Extended fields for order management
    private String shipperName;
    private String shipperPhone;
    private String trackingCode;
    private String cancelledReason;
    private String adminNotes;
    private String confirmedAt;
    private String shippedAt;
    private String completedAt;
    private String cancelledAt;

    public Order() {}

    public Order(int id, int userId, String orderDate, double totalAmount,
                 String status, String shippingAddress, String paymentMethod) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    // Extended getters and setters
    public String getShipperName() { return shipperName; }
    public void setShipperName(String shipperName) { this.shipperName = shipperName; }
    
    public String getShipperPhone() { return shipperPhone; }
    public void setShipperPhone(String shipperPhone) { this.shipperPhone = shipperPhone; }
    
    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }
    
    public String getCancelledReason() { return cancelledReason; }
    public void setCancelledReason(String cancelledReason) { this.cancelledReason = cancelledReason; }
    
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    
    public String getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(String confirmedAt) { this.confirmedAt = confirmedAt; }
    
    public String getShippedAt() { return shippedAt; }
    public void setShippedAt(String shippedAt) { this.shippedAt = shippedAt; }
    
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    
    public String getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(String cancelledAt) { this.cancelledAt = cancelledAt; }
    
    // Helper methods
    public String getStatusDisplay() {
        switch (status) {
            case STATUS_PENDING: return "Chờ xác nhận";
            case STATUS_CONFIRMED: return "Đã xác nhận";
            case STATUS_SHIPPING: return "Đang giao";
            case STATUS_COMPLETED: return "Hoàn thành";
            case STATUS_CANCELLED: return "Đã hủy";
            default: return status;
        }
    }
    
    public int getStatusColor() {
        switch (status) {
            case STATUS_PENDING: return android.graphics.Color.parseColor("#FFA726");      // Orange
            case STATUS_CONFIRMED: return android.graphics.Color.parseColor("#42A5F5");    // Blue
            case STATUS_SHIPPING: return android.graphics.Color.parseColor("#AB47BC");     // Purple
            case STATUS_COMPLETED: return android.graphics.Color.parseColor("#66BB6A");    // Green
            case STATUS_CANCELLED: return android.graphics.Color.parseColor("#EF5350");    // Red
            default: return android.graphics.Color.GRAY;
        }
    }
}
