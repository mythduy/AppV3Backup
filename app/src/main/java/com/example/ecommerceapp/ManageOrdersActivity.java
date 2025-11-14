package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.OrderAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Order;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ManageOrdersActivity extends AppCompatActivity {
    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private DatabaseHelper dbHelper;
    private Toolbar toolbar;
    private TextView tvTotalOrders, tvTotalRevenue, tvPendingOrders, tvCompletedOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        dbHelper = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        rvOrders = findViewById(R.id.rvOrders);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvCompletedOrders = findViewById(R.id.tvCompletedOrders);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý đơn hàng");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupRecyclerView();
        loadAllOrders();
    }

    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(this, order -> {
            // Open order detail
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
        });
        rvOrders.setAdapter(adapter);
    }

    private void loadAllOrders() {
        List<Order> orders = dbHelper.getAllOrders();
        adapter.updateOrders(orders);
        
        // Calculate statistics
        int totalOrders = orders.size();
        int pendingOrders = 0;
        int completedOrders = 0;
        double totalRevenue = 0;
        
        for (Order order : orders) {
            totalRevenue += order.getTotalAmount();
            if ("Pending".equalsIgnoreCase(order.getStatus()) || 
                "Đang xử lý".equalsIgnoreCase(order.getStatus())) {
                pendingOrders++;
            } else if ("Completed".equalsIgnoreCase(order.getStatus()) || 
                       "Hoàn thành".equalsIgnoreCase(order.getStatus())) {
                completedOrders++;
            }
        }
        
        // Update statistics
        tvTotalOrders.setText(String.valueOf(totalOrders));
        tvPendingOrders.setText(String.valueOf(pendingOrders));
        tvCompletedOrders.setText(String.valueOf(completedOrders));
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalRevenue.setText(formatter.format(totalRevenue));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllOrders();
    }
}
