package com.example.ecommerceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvFullName, tvEmail, tvAdminSection, tvWishlistCount;
    private TextView tvPendingCount, tvShippingCount, tvCompletedCount, tvAllOrdersCount;
    private com.google.android.material.imageview.ShapeableImageView ivAvatar;
    private com.google.android.material.button.MaterialButton btnEditProfile, btnLogout;
    private com.google.android.material.card.MaterialCardView cardAdminMenu;
    private LinearLayout btnAccountInfo, btnWishlist, btnOrderHistory;
    private LinearLayout btnPendingOrders, btnShippingOrders, btnCompletedOrders, btnAllOrders;
    private LinearLayout btnManageProducts, btnManageCategories, btnManageOrders, btnManageUsers;
    private BottomNavigationView bottomNav;
    private DatabaseHelper dbHelper;
    private User currentUser;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_profile);

            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userId = prefs.getInt("user_id", -1);

            // Check if user is logged in
            if (userId == -1) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            dbHelper = new DatabaseHelper(this);
            currentUser = dbHelper.getUserById(userId);

            // Check if user exists in database
            if (currentUser == null) {
                Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            initViews();
            setupBottomNavigation();
            loadUserInfo();
            setupClickListeners();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        bottomNav = findViewById(R.id.bottomNav);
        
        // Order stats
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvShippingCount = findViewById(R.id.tvShippingCount);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        tvAllOrdersCount = findViewById(R.id.tvAllOrdersCount);
        btnPendingOrders = findViewById(R.id.btnPendingOrders);
        btnShippingOrders = findViewById(R.id.btnShippingOrders);
        btnCompletedOrders = findViewById(R.id.btnCompletedOrders);
        btnAllOrders = findViewById(R.id.btnAllOrders);
        
        tvAdminSection = findViewById(R.id.tvAdminSection);
        cardAdminMenu = findViewById(R.id.cardAdminMenu);
        
        btnAccountInfo = findViewById(R.id.btnAccountInfo);
        btnWishlist = findViewById(R.id.btnWishlist);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        tvWishlistCount = findViewById(R.id.tvWishlistCount);
        
        btnManageProducts = findViewById(R.id.btnManageProducts);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } else if (id == R.id.nav_categories) {
                Intent intent = new Intent(ProfileActivity.this, CategoriesActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } else if (id == R.id.nav_cart) {
                Intent intent = new Intent(ProfileActivity.this, CartActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }

            return false;
        });
    }

    private void loadUserInfo() {
        if (currentUser != null) {
            tvFullName.setText(currentUser.getFullName());
            tvEmail.setText(currentUser.getEmail());
            
            // Load avatar from file
            String avatarUrl = currentUser.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                java.io.File avatarFile = new java.io.File(avatarUrl);
                if (avatarFile.exists()) {
                    Glide.with(this)
                            .load(avatarFile)
                            .circleCrop()
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            } else {
                ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
            }
            
            // Show admin menu if user is admin
            if (currentUser.isAdmin()) {
                if (tvAdminSection != null) {
                    tvAdminSection.setVisibility(View.VISIBLE);
                }
                if (cardAdminMenu != null) {
                    cardAdminMenu.setVisibility(View.VISIBLE);
                }
            }
            
            // Update wishlist count
            int wishlistCount = dbHelper.getWishlistCount(userId);
            if (tvWishlistCount != null) {
                tvWishlistCount.setText(String.valueOf(wishlistCount));
            }
            
            // Load order statistics
            loadOrderStats();
        }
    }
    
    private void loadOrderStats() {
        java.util.List<com.example.ecommerceapp.models.Order> allOrders = dbHelper.getOrderHistory(userId);
        
        int pendingCount = 0;
        int shippingCount = 0;
        int completedCount = 0;
        
        for (com.example.ecommerceapp.models.Order order : allOrders) {
            String status = order.getStatus();
            if ("Chờ xác nhận".equals(status)) {
                pendingCount++;
            } else if ("Đang giao hàng".equals(status)) {
                shippingCount++;
            } else if ("Hoàn thành".equals(status)) {
                completedCount++;
            }
        }
        
        if (tvPendingCount != null) tvPendingCount.setText(String.valueOf(pendingCount));
        if (tvShippingCount != null) tvShippingCount.setText(String.valueOf(shippingCount));
        if (tvCompletedCount != null) tvCompletedCount.setText(String.valueOf(completedCount));
        if (tvAllOrdersCount != null) tvAllOrdersCount.setText(String.valueOf(allOrders.size()));
    }

    private void setupClickListeners() {
        // Avatar and edit profile clicks
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
        
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Order stats clicks
        if (btnPendingOrders != null) {
            btnPendingOrders.setOnClickListener(v -> openOrderHistory("Chờ xác nhận"));
        }
        
        if (btnShippingOrders != null) {
            btnShippingOrders.setOnClickListener(v -> openOrderHistory("Đang giao hàng"));
        }
        
        if (btnCompletedOrders != null) {
            btnCompletedOrders.setOnClickListener(v -> openOrderHistory("Hoàn thành"));
        }
        
        if (btnAllOrders != null) {
            btnAllOrders.setOnClickListener(v -> openOrderHistory("all"));
        }

        // Account settings clicks
        if (btnAccountInfo != null) {
            btnAccountInfo.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        if (btnWishlist != null) {
            btnWishlist.setOnClickListener(v -> {
                Intent intent = new Intent(this, WishlistActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
        
        if (btnOrderHistory != null) {
            btnOrderHistory.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrderHistoryActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Admin menu clicks - only set if views exist
        if (btnManageProducts != null) {
            btnManageProducts.setOnClickListener(v -> {
                Intent intent = new Intent(this, ManageProductsActivity.class);
                startActivity(intent);
            });
        }

        if (btnManageCategories != null) {
            btnManageCategories.setOnClickListener(v -> {
                Intent intent = new Intent(this, ManageCategoriesActivity.class);
                startActivity(intent);
            });
        }

        if (btnManageOrders != null) {
            btnManageOrders.setOnClickListener(v -> {
                Intent intent = new Intent(this, ManageOrdersActivity.class);
                startActivity(intent);
            });
        }

        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> {
                Intent intent = new Intent(this, ManageUsersActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                // Show confirmation dialog
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.clear();
                        editor.apply();
                        
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            });
        }
    }
    
    private void openOrderHistory(String filter) {
        Intent intent = new Intent(this, OrderHistoryActivity.class);
        intent.putExtra("filter", filter);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user info when returning to this activity
        currentUser = dbHelper.getUserById(userId);
        loadUserInfo();
    }
}
