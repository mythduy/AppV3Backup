package com.example.ecommerceapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.CartAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.CartItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {
    private RecyclerView rvCart;
    private TextView tvTotal, tvEmpty;
    private Button btnCheckout;
    private CartAdapter cartAdapter;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        rvCart = findViewById(R.id.rvCart);
        tvTotal = findViewById(R.id.tvTotal);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnCheckout = findViewById(R.id.btnCheckout);

        setupRecyclerView();
        loadCart();

        btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, dbHelper, this::loadCart);
        rvCart.setAdapter(cartAdapter);
    }

    private void loadCart() {
        List<CartItem> cartItems = dbHelper.getCartItems(userId);
        cartAdapter.updateCartItems(cartItems);

        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }

        tvTotal.setText("Tổng cộng: " + formatPrice(total));

        if (cartItems.isEmpty()) {
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            btnCheckout.setEnabled(false);
        } else {
            tvEmpty.setVisibility(android.view.View.GONE);
            btnCheckout.setEnabled(true);
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }
}
