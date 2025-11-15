package com.example.ecommerceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.WishlistAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView rvWishlist;
    private LinearLayout tvEmpty;
    private WishlistAdapter wishlistAdapter;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadWishlist();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvWishlist = findViewById(R.id.rvWishlist);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Yêu thích");
        }
    }

    private void setupRecyclerView() {
        rvWishlist.setLayoutManager(new GridLayoutManager(this, 2));
        wishlistAdapter = new WishlistAdapter(this, product -> {
            Intent intent = new Intent(WishlistActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        }, () -> {
            // Callback khi xóa item
            loadWishlist();
        });
        rvWishlist.setAdapter(wishlistAdapter);
    }

    private void loadWishlist() {
        List<Product> products = dbHelper.getWishlistProducts(userId);
        wishlistAdapter.updateProducts(products);

        if (products.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvWishlist.setVisibility(View.GONE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("0 sản phẩm");
            }
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvWishlist.setVisibility(View.VISIBLE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(products.size() + " sản phẩm");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWishlist();
    }
}
