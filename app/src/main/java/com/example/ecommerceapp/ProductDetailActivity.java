package com.example.ecommerceapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView ivProduct;
    private TextView tvName, tvPrice, tvDescription, tvCategory, tvStock;
    private Button btnAddToCart, btnBuyNow;
    private DatabaseHelper dbHelper;
    private Product product;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        ivProduct = findViewById(R.id.ivProduct);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvStock = findViewById(R.id.tvStock);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        int productId = getIntent().getIntExtra("product_id", -1);
        loadProductDetails(productId);

        btnAddToCart.setOnClickListener(v -> addToCart());
        btnBuyNow.setOnClickListener(v -> buyNow());
    }

    private void loadProductDetails(int productId) {
        product = dbHelper.getProductById(productId);

        if (product != null) {
            tvName.setText(product.getName());
            tvPrice.setText(formatPrice(product.getPrice()));
            tvDescription.setText(product.getDescription());
            tvCategory.setText("Danh mục: " + product.getCategory());
            tvStock.setText("Còn lại: " + product.getStock() + " sản phẩm");

            // Set image (trong thực tế sẽ load từ URL hoặc resource)
            ivProduct.setImageResource(R.drawable.ic_product_placeholder);
        }
    }

    private void addToCart() {
        if (product.getStock() > 0) {
            long result = dbHelper.addToCart(userId, product.getId(), 1);
            if (result != -1) {
                Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void buyNow() {
        if (product.getStock() > 0) {
            addToCart();
            startActivity(new android.content.Intent(this, CartActivity.class));
        } else {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
}