package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.OrderAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Order;
import com.google.android.material.tabs.TabLayout;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ManageOrdersActivity extends AppCompatActivity {
    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private DatabaseHelper dbHelper;
    private Toolbar toolbar;
    private TextView tvTotalOrders, tvTotalRevenue, tvPendingOrders, tvCompletedOrders;
    private TabLayout tabLayout;
    private CardView cardPending, cardConfirmed, cardShipping, cardCompleted;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        dbHelper = new DatabaseHelper(this);

        initViews();
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý đơn hàng");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupTabs();
        setupRecyclerView();
        loadAllOrders();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvOrders = findViewById(R.id.rvOrders);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvCompletedOrders = findViewById(R.id.tvCompletedOrders);
        tabLayout = findViewById(R.id.tabLayout);
    }
    
    private void setupTabs() {
        if (tabLayout != null) {
            tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
            tabLayout.addTab(tabLayout.newTab().setText("Chờ XN"));
            tabLayout.addTab(tabLayout.newTab().setText("Đã XN"));
            tabLayout.addTab(tabLayout.newTab().setText("Đang giao"));
            tabLayout.addTab(tabLayout.newTab().setText("Hoàn thành"));
            tabLayout.addTab(tabLayout.newTab().setText("Đã hủy"));
            
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0: filterOrders("all"); break;
                        case 1: filterOrders(Order.STATUS_PENDING); break;
                        case 2: filterOrders(Order.STATUS_CONFIRMED); break;
                        case 3: filterOrders(Order.STATUS_SHIPPING); break;
                        case 4: filterOrders(Order.STATUS_COMPLETED); break;
                        case 5: filterOrders(Order.STATUS_CANCELLED); break;
                    }
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(this, order -> {
            // Open admin order detail page with action buttons
            Intent intent = new Intent(this, OrderDetailAdminActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
        });
        rvOrders.setAdapter(adapter);
    }

    private void loadAllOrders() {
        filterOrders(currentFilter);
    }
    
    private void filterOrders(String status) {
        currentFilter = status;
        List<Order> orders;
        
        if (status.equals("all")) {
            orders = dbHelper.getAllOrders();
        } else {
            orders = dbHelper.getOrdersByStatus(status);
        }
        
        adapter.updateOrders(orders);
        
        // Calculate statistics for all orders
        List<Order> allOrders = dbHelper.getAllOrders();
        int totalOrders = allOrders.size();
        int pendingOrders = 0;
        int completedOrders = 0;
        double totalRevenue = 0;
        
        for (Order order : allOrders) {
            totalRevenue += order.getTotalAmount();
            if (Order.STATUS_PENDING.equals(order.getStatus())) {
                pendingOrders++;
            } else if (Order.STATUS_COMPLETED.equals(order.getStatus())) {
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
