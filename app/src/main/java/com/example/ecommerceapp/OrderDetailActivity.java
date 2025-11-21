package com.example.ecommerceapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.OrderDetailAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Order;
import com.example.ecommerceapp.models.OrderItem;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvShippingAddress, tvPaymentMethod;
    private TextView tvSubtotal, tvShipping, tvTotal, tvCancelledReason;
    private RecyclerView rvOrderItems;
    private MaterialButton btnCancelOrder;
    private android.widget.LinearLayout layoutCancelledReason;
    private OrderDetailAdapter adapter;
    private DatabaseHelper dbHelper;
    private int orderId;
    private int userId;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);
        orderId = getIntent().getIntExtra("order_id", -1);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadOrderDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShipping = findViewById(R.id.tvShipping);
        tvTotal = findViewById(R.id.tvTotal);
        tvCancelledReason = findViewById(R.id.tvCancelledReason);
        layoutCancelledReason = findViewById(R.id.layoutCancelledReason);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        
        if (btnCancelOrder != null) {
            btnCancelOrder.setOnClickListener(v -> showCancelOrderDialog());
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đơn hàng");
        }
    }

    private void setupRecyclerView() {
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderDetailAdapter(this);
        rvOrderItems.setAdapter(adapter);
    }

    private void loadOrderDetails() {
        currentOrder = dbHelper.getOrderById(orderId);
        if (currentOrder != null) {
            tvOrderId.setText("Đơn hàng #" + currentOrder.getId());
            tvOrderDate.setText(currentOrder.getOrderDate());
            tvOrderStatus.setText(currentOrder.getStatusDisplay());
            tvShippingAddress.setText(currentOrder.getShippingAddress());
            tvPaymentMethod.setText(currentOrder.getPaymentMethod());
            
            double subtotal = currentOrder.getTotalAmount();
            double shipping = 0; // Phí ship = 0
            double total = subtotal + shipping;
            
            tvSubtotal.setText(formatPrice(subtotal));
            tvShipping.setText(formatPrice(shipping));
            tvTotal.setText(formatPrice(total));
            
            // Show/Hide cancel button based on status
            if (btnCancelOrder != null) {
                if (Order.STATUS_PENDING.equals(currentOrder.getStatus())) {
                    btnCancelOrder.setVisibility(View.VISIBLE);
                } else {
                    btnCancelOrder.setVisibility(View.GONE);
                }
            }
            
            // Show cancelled reason if order was cancelled
            if (Order.STATUS_CANCELLED.equals(currentOrder.getStatus()) && 
                currentOrder.getCancelledReason() != null && 
                !currentOrder.getCancelledReason().isEmpty()) {
                if (layoutCancelledReason != null && tvCancelledReason != null) {
                    layoutCancelledReason.setVisibility(View.VISIBLE);
                    tvCancelledReason.setText(currentOrder.getCancelledReason());
                }
            } else {
                if (layoutCancelledReason != null) {
                    layoutCancelledReason.setVisibility(View.GONE);
                }
            }
            
            // Load order items
            adapter.updateOrderItems(dbHelper.getOrderItems(orderId));
        }
    }
    
    private void showCancelOrderDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cancel_order, null);
        RadioGroup rgReason = dialogView.findViewById(R.id.rgReason);
        EditText etOtherReason = dialogView.findViewById(R.id.etOtherReason);
        RadioButton rbOther = dialogView.findViewById(R.id.rbOther);
        
        rgReason.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOther) {
                etOtherReason.setVisibility(View.VISIBLE);
                etOtherReason.requestFocus();
            } else {
                etOtherReason.setVisibility(View.GONE);
            }
        });
        
        new AlertDialog.Builder(this)
            .setTitle("Hủy đơn hàng")
            .setView(dialogView)
            .setPositiveButton("Xác nhận hủy", (dialog, which) -> {
                int selectedId = rgReason.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(this, "Vui lòng chọn lý do hủy đơn", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String reason;
                if (selectedId == R.id.rbOther) {
                    reason = etOtherReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập lý do hủy", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    RadioButton selectedButton = dialogView.findViewById(selectedId);
                    reason = selectedButton.getText().toString();
                }
                
                cancelOrder(reason);
            })
            .setNegativeButton("Đóng", null)
            .show();
    }
    
    private void cancelOrder(String reason) {
        boolean success = dbHelper.cancelOrder(orderId, reason);
        if (success) {
            Toast.makeText(this, "✅ Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
            loadOrderDetails(); // Reload to show updated status
        } else {
            Toast.makeText(this, "❌ Lỗi khi hủy đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
