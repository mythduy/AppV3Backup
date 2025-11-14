package com.example.ecommerceapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.AdminProductAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ManageProductsActivity extends AppCompatActivity {
    private RecyclerView rvProducts;
    private AdminProductAdapter adapter;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;
    private Toolbar toolbar;
    private TextView tvTotalProducts, tvTotalValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        dbHelper = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        rvProducts = findViewById(R.id.rvProducts);
        fabAdd = findViewById(R.id.fabAdd);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvTotalValue = findViewById(R.id.tvTotalValue);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý sản phẩm");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupRecyclerView();
        loadProducts();

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditProductActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        List<Product> products = dbHelper.getAllProducts();
        adapter = new AdminProductAdapter(this, products, new AdminProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {
                Intent intent = new Intent(ManageProductsActivity.this, AddEditProductActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Product product) {
                showDeleteConfirmDialog(product);
            }
        });
        rvProducts.setAdapter(adapter);
    }

    private void showDeleteConfirmDialog(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa sản phẩm \"" + product.getName() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean success = dbHelper.deleteProduct(product.getId());
                    if (success) {
                        Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadProducts() {
        List<Product> products = dbHelper.getAllProducts();
        adapter.updateProducts(products);
        
        // Update statistics
        tvTotalProducts.setText(String.valueOf(products.size()));
        
        double totalValue = 0;
        for (Product product : products) {
            totalValue += product.getPrice() * product.getStock();
        }
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalValue.setText(formatter.format(totalValue));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
}
