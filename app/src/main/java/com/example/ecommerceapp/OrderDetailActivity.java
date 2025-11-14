package com.example.ecommerceapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.OrderDetailAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Order;
import com.example.ecommerceapp.models.OrderItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvShippingAddress, tvPaymentMethod;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private RecyclerView rvOrderItems;
    private OrderDetailAdapter adapter;
    private DatabaseHelper dbHelper;
    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

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
        rvOrderItems = findViewById(R.id.rvOrderItems);
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
        Order order = dbHelper.getOrderById(orderId);
        if (order != null) {
            tvOrderId.setText("Đơn hàng #" + order.getId());
            tvOrderDate.setText(order.getOrderDate());
            tvOrderStatus.setText(order.getStatus());
            tvShippingAddress.setText(order.getShippingAddress());
            tvPaymentMethod.setText(order.getPaymentMethod());
            
            double subtotal = order.getTotalAmount();
            double shipping = 0; // Phí ship = 0
            double total = subtotal + shipping;
            
            tvSubtotal.setText(formatPrice(subtotal));
            tvShipping.setText(formatPrice(shipping));
            tvTotal.setText(formatPrice(total));
            
            // Load order items
            adapter.updateOrderItems(dbHelper.getOrderItems(orderId));
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
