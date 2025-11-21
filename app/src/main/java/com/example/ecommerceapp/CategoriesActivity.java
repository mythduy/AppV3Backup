package com.example.ecommerceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.CategoryGridAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriesActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView rvCategories;
    private BottomNavigationView bottomNav;
    private CategoryGridAdapter categoryAdapter;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        dbHelper = new DatabaseHelper(this);
        
        // Get user ID
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupBottomNavigation();
        loadCategories();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvCategories = findViewById(R.id.rvCategories);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvCategories.setLayoutManager(layoutManager);
        rvCategories.setHasFixedSize(false);
        rvCategories.setNestedScrollingEnabled(false);
        
        categoryAdapter = new CategoryGridAdapter(this, category -> {
            Intent intent = new Intent(CategoriesActivity.this, CategoryProductsActivity.class);
            intent.putExtra("category_name", category);
            startActivity(intent);
        });
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_categories);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(CategoriesActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } else if (id == R.id.nav_categories) {
                return true;
            } else if (id == R.id.nav_cart) {
                Intent intent = new Intent(CategoriesActivity.this, CartActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(CategoriesActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            return false;
        });
        
        // Update cart badge on load
        updateCartBadge();
    }
    
    private void updateCartBadge() {
        if (userId != -1) {
            int cartCount = dbHelper.getCartItemCount(userId);
            
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_cart);
            if (cartCount > 0) {
                badge.setVisible(true);
                badge.setNumber(cartCount);
                badge.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorRed));
            } else {
                badge.setVisible(false);
            }
        }
    }

    private void loadCategories() {
        List<String> categories = dbHelper.getAllCategories();
        Map<String, Integer> categoryCount = new HashMap<>();
        
        for (String category : categories) {
            int count = dbHelper.getProductCountByCategory(category);
            categoryCount.put(category, count);
        }
        
        categoryAdapter.updateCategories(categories, categoryCount);
        
        // Đảm bảo RecyclerView được đo lại sau khi load data
        rvCategories.post(() -> {
            rvCategories.requestLayout();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload categories to show updated images
        loadCategories();
        // Update cart badge
        updateCartBadge();
    }

}
