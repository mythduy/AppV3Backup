package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.OrderAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Order;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyTitle, tvEmptyMessage;
    private MaterialButton btnShopNow;
    private OrderAdapter orderAdapter;
    private DatabaseHelper dbHelper;
    private int userId;
    private List<Order> allOrders;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        loadOrders();
        
        // Check if filter is provided from intent
        String filter = getIntent().getStringExtra("filter");
        if (filter != null && !filter.equals("all")) {
            // Select appropriate tab based on filter
            int tabPosition = getTabPositionForFilter(filter);
            if (tabPosition != -1 && tabLayout.getTabCount() > tabPosition) {
                TabLayout.Tab tab = tabLayout.getTabAt(tabPosition);
                if (tab != null) {
                    tab.select();
                }
            }
        }
    }
    
    private int getTabPositionForFilter(String filter) {
        switch (filter) {
            case Order.STATUS_PENDING: return 1;
            case Order.STATUS_SHIPPING: return 2;
            case Order.STATUS_COMPLETED: return 3;
            case Order.STATUS_CANCELLED: return 4;
            default: return 0;
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        rvOrders = findViewById(R.id.rvOrders);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnShopNow = findViewById(R.id.btnShopNow);

        btnShopNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Chờ XN"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang giao"));
        tabLayout.addTab(tabLayout.newTab().setText("Hoàn thành"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã hủy"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFilter = "all";
                        filterOrders("all");
                        break;
                    case 1:
                        currentFilter = Order.STATUS_PENDING;
                        filterOrders(Order.STATUS_PENDING);
                        break;
                    case 2:
                        currentFilter = Order.STATUS_SHIPPING;
                        filterOrders(Order.STATUS_SHIPPING);
                        break;
                    case 3:
                        currentFilter = Order.STATUS_COMPLETED;
                        filterOrders(Order.STATUS_COMPLETED);
                        break;
                    case 4:
                        currentFilter = Order.STATUS_CANCELLED;
                        filterOrders(Order.STATUS_CANCELLED);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this);
        orderAdapter.setOnOrderClickListener(order -> {
            Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        allOrders = dbHelper.getOrderHistory(userId);
        filterOrders(currentFilter);
    }

    private void filterOrders(String status) {
        List<Order> filteredOrders;
        
        if (status.equals("all")) {
            filteredOrders = allOrders;
        } else {
            filteredOrders = new ArrayList<>();
            for (Order order : allOrders) {
                if (order.getStatus().equals(status)) {
                    filteredOrders.add(order);
                }
            }
        }

        orderAdapter.updateOrders(filteredOrders);
        updateEmptyState(filteredOrders.isEmpty(), status);
    }

    private void updateEmptyState(boolean isEmpty, String filter) {
        if (isEmpty) {
            rvOrders.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);

            if (filter.equals("all")) {
                tvEmptyTitle.setText("Chưa có đơn hàng");
                tvEmptyMessage.setText("Các đơn hàng của bạn sẽ hiển thị ở đây");
                btnShopNow.setVisibility(View.VISIBLE);
            } else {
                tvEmptyTitle.setText("Không có đơn hàng");
                tvEmptyMessage.setText("Bạn chưa có đơn hàng nào ở trạng thái này");
                btnShopNow.setVisibility(View.GONE);
            }
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }
}
