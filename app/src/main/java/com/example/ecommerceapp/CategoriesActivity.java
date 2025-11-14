package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.CategoryGridAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        dbHelper = new DatabaseHelper(this);

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
            getSupportActionBar().setTitle("Danh mục sản phẩm");
        }
    }

    private void setupRecyclerView() {
        rvCategories.setLayoutManager(new GridLayoutManager(this, 2));
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
                startActivity(new Intent(CategoriesActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_categories) {
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(CategoriesActivity.this, CartActivity.class));
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(CategoriesActivity.this, OrderHistoryActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(CategoriesActivity.this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    private void loadCategories() {
        List<String> categories = dbHelper.getAllCategories();
        Map<String, Integer> categoryCount = new HashMap<>();
        
        for (String category : categories) {
            int count = dbHelper.getProductCountByCategory(category);
            categoryCount.put(category, count);
        }
        
        categoryAdapter.updateCategories(categories, categoryCount);
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
