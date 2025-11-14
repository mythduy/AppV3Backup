package com.example.ecommerceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.ecommerceapp.adapters.BannerAdapter;
import com.example.ecommerceapp.adapters.CategoryAdapter;
import com.example.ecommerceapp.adapters.ProductAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
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
            dbHelper = new DatabaseHelper(this);

            initViews();
            setupToolbar();
            setupBanner();
            setupTabs();
            setupCategories();
            setupProducts();
            setupBottomNavigation();

            loadProducts("all");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
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
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }

    private void setupBanner() {
        List<Integer> bannerImages = new ArrayList<>();
        bannerImages.add(R.drawable.banner_1);
        bannerImages.add(R.drawable.banner_2);
        bannerImages.add(R.drawable.banner_3);

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

    private void loadProductsByCategory(String category) {
        List<Product> products = dbHelper.getProductsByCategory(category);
        productAdapter.updateProducts(products);
    }

    private void updateCartBadge() {
        if (userId != -1) {
            cartCount = dbHelper.getCartItems(userId).size();
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_cart);
            if (cartCount > 0) {
                badge.setVisible(true);
                badge.setNumber(cartCount);
                badge.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorRed));
            } else {
                badge.setVisible(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Tìm sản phẩm...");

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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_cart) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        } else if (id == R.id.action_orders) {
            startActivity(new Intent(this, OrderHistoryActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void searchProducts(String query) {
        List<Product> products = dbHelper.searchProducts(query);
        productAdapter.updateProducts(products);
        tabLayout.selectTab(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Cập nhật lại userId khi quay lại (sau khi đăng nhập)
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        
        updateCartBadge();
    }
}