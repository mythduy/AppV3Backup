package com.example.ecommerceapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.CartAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.CartItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartUpdateListener {
    private RecyclerView rvCart;
    private TextView tvTotal, tvEmpty, tvItemCount, tvSelectedCount;
    private CheckBox cbSelectAll;
    private Button btnCheckout, btnDeleteSelected;
    private Toolbar toolbar;
    private BottomNavigationView bottomNav;
    private View layoutItemCount;
    private CartAdapter cartAdapter;
    private DatabaseHelper dbHelper;
    private int userId;
    private List<CartItem> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottomNav);
        tvItemCount = findViewById(R.id.tvItemCount);
        layoutItemCount = findViewById(R.id.layoutItemCount);
        rvCart = findViewById(R.id.rvCart);
        tvTotal = findViewById(R.id.tvTotal);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnCheckout = findViewById(R.id.btnCheckout);
        cbSelectAll = findViewById(R.id.cbSelectAll);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);

        setupToolbar();
        setupBottomNavigation();
        setupRecyclerView();
        setupListeners();
        loadCart();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_cart);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } else if (id == R.id.nav_categories) {
                Intent intent = new Intent(CartActivity.this, CategoriesActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } else if (id == R.id.nav_cart) {
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(CartActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            return false;
        });
    }

    private void setupRecyclerView() {
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, dbHelper, this);
        rvCart.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        // Checkbox chọn tất cả
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (cartItems != null) {
                for (CartItem item : cartItems) {
                    item.setSelected(isChecked);
                }
                cartAdapter.notifyDataSetChanged();
                updateTotalAndSelection();
            }
        });

        // Nút xóa sản phẩm đã chọn
        btnDeleteSelected.setOnClickListener(v -> deleteSelectedItems());

        // Nút thanh toán
        btnCheckout.setOnClickListener(v -> {
            List<CartItem> selectedItems = getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            // Truyền danh sách ID của các sản phẩm đã chọn
            ArrayList<Integer> selectedIds = new ArrayList<>();
            for (CartItem item : selectedItems) {
                selectedIds.add(item.getId());
            }
            intent.putIntegerArrayListExtra("selected_cart_ids", selectedIds);
            startActivity(intent);
        });
    }

    private void loadCart() {
        cartItems = dbHelper.getCartItems(userId);
        cartAdapter.updateCartItems(cartItems);
        updateTotalAndSelection();
    }

    private void updateTotalAndSelection() {
        if (cartItems == null || cartItems.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(false);
            btnDeleteSelected.setEnabled(false);
            cbSelectAll.setEnabled(false);
            layoutItemCount.setVisibility(View.GONE);
            tvSelectedCount.setText("Chọn: 0");
            tvTotal.setText("Tổng cộng: " + formatPrice(0));
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        cbSelectAll.setEnabled(true);

        // Tính toán
        double total = 0;
        int totalItemCount = cartItems.size(); // Số lượng sản phẩm, không phải tổng quantity
        int selectedCount = 0;

        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getTotalPrice();
                selectedCount++;
            }
        }

        // Cập nhật UI
        tvTotal.setText("Tổng cộng: " + formatPrice(total));
        tvSelectedCount.setText("Chọn: " + selectedCount + "/" + cartItems.size());
        
        // Update item count in toolbar - hiển thị số lượng sản phẩm
        if (totalItemCount > 0) {
            tvItemCount.setText(String.valueOf(totalItemCount));
            layoutItemCount.setVisibility(View.VISIBLE);
        } else {
            layoutItemCount.setVisibility(View.GONE);
        }

        // Enable/disable buttons
        btnCheckout.setEnabled(selectedCount > 0);
        btnDeleteSelected.setEnabled(selectedCount > 0);

        // Update select all checkbox state
        cbSelectAll.setOnCheckedChangeListener(null); // Tạm tắt listener
        cbSelectAll.setChecked(selectedCount == cartItems.size() && cartItems.size() > 0);
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CartItem item : cartItems) {
                item.setSelected(isChecked);
            }
            cartAdapter.notifyDataSetChanged();
            updateTotalAndSelection();
        });
    }

    private List<CartItem> getSelectedItems() {
        List<CartItem> selectedItems = new ArrayList<>();
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }
        }
        return selectedItems;
    }

    private void deleteSelectedItems() {
        List<CartItem> selectedItems = getSelectedItems();
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn sản phẩm cần xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa " + selectedItems.size() + " sản phẩm đã chọn?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    for (CartItem item : selectedItems) {
                        dbHelper.removeFromCart(item.getId());
                    }
                    Toast.makeText(this, "Đã xóa " + selectedItems.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
                    loadCart();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    @Override
    public void onCartUpdated() {
        loadCart();
    }

    @Override
    public void onSelectionChanged() {
        updateTotalAndSelection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }
}
