package com.example.ecommerceapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.OrderItemAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Order;
import com.example.ecommerceapp.models.OrderItem;
import com.example.ecommerceapp.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdminActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvOrderId, tvOrderDate, tvStatus, tvCustomerName, tvCustomerPhone, tvCustomerAddress;
    private TextView tvShipperInfo, tvTrackingCode, tvAdminNotes, tvCancelledReason;
    private TextView tvTotalAmount, tvPaymentMethod;
    private MaterialCardView cardShipperInfo, cardAdminNotes, cardCancelledReason;
    private MaterialButton btnConfirm, btnShip, btnComplete, btnCancel, btnCallCustomer, btnAddShipping, btnAddNotes;
    private RecyclerView rvOrderItems;
    private LinearLayout layoutTimeline;
    private TextView tvTimelinePending, tvTimelineConfirmed, tvTimelineShipping, tvTimelineCompleted;
    
    private DatabaseHelper dbHelper;
    private int orderId;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_admin);

        orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        
        initViews();
        setupToolbar();
        loadOrderDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        tvCustomerAddress = findViewById(R.id.tvCustomerAddress);
        tvShipperInfo = findViewById(R.id.tvShipperInfo);
        tvTrackingCode = findViewById(R.id.tvTrackingCode);
        tvAdminNotes = findViewById(R.id.tvAdminNotes);
        tvCancelledReason = findViewById(R.id.tvCancelledReason);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        
        cardShipperInfo = findViewById(R.id.cardShipperInfo);
        cardAdminNotes = findViewById(R.id.cardAdminNotes);
        cardCancelledReason = findViewById(R.id.cardCancelledReason);
        
        btnConfirm = findViewById(R.id.btnConfirm);
        btnShip = findViewById(R.id.btnShip);
        btnComplete = findViewById(R.id.btnComplete);
        btnCancel = findViewById(R.id.btnCancel);
        btnCallCustomer = findViewById(R.id.btnCallCustomer);
        btnAddShipping = findViewById(R.id.btnAddShipping);
        btnAddNotes = findViewById(R.id.btnAddNotes);
        
        rvOrderItems = findViewById(R.id.rvOrderItems);
        layoutTimeline = findViewById(R.id.layoutTimeline);
        tvTimelinePending = findViewById(R.id.tvTimelinePending);
        tvTimelineConfirmed = findViewById(R.id.tvTimelineConfirmed);
        tvTimelineShipping = findViewById(R.id.tvTimelineShipping);
        tvTimelineCompleted = findViewById(R.id.tvTimelineCompleted);
        
        setupClickListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đơn hàng");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        btnConfirm.setOnClickListener(v -> confirmOrder());
        btnShip.setOnClickListener(v -> showAddShippingDialog());
        btnComplete.setOnClickListener(v -> completeOrder());
        btnCancel.setOnClickListener(v -> showCancelDialog());
        btnCallCustomer.setOnClickListener(v -> callCustomer());
        btnAddShipping.setOnClickListener(v -> showAddShippingDialog());
        btnAddNotes.setOnClickListener(v -> showAddNotesDialog());
    }

    private void loadOrderDetails() {
        order = dbHelper.getOrderById(orderId);
        if (order == null) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Order info
        tvOrderId.setText("#" + order.getId());
        tvOrderDate.setText(order.getOrderDate());
        tvStatus.setText(order.getStatusDisplay());
        tvStatus.setTextColor(order.getStatusColor());
        
        // Customer info
        User customer = dbHelper.getUserById(order.getUserId());
        if (customer != null) {
            tvCustomerName.setText(customer.getFullName());
            tvCustomerPhone.setText(customer.getPhone());
        }
        tvCustomerAddress.setText(order.getShippingAddress());
        
        // Payment
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText(formatter.format(order.getTotalAmount()));
        tvPaymentMethod.setText(order.getPaymentMethod());
        
        // Shipper info
        if (order.getShipperName() != null && !order.getShipperName().isEmpty()) {
            cardShipperInfo.setVisibility(View.VISIBLE);
            String shipperInfo = order.getShipperName() + " - " + order.getShipperPhone();
            tvShipperInfo.setText(shipperInfo);
            tvTrackingCode.setText("Mã vận đơn: " + order.getTrackingCode());
        } else {
            cardShipperInfo.setVisibility(View.GONE);
        }
        
        // Admin notes
        if (order.getAdminNotes() != null && !order.getAdminNotes().isEmpty()) {
            cardAdminNotes.setVisibility(View.VISIBLE);
            tvAdminNotes.setText(order.getAdminNotes());
        } else {
            cardAdminNotes.setVisibility(View.GONE);
        }
        
        // Cancelled reason
        if (Order.STATUS_CANCELLED.equals(order.getStatus())) {
            cardCancelledReason.setVisibility(View.VISIBLE);
            tvCancelledReason.setText(order.getCancelledReason());
        } else {
            cardCancelledReason.setVisibility(View.GONE);
        }
        
        // Order items
        List<OrderItem> items = dbHelper.getOrderItems(orderId);
        OrderItemAdapter adapter = new OrderItemAdapter(this, items);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(adapter);
        
        // Timeline
        updateTimeline();
        
        // Action buttons
        updateActionButtons();
    }

    private void updateTimeline() {
        // Pending
        tvTimelinePending.setText("✅ Đã đặt hàng\n" + order.getOrderDate());
        
        // Confirmed
        if (order.getConfirmedAt() != null) {
            tvTimelineConfirmed.setText("✅ Đã xác nhận\n" + order.getConfirmedAt());
            tvTimelineConfirmed.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvTimelineConfirmed.setText("⏳ Chờ xác nhận");
            tvTimelineConfirmed.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
        
        // Shipping
        if (order.getShippedAt() != null) {
            tvTimelineShipping.setText("✅ Đang giao hàng\n" + order.getShippedAt());
            tvTimelineShipping.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvTimelineShipping.setText("⏳ Chờ giao hàng");
            tvTimelineShipping.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
        
        // Completed
        if (order.getCompletedAt() != null) {
            tvTimelineCompleted.setText("✅ Hoàn thành\n" + order.getCompletedAt());
            tvTimelineCompleted.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (Order.STATUS_CANCELLED.equals(order.getStatus())) {
            tvTimelineCompleted.setText("❌ Đã hủy\n" + order.getCancelledAt());
            tvTimelineCompleted.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvTimelineCompleted.setText("⏳ Chờ hoàn thành");
            tvTimelineCompleted.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void updateActionButtons() {
        // Hide all first
        btnConfirm.setVisibility(View.GONE);
        btnShip.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.VISIBLE);
        btnAddShipping.setVisibility(View.GONE);
        
        String status = order.getStatus();
        
        switch (status) {
            case Order.STATUS_PENDING:
                btnConfirm.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
                
            case Order.STATUS_CONFIRMED:
                btnShip.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
                
            case Order.STATUS_SHIPPING:
                btnComplete.setVisibility(View.VISIBLE);
                btnAddShipping.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
                
            case Order.STATUS_COMPLETED:
            case Order.STATUS_CANCELLED:
                btnCancel.setVisibility(View.GONE);
                break;
        }
    }

    private void confirmOrder() {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận đơn hàng")
            .setMessage("Xác nhận đơn hàng #" + orderId + "?")
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                boolean success = dbHelper.updateOrderStatus(orderId, Order.STATUS_CONFIRMED);
                if (success) {
                    Toast.makeText(this, "✅ Đã xác nhận đơn hàng", Toast.LENGTH_SHORT).show();
                    loadOrderDetails();
                } else {
                    Toast.makeText(this, "❌ Lỗi khi xác nhận", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showAddShippingDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_shipping, null);
        EditText etShipperName = dialogView.findViewById(R.id.etShipperName);
        EditText etShipperPhone = dialogView.findViewById(R.id.etShipperPhone);
        EditText etTrackingCode = dialogView.findViewById(R.id.etTrackingCode);
        
        // Pre-fill if exists
        if (order.getShipperName() != null) {
            etShipperName.setText(order.getShipperName());
            etShipperPhone.setText(order.getShipperPhone());
            etTrackingCode.setText(order.getTrackingCode());
        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Thông tin giao hàng")
            .setView(dialogView)
            .setPositiveButton("Lưu", null)
            .setNegativeButton("Hủy", null)
            .create();
            
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String shipperName = etShipperName.getText().toString().trim();
                String shipperPhone = etShipperPhone.getText().toString().trim();
                String trackingCode = etTrackingCode.getText().toString().trim();
                
                if (shipperName.isEmpty() || shipperPhone.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                boolean success = dbHelper.updateOrderShippingInfo(orderId, shipperName, shipperPhone, trackingCode);
                if (success) {
                    // Update status to shipping
                    dbHelper.updateOrderStatus(orderId, Order.STATUS_SHIPPING);
                    Toast.makeText(this, "✅ Đã cập nhật thông tin giao hàng", Toast.LENGTH_SHORT).show();
                    loadOrderDetails();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "❌ Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        dialog.show();
    }

    private void completeOrder() {
        new AlertDialog.Builder(this)
            .setTitle("Hoàn thành đơn hàng")
            .setMessage("Xác nhận đơn hàng #" + orderId + " đã được giao thành công?")
            .setPositiveButton("Hoàn thành", (dialog, which) -> {
                boolean success = dbHelper.updateOrderStatus(orderId, Order.STATUS_COMPLETED);
                if (success) {
                    Toast.makeText(this, "✅ Đơn hàng đã hoàn thành", Toast.LENGTH_SHORT).show();
                    loadOrderDetails();
                } else {
                    Toast.makeText(this, "❌ Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showCancelDialog() {
        String[] reasons = {"Hết hàng", "Khách yêu cầu hủy", "Địa chỉ không hợp lệ", "Không liên lạc được", "Lý do khác"};
        
        new AlertDialog.Builder(this)
            .setTitle("Hủy đơn hàng")
            .setItems(reasons, (dialog, which) -> {
                String reason = reasons[which];
                if (which == reasons.length - 1) {
                    // Custom reason
                    showCustomReasonDialog();
                } else {
                    cancelOrderWithReason(reason);
                }
            })
            .setNegativeButton("Đóng", null)
            .show();
    }

    private void showCustomReasonDialog() {
        EditText et = new EditText(this);
        et.setHint("Nhập lý do hủy");
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        
        new AlertDialog.Builder(this)
            .setTitle("Lý do hủy đơn")
            .setView(et)
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                String reason = et.getText().toString().trim();
                if (!reason.isEmpty()) {
                    cancelOrderWithReason(reason);
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void cancelOrderWithReason(String reason) {
        boolean success = dbHelper.cancelOrder(orderId, reason);
        if (success) {
            Toast.makeText(this, "✅ Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
            loadOrderDetails();
        } else {
            Toast.makeText(this, "❌ Lỗi khi hủy đơn", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddNotesDialog() {
        EditText et = new EditText(this);
        et.setHint("Ghi chú nội bộ");
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        if (order.getAdminNotes() != null) {
            et.setText(order.getAdminNotes());
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Ghi chú admin")
            .setView(et)
            .setPositiveButton("Lưu", (dialog, which) -> {
                String notes = et.getText().toString().trim();
                boolean success = dbHelper.updateAdminNotes(orderId, notes);
                if (success) {
                    Toast.makeText(this, "✅ Đã lưu ghi chú", Toast.LENGTH_SHORT).show();
                    loadOrderDetails();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void callCustomer() {
        User customer = dbHelper.getUserById(order.getUserId());
        if (customer != null && customer.getPhone() != null) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + customer.getPhone()));
            startActivity(intent);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (order != null) {
            loadOrderDetails();
        }
    }
}
