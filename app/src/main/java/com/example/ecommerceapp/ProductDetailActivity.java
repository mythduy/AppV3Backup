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
import java.util.ArrayList;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView ivProduct;
    private TextView tvName, tvPrice, tvDescription, tvCategory, tvStock;
    private TextView tvSKU, tvWarranty, tvQuantity, tvTotalPrice, tvRating;
    private TextView tvReviewCount, tvAverageRating, tvRatingCount, tvSold;
    private android.widget.RatingBar ratingBarAverage, ratingBarHeader;
    private MaterialButton btnAddToCart, btnBuyNow, btnFavorite, btnShare;
    private MaterialButton btnIncrement, btnDecrement, btnWriteReview;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private androidx.recyclerview.widget.RecyclerView rvReviews, rvRelatedProducts;
    private android.widget.LinearLayout layoutEmptyReviews;
    private TextView tvNoRelatedProducts;
    private com.google.android.material.card.MaterialCardView cardRelatedProducts;
    private com.example.ecommerceapp.adapters.ReviewAdapter reviewAdapter;
    private com.example.ecommerceapp.adapters.ProductAdapter relatedProductsAdapter;
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
        tvRatingCount = findViewById(R.id.tvRatingCount);
        tvSold = findViewById(R.id.tvSold);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare = findViewById(R.id.btnShare);
        btnIncrement = findViewById(R.id.btnIncrement);
        btnDecrement = findViewById(R.id.btnDecrement);
        
        // Reviews views
        tvReviewCount = findViewById(R.id.tvReviewCount);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        ratingBarAverage = findViewById(R.id.ratingBarAverage);
        ratingBarHeader = findViewById(R.id.ratingBarHeader);
        rvRelatedProducts = findViewById(R.id.rvRelatedProducts);
        tvNoRelatedProducts = findViewById(R.id.tvNoRelatedProducts);
        cardRelatedProducts = findViewById(R.id.cardRelatedProducts);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        rvReviews = findViewById(R.id.rvReviews);
        layoutEmptyReviews = findViewById(R.id.layoutEmptyReviews);
        
        // Setup reviews RecyclerView
        rvReviews.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        reviewAdapter = new com.example.ecommerceapp.adapters.ReviewAdapter(this);
        rvReviews.setAdapter(reviewAdapter);
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
        
        btnWriteReview.setOnClickListener(v -> showWriteReviewDialog());
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
            
            // Display price with discount if available
            if (product.getDiscount() > 0) {
                tvPrice.setText(formatPrice(product.getFinalPrice()) + " (Giảm " + 
                              String.format("%.0f", product.getDiscount()) + "%)");
            } else {
                tvPrice.setText(formatPrice(product.getPrice()));
            }
            
            tvDescription.setText(product.getDescription());
            
            // Product details
            tvCategory.setText(product.getCategory());
            
            // Handle stock display and button states
            if (product.getStock() <= 0) {
                tvStock.setText("Hết hàng");
                tvStock.setTextColor(getResources().getColor(R.color.colorRed));
                
                // Disable buttons when out of stock
                btnAddToCart.setEnabled(false);
                btnAddToCart.setAlpha(0.5f);
                btnAddToCart.setText("Hết hàng");
                
                btnBuyNow.setEnabled(false);
                btnBuyNow.setAlpha(0.5f);
                btnBuyNow.setText("Hết hàng");
                
                btnIncrement.setEnabled(false);
                btnDecrement.setEnabled(false);
            } else {
                tvStock.setText("Còn " + product.getStock() + " sp");
                tvStock.setTextColor(getResources().getColor(R.color.colorGreen));
                
                // Enable buttons when in stock
                btnAddToCart.setEnabled(true);
                btnAddToCart.setAlpha(1.0f);
                btnAddToCart.setText("Thêm vào giỏ");
                
                btnBuyNow.setEnabled(true);
                btnBuyNow.setAlpha(1.0f);
                btnBuyNow.setText("Mua ngay");
                
                btnIncrement.setEnabled(true);
                btnDecrement.setEnabled(true);
            }
            
            tvSKU.setText(product.getFormattedSku());
            tvWarranty.setText(product.getWarranty());
            
            // Update rating display in header
            double rating = product.getRating();
            int reviewCount = dbHelper.getReviewCount(product.getId());
            
            if (rating > 0 && reviewCount > 0) {
                tvRating.setText(String.format("%.1f", rating));
                tvRatingCount.setText("(" + reviewCount + " đánh giá)");
                ratingBarHeader.setRating((float) rating);
            } else {
                tvRating.setText("0.0");
                tvRatingCount.setText("(Chưa có đánh giá)");
                ratingBarHeader.setRating(0);
            }
            
            // Update sold count
            int soldCount = dbHelper.getProductSoldCount(product.getId());
            tvSold.setText("Đã bán: " + soldCount);
            
            // Initial quantity and total
            updateQuantityAndTotal();

            // Load product image - support both file path and URL
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                java.io.File imageFile = new java.io.File(product.getImageUrl());
                if (imageFile.exists()) {
                    // Load from internal storage
                    Glide.with(this)
                        .load(imageFile)
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .centerCrop()
                        .into(ivProduct);
                } else if (product.getImageUrl().startsWith("http://") || 
                          product.getImageUrl().startsWith("https://")) {
                    // Try loading as URL if it's a valid URL
                    Glide.with(this)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(ivProduct);
                } else {
                    // Invalid path - use placeholder
                    ivProduct.setImageResource(R.drawable.ic_product_placeholder);
                }
            } else {
                ivProduct.setImageResource(R.drawable.ic_product_placeholder);
            }

            // Check if product is in wishlist
            checkFavoriteStatus();
            
            // Load reviews
            loadReviews();
            
            // Load related products
            loadRelatedProducts();
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
            double totalPrice = product.getFinalPrice() * quantity;
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
            
            String priceText = product.getDiscount() > 0 ? 
                formatPrice(product.getFinalPrice()) + " (Giảm " + String.format("%.0f", product.getDiscount()) + "%)" :
                formatPrice(product.getPrice());
            
            String shareMessage = "Xem sản phẩm tuyệt vời này: " + product.getName() + 
                                "\nGiá: " + priceText +
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
            // Show loading
            btnBuyNow.setEnabled(false);
            btnBuyNow.setText("Đang xử lý...");
            
            // Add to cart first and get cart item ID
            long cartId = dbHelper.addToCart(userId, product.getId(), quantity);
            
            if (cartId != -1) {
                // Navigate directly to checkout with this cart item
                ArrayList<Integer> selectedCartIds = new ArrayList<>();
                selectedCartIds.add((int) cartId);
                
                Intent intent = new Intent(this, CheckoutActivity.class);
                intent.putIntegerArrayListExtra("selected_cart_ids", selectedCartIds);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                
                // Reset quantity for next purchase
                quantity = 1;
                updateQuantityAndTotal();
            } else {
                Toast.makeText(this, "❌ Lỗi khi xử lý đơn hàng", Toast.LENGTH_SHORT).show();
            }
            
            // Restore button
            btnBuyNow.setEnabled(true);
            btnBuyNow.setText("Mua ngay");
        } else {
            Toast.makeText(this, "❌ Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    private void loadReviews() {
        if (product == null) return;
        
        java.util.List<com.example.ecommerceapp.models.Review> reviews = 
            dbHelper.getProductReviews(product.getId());
        
        // Update review count
        int reviewCount = reviews.size();
        if (reviewCount > 0) {
            tvReviewCount.setText("(" + reviewCount + " đánh giá)");
        } else {
            tvReviewCount.setText("(Chưa có đánh giá)");
        }
        
        // Update average rating
        double avgRating = product.getRating();
        if (avgRating > 0) {
            tvAverageRating.setText(String.format("%.1f", avgRating));
            ratingBarAverage.setRating((float) avgRating);
        } else {
            tvAverageRating.setText("0.0");
            ratingBarAverage.setRating(0);
        }
        
        // Show/hide reviews list
        if (reviews.isEmpty()) {
            rvReviews.setVisibility(View.GONE);
            layoutEmptyReviews.setVisibility(View.VISIBLE);
        } else {
            rvReviews.setVisibility(View.VISIBLE);
            layoutEmptyReviews.setVisibility(View.GONE);
            reviewAdapter.updateReviews(reviews);
        }
    }

    private void loadRelatedProducts() {
        if (product == null || product.getCategory() == null || product.getCategory().isEmpty()) {
            cardRelatedProducts.setVisibility(View.GONE);
            return;
        }
        
        // Get all products and filter by same category
        java.util.List<Product> allProducts = dbHelper.getAllProducts();
        java.util.List<Product> relatedProducts = new java.util.ArrayList<>();
        
        for (Product p : allProducts) {
            if (p.getId() != product.getId() && 
                p.getCategory() != null && 
                p.getCategory().equals(product.getCategory())) {
                relatedProducts.add(p);
            }
        }
        
        if (relatedProducts.isEmpty()) {
            cardRelatedProducts.setVisibility(View.GONE);
            return;
        }
        
        // Limit to 6 products
        if (relatedProducts.size() > 6) {
            relatedProducts = relatedProducts.subList(0, 6);
        }
        
        cardRelatedProducts.setVisibility(View.VISIBLE);
        tvNoRelatedProducts.setVisibility(View.GONE);
        
        // Setup horizontal RecyclerView
        androidx.recyclerview.widget.LinearLayoutManager layoutManager = 
            new androidx.recyclerview.widget.LinearLayoutManager(this, 
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false);
        rvRelatedProducts.setLayoutManager(layoutManager);
        
        relatedProductsAdapter = new com.example.ecommerceapp.adapters.ProductAdapter(
            this, relatedProducts, product1 -> {
                // Click listener for related product
                Intent intent = new Intent(ProductDetailActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product1.getId());
                startActivity(intent);
                finish(); // Finish current activity to reload with new product
            });
        rvRelatedProducts.setAdapter(relatedProductsAdapter);
    }

    private void showWriteReviewDialog() {
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá sản phẩm", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        
        // Check if user already reviewed this product
        if (dbHelper.hasUserReviewedProduct(userId, product.getId())) {
            Toast.makeText(this, "Bạn đã đánh giá sản phẩm này rồi", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if user has purchased this product
        if (!dbHelper.hasUserPurchasedProduct(userId, product.getId())) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Bạn cần mua và nhận sản phẩm này trước khi có thể đánh giá")
                .setPositiveButton("OK", null)
                .show();
            return;
        }
        
        // Show review dialog
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_write_review);
        dialog.getWindow().setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawable(
            new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        );
        
        android.widget.RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        TextView tvRatingText = dialog.findViewById(R.id.tvRatingText);
        android.widget.EditText etComment = dialog.findViewById(R.id.etComment);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnSubmit = dialog.findViewById(R.id.btnSubmit);
        
        // Update rating text when rating changes
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (rating == 0) {
                tvRatingText.setText("");
            } else if (rating == 1) {
                tvRatingText.setText("Rất tệ");
            } else if (rating == 2) {
                tvRatingText.setText("Tệ");
            } else if (rating == 3) {
                tvRatingText.setText("Bình thường");
            } else if (rating == 4) {
                tvRatingText.setText("Tốt");
            } else if (rating == 5) {
                tvRatingText.setText("Tuyệt vời");
            }
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString().trim();
            
            if (rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (comment.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
                return;
            }
            
            long result = dbHelper.addReview(product.getId(), userId, rating, comment);
            if (result != -1) {
                Toast.makeText(this, "✅ Cảm ơn bạn đã đánh giá", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                
                // Reload product để lấy rating mới (đã được update trong addReview)
                product = dbHelper.getProductById(product.getId());
                if (product != null) {
                    // Update rating display in header
                    int reviewCount = dbHelper.getReviewCount(product.getId());
                    tvRating.setText(String.format("%.1f", product.getRating()));
                    tvRatingCount.setText("(" + reviewCount + " đánh giá)");
                    ratingBarHeader.setRating((float) product.getRating());
                    
                    // Update rating display in review section
                    tvAverageRating.setText(String.format("%.1f", product.getRating()));
                    ratingBarAverage.setRating((float) product.getRating());
                }
                
                // Reload reviews list
                loadReviews();
            } else {
                Toast.makeText(this, "❌ Lỗi khi gửi đánh giá", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}