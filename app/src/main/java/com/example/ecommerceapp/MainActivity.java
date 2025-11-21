package com.example.ecommerceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.ecommerceapp.adapters.BannerAdapter;
import com.example.ecommerceapp.adapters.CategoryAdapter;
import com.example.ecommerceapp.adapters.ProductAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import com.example.ecommerceapp.utils.LogUtil;
import com.example.ecommerceapp.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private SearchView searchView;
    private android.widget.ImageView ivAppLogo;
    private com.google.android.material.imageview.ShapeableImageView ivProfile;
    private TextView tvCartBadge;
    private ViewPager2 vpBanner;
    private TabLayout tabLayout;
    private RecyclerView rvCategories, rvProducts;
    private BottomNavigationView bottomNav;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private DatabaseHelper dbHelper;
    private int userId;
    private int cartCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userId = prefs.getInt("user_id", -1);

            // Không yêu cầu đăng nhập ngay, cho phép xem sản phẩm
            
            // XÓA DATABASE CŨ VÀ TẠO LẠI - CHỈ CHẠY 1 LẦN
            // Sau khi chạy app 1 lần thành công, hãy comment lại 2 dòng này
            // IMPORTANT: ĐÃ COMMENT LẠI ĐỂ KHÔNG MẤT DATA
            // this.deleteDatabase("ecommerce.db");
            
            dbHelper = new DatabaseHelper(this);
            // CHỈ CHẠY 1 LẦN ĐỂ CẬP NHẬT HÌNH ẢNH
            // dbHelper.updateProductImages();
            
            // CHỈ CHẠY 1 LẦN để copy ảnh từ assets sang internal storage
            // Sau khi chạy xong, comment lại dòng này
            //dbHelper.migrateProductImagesFromAssets();
            
            // Migrate category images from assets (chỉ chạy 1 lần)
            new com.example.ecommerceapp.utils.CategoryImageManager(this).migrateCategoryImagesFromAssets();

            initViews();
            setupToolbar();
            setupBanner();
            setupTabs();
            setupCategories();
            setupProducts();
            setupBottomNavigation();

            loadProducts("all");
        } catch (Exception e) {
            LogUtil.e("MainActivity", "Error initializing MainActivity", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.searchView);
        ivAppLogo = findViewById(R.id.ivAppLogo);
        ivProfile = findViewById(R.id.ivProfile);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        vpBanner = findViewById(R.id.vpBanner);
        tabLayout = findViewById(R.id.tabLayout);
        rvCategories = findViewById(R.id.rvCategories);
        rvProducts = findViewById(R.id.rvProducts);
        bottomNav = findViewById(R.id.bottomNav);
        
        // Setup "Xem tất cả" button
        findViewById(R.id.tvViewAllCategories).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        
        // Setup app logo click - refresh trang
        ivAppLogo.setOnClickListener(v -> {
            refreshPage();
        });
        
        // Setup profile avatar click
        ivProfile.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(this, "Vui lòng đăng nhập 🔐", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                // Hiển thị menu với giỏ hàng và profile
                showProfileMenu();
            }
        });
        
        // Setup search view
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadProducts("all");
                } else {
                    searchProducts(newText);
                }
                return true;
            }
        });
    }
    
    private void refreshPage() {
        // Clear search
        searchView.setQuery("", false);
        searchView.clearFocus();
        
        // Reset tab về đầu tiên
        if (tabLayout.getTabCount() > 0) {
            TabLayout.Tab firstTab = tabLayout.getTabAt(0);
            if (firstTab != null) {
                firstTab.select();
            }
        }
        
        // Reload products
        loadProducts("new");
        
        // Scroll to top
        if (rvProducts != null) {
            rvProducts.smoothScrollToPosition(0);
        }
        
        // Show feedback
        Toast.makeText(this, "Đã làm mới 🔄", Toast.LENGTH_SHORT).show();
    }
    
    private void showProfileMenu() {
        // Tạo popup menu khi click vào avatar
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, ivProfile);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.action_cart) {
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (id == R.id.action_orders) {
                Intent intent = new Intent(this, OrderHistoryActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (id == R.id.action_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            
            return false;
        });
        
        popup.show();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }

    private void setupBanner() {
        List<Integer> bannerImages = new ArrayList<>();
        bannerImages.add(R.drawable.banner1);
        bannerImages.add(R.drawable.banner2);
        bannerImages.add(R.drawable.banner3);

        BannerAdapter bannerAdapter = new BannerAdapter(bannerImages);
        vpBanner.setAdapter(bannerAdapter);

        // Auto scroll banner
        vpBanner.postDelayed(new Runnable() {
            @Override
            public void run() {
                int nextItem = (vpBanner.getCurrentItem() + 1) % bannerImages.size();
                vpBanner.setCurrentItem(nextItem, true);
                vpBanner.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Sản phẩm mới"));
        tabLayout.addTab(tabLayout.newTab().setText("Nổi bật"));
        tabLayout.addTab(tabLayout.newTab().setText("Bán chạy"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadProducts("new");
                        break;
                    case 1:
                        loadProducts("featured");
                        break;
                    case 2:
                        loadProducts("bestseller");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupCategories() {
        rvCategories.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(
                this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));

        List<String> categories = dbHelper.getAllCategories();
        categoryAdapter = new CategoryAdapter(this, categories, category -> {
            loadProductsByCategory(category);
            tabLayout.selectTab(null);
        });
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupProducts() {
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, new ArrayList<>(), product -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        
        // Đăng ký listener để cập nhật badge giỏ hàng ngay lập tức
        productAdapter.setOnCartUpdateListener(() -> updateCartBadge());
        
        rvProducts.setAdapter(productAdapter);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_categories) {
                Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (id == R.id.nav_cart) {
                // Kiểm tra đăng nhập khi vào giỏ hàng
                if (userId == -1) {
                    Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    Intent intent = new Intent(MainActivity.this, CartActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                return true;
            } else if (id == R.id.nav_profile) {
                // Kiểm tra đăng nhập khi vào trang cá nhân
                if (userId == -1) {
                    Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                return true;
            }

            return false;
        });

        updateCartBadge();
    }

    private void loadProducts(String type) {
        List<Product> products;
        switch (type) {
            case "new":
                products = dbHelper.getLatestProducts(10);
                break;
            case "featured":
                products = dbHelper.getFeaturedProducts(10);
                break;
            case "bestseller":
                products = dbHelper.getBestsellerProducts(10);
                break;
            default:
                products = dbHelper.getAllProducts();
        }
        productAdapter.updateProducts(products);
    }

    // this.deleteDatabase("ecommerce.db");
    // dbHelper.updateProductImages();

    private void loadProductsByCategory(String category) {
        List<Product> products = dbHelper.getProductsByCategory(category);
        productAdapter.updateProducts(products);
    }

    private void updateCartBadge() {
        if (userId != -1) {
            cartCount = dbHelper.getCartItemCount(userId); // Sử dụng method mới đếm số sản phẩm, không phải tổng quantity
            
            // Update badge in bottom navigation
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_cart);
            if (cartCount > 0) {
                badge.setVisible(true);
                badge.setNumber(cartCount);
                badge.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorRed));
            } else {
                badge.setVisible(false);
            }
            
            // Update badge in toolbar
            if (cartCount > 0) {
                tvCartBadge.setText(String.valueOf(cartCount));
                tvCartBadge.setVisibility(View.VISIBLE);
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // No menu needed - using custom toolbar buttons
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // No menu items - using custom toolbar buttons
        return super.onOptionsItemSelected(item);
    }

    private void searchProducts(String query) {
        List<Product> products = dbHelper.searchProducts(query);
        productAdapter.updateProducts(products);
        tabLayout.selectTab(null);
    }

    private void loadUserAvatar() {
        if (userId != -1) {
            User user = dbHelper.getUserById(userId);
            if (user != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                File avatarFile = new File(user.getAvatarUrl());
                if (avatarFile.exists()) {
                    Glide.with(this)
                            .load(avatarFile)
                            .circleCrop()
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .into(ivProfile);
                } else {
                    ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            } else {
                ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
            }
        } else {
            ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Cập nhật lại userId khi quay lại (sau khi đăng nhập)
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        
        // Load avatar mới nhất
        loadUserAvatar();
        
        updateCartBadge();
    }
}