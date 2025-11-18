package com.example.ecommerceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView ivProduct;
    private TextView tvName, tvPrice, tvDescription, tvCategory, tvStock;
    private TextView tvSKU, tvWarranty, tvQuantity, tvTotalPrice, tvRating;
    private MaterialButton btnAddToCart, btnBuyNow, btnFavorite, btnShare;
    private MaterialButton btnIncrement, btnDecrement;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private DatabaseHelper dbHelper;
    private Product product;
    private int userId;
    private int quantity = 1;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Load product data
        int productId = getIntent().getIntExtra("product_id", -1);
        loadProductDetails(productId);

        // Setup click listeners
        setupClickListeners();

        // Apply animations
        applyAnimations();
    }

    private void initViews() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);
        ivProduct = findViewById(R.id.ivProduct);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvStock = findViewById(R.id.tvStock);
        tvSKU = findViewById(R.id.tvSKU);
        tvWarranty = findViewById(R.id.tvWarranty);
        tvRating = findViewById(R.id.tvRating);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare = findViewById(R.id.btnShare);
        btnIncrement = findViewById(R.id.btnIncrement);
        btnDecrement = findViewById(R.id.btnDecrement);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void setupClickListeners() {
        btnAddToCart.setOnClickListener(v -> addToCart());
        btnBuyNow.setOnClickListener(v -> buyNow());
        
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        
        btnShare.setOnClickListener(v -> shareProduct());

        btnIncrement.setOnClickListener(v -> incrementQuantity());
        
        btnDecrement.setOnClickListener(v -> decrementQuantity());
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        findViewById(R.id.tvName).startAnimation(fadeIn);
    }

    private void loadProductDetails(int productId) {
        product = dbHelper.getProductById(productId);

        if (product != null) {
            // Set collapsing toolbar title
            collapsingToolbar.setTitle(product.getName());

            // Product basic info
            tvName.setText(product.getName());
            tvPrice.setText(formatPrice(product.getPrice()));
            tvDescription.setText(product.getDescription());
            
            // Product details
            tvCategory.setText(product.getCategory());
            tvStock.setText("Còn " + product.getStock() + " sp");
            tvSKU.setText("PRD-" + String.format("%04d", product.getId()));
            tvWarranty.setText("12 tháng");
            tvRating.setText("4.5");
            
            // Initial quantity and total
            updateQuantityAndTotal();

            // Load product image with Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_product_placeholder)
                    .error(R.drawable.ic_product_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(ivProduct);
            } else {
                ivProduct.setImageResource(R.drawable.ic_product_placeholder);
            }

            // Check if product is in wishlist
            checkFavoriteStatus();
        }
    }

    private void incrementQuantity() {
        if (product != null && quantity < product.getStock()) {
            quantity++;
            updateQuantityAndTotal();
            
            // Animation feedback
            Animation scale = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
            tvQuantity.startAnimation(scale);
        } else {
            Toast.makeText(this, "⚠️ Đã đạt số lượng tối đa trong kho", Toast.LENGTH_SHORT).show();
        }
    }

    private void decrementQuantity() {
        if (quantity > 1) {
            quantity--;
            updateQuantityAndTotal();
            
            // Animation feedback
            Animation scale = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
            tvQuantity.startAnimation(scale);
        } else {
            Toast.makeText(this, "⚠️ Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateQuantityAndTotal() {
        tvQuantity.setText(String.valueOf(quantity));
        if (product != null) {
            double totalPrice = product.getPrice() * quantity;
            tvTotalPrice.setText(formatPrice(totalPrice));
        }
    }

    private void toggleFavorite() {
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng yêu thích", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        isFavorite = !isFavorite;
        
        if (isFavorite) {
            // Add to wishlist
            long result = dbHelper.addToWishlist(userId, product.getId());
            if (result != -1) {
                btnFavorite.setIcon(getDrawable(R.drawable.ic_favorite_filled));
                Toast.makeText(this, "❤️ Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Remove from wishlist
            dbHelper.removeFromWishlist(userId, product.getId());
            btnFavorite.setIcon(getDrawable(R.drawable.ic_favorite_border));
            Toast.makeText(this, "💔 Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        }

        // Animation
        Animation scale = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        btnFavorite.startAnimation(scale);
    }

    private void checkFavoriteStatus() {
        if (userId != -1 && product != null) {
            isFavorite = dbHelper.isInWishlist(userId, product.getId());
            if (isFavorite) {
                btnFavorite.setIcon(getDrawable(R.drawable.ic_favorite_filled));
            }
        }
    }

    private void shareProduct() {
        if (product != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "Xem sản phẩm tuyệt vời này: " + product.getName() + 
                                "\nGiá: " + formatPrice(product.getPrice()) +
                                "\n\nTải app Electronics Shop để mua ngay!";
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, product.getName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
        }
    }

    private void addToCart() {
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        
        if (product.getStock() > 0) {
            // Show loading
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Đang thêm...");

            long result = dbHelper.addToCart(userId, product.getId(), quantity);
            if (result != -1) {
                Toast.makeText(this, "✅ Đã thêm " + quantity + " sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                
                // Animation
                Animation scale = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
                btnAddToCart.startAnimation(scale);
                
                // Reset quantity
                quantity = 1;
                updateQuantityAndTotal();
            } else {
                Toast.makeText(this, "❌ Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }

            // Restore button
            btnAddToCart.setEnabled(true);
            btnAddToCart.setText("Thêm vào giỏ");
        } else {
            Toast.makeText(this, "❌ Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void buyNow() {
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        
        if (product.getStock() > 0) {
            // Add to cart first
            long result = dbHelper.addToCart(userId, product.getId(), quantity);
            
            if (result != -1) {
                // Navigate to cart
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            } else {
                Toast.makeText(this, "❌ Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "❌ Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}