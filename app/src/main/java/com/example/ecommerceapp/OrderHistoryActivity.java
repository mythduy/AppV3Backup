package com.example.ecommerceapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.OrderAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Order;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {
    private RecyclerView rvOrders;
    private TextView tvEmpty;
    private OrderAdapter orderAdapter;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        rvOrders = findViewById(R.id.rvOrders);
        tvEmpty = findViewById(R.id.tvEmpty);

        setupRecyclerView();
        loadOrders();
    }

    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this);
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        List<Order> orders = dbHelper.getOrderHistory(userId);
        orderAdapter.updateOrders(orders);

        if (orders.isEmpty()) {
            tvEmpty.setVisibility(android.view.View.VISIBLE);
        } else {
            tvEmpty.setVisibility(android.view.View.GONE);
        }
    }
}
