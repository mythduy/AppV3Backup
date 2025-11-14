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
    private TextView tvFullName, tvEmail, tvAdminSection;
    private ImageView ivAvatar, btnEditProfile;
    private CardView cardAdminMenu;
    private LinearLayout btnAccountInfo, btnAddress, btnPayment;
    private LinearLayout btnManageProducts, btnManageCategories, btnManageOrders, btnManageUsers;
    private Button btnLogout;
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
        
        tvAdminSection = findViewById(R.id.tvAdminSection);
        cardAdminMenu = findViewById(R.id.cardAdminMenu);
        
        btnAccountInfo = findViewById(R.id.btnAccountInfo);
        btnAddress = findViewById(R.id.btnAddress);
        btnPayment = findViewById(R.id.btnPayment);
        
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
            
            // Load avatar
            String avatarUrl = currentUser.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this)
                        .load(avatarUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .into(ivAvatar);
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
        }
    }

    private void setupClickListeners() {
        // Avatar click - open edit profile
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
            });
        }
        
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        if (btnAccountInfo != null) {
            btnAccountInfo.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        if (btnAddress != null) {
            btnAddress.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnPayment != null) {
            btnPayment.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
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
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user info when returning to this activity
        currentUser = dbHelper.getUserById(userId);
        loadUserInfo();
    }
}
