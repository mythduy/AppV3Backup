package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.ProductAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import java.util.List;

public class CategoryProductsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvCategoryName, tvProductCount;
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private DatabaseHelper dbHelper;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        dbHelper = new DatabaseHelper(this);
        categoryName = getIntent().getStringExtra("category_name");

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadProducts();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvProductCount = findViewById(R.id.tvProductCount);
        rvProducts = findViewById(R.id.rvProducts);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName);
        }
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, new java.util.ArrayList<>(), product -> {
            Intent intent = new Intent(CategoryProductsActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        rvProducts.setAdapter(productAdapter);
    }

    private void loadProducts() {
        List<Product> products = dbHelper.getProductsByCategory(categoryName);
        productAdapter.updateProducts(products);
        
        tvCategoryName.setText(categoryName);
        tvProductCount.setText(products.size() + " sản phẩm");
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
