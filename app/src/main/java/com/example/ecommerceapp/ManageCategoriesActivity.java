package com.example.ecommerceapp;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.AdminCategoryAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity {
    private RecyclerView rvCategories;
    private AdminCategoryAdapter adapter;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;
    private Toolbar toolbar;
    private TextView tvTotalCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        dbHelper = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        rvCategories = findViewById(R.id.rvCategories);
        fabAdd = findViewById(R.id.fabAdd);
        tvTotalCategories = findViewById(R.id.tvTotalCategories);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý danh mục");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupRecyclerView();
        loadCategories();

        fabAdd.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void setupRecyclerView() {
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCategoryAdapter(this, new AdminCategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(String category) {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDelete(String category) {
                showDeleteConfirmDialog(category);
            }
        });
        rvCategories.setAdapter(adapter);
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm danh mục mới");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Tên danh mục");
        input.setPadding(50, 40, 50, 40);
        builder.setView(input);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String categoryName = input.getText().toString().trim();
            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.addCategory(categoryName);
            if (success) {
                Toast.makeText(this, "Đã thêm danh mục", Toast.LENGTH_SHORT).show();
                loadCategories();
            } else {
                Toast.makeText(this, "Danh mục đã tồn tại", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showEditCategoryDialog(String oldCategory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa danh mục");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(oldCategory);
        input.setPadding(50, 40, 50, 40);
        input.setSelection(oldCategory.length());
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newCategory = input.getText().toString().trim();
            if (newCategory.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newCategory.equals(oldCategory)) {
                return;
            }

            boolean success = dbHelper.updateCategory(oldCategory, newCategory);
            if (success) {
                Toast.makeText(this, "Đã cập nhật danh mục", Toast.LENGTH_SHORT).show();
                loadCategories();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật danh mục", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showDeleteConfirmDialog(String category) {
        // Check if category has products
        int productCount = dbHelper.getProductCountByCategory(category);
        
        String message = productCount > 0 
            ? "Danh mục này có " + productCount + " sản phẩm. Xóa danh mục sẽ xóa tất cả sản phẩm trong danh mục này. Bạn có chắc chắn muốn xóa?"
            : "Bạn có chắc muốn xóa danh mục \"" + category + "\" không?";

        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage(message)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean success = dbHelper.deleteCategory(category);
                    if (success) {
                        Toast.makeText(this, "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa danh mục", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadCategories() {
        List<String> categories = dbHelper.getAllCategories();
        adapter.updateCategories(categories);
        tvTotalCategories.setText(String.valueOf(categories.size()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }
}
