package com.example.ecommerceapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import java.util.List;

public class AddEditProductActivity extends AppCompatActivity {
    private EditText etName, etDescription, etPrice, etStock;
    private Spinner spinnerCategory;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private Product product;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etStock = findViewById(R.id.etStock);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupCategorySpinner();

        // Check if editing existing product
        int productId = getIntent().getIntExtra("product_id", -1);
        if (productId != -1) {
            isEditMode = true;
            product = dbHelper.getProductById(productId);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Chỉnh sửa sản phẩm");
            }
            fillProductData();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Thêm sản phẩm mới");
            }
        }

        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void setupCategorySpinner() {
        List<String> categories = dbHelper.getAllCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void fillProductData() {
        if (product != null) {
            etName.setText(product.getName());
            etDescription.setText(product.getDescription());
            etPrice.setText(String.valueOf(product.getPrice()));
            etStock.setText(String.valueOf(product.getStock()));

            // Set category spinner selection
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategory.getAdapter();
            int position = adapter.getPosition(product.getCategory());
            if (position >= 0) {
                spinnerCategory.setSelection(position);
            }
        }
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        // Validation
        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập tên sản phẩm");
            etName.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Vui lòng nhập giá");
            etPrice.requestFocus();
            return;
        }

        if (stockStr.isEmpty()) {
            etStock.setError("Vui lòng nhập số lượng");
            etStock.requestFocus();
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá và số lượng phải là số", Toast.LENGTH_SHORT).show();
            return;
        }

        if (price < 0) {
            etPrice.setError("Giá phải lớn hơn 0");
            etPrice.requestFocus();
            return;
        }

        if (stock < 0) {
            etStock.setError("Số lượng phải lớn hơn hoặc bằng 0");
            etStock.requestFocus();
            return;
        }

        boolean success;
        if (isEditMode) {
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);
            product.setCategory(category);
            success = dbHelper.updateProduct(product);
            
            if (success) {
                Toast.makeText(this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật sản phẩm", Toast.LENGTH_SHORT).show();
            }
        } else {
            Product newProduct = new Product();
            newProduct.setName(name);
            newProduct.setDescription(description);
            newProduct.setPrice(price);
            newProduct.setStock(stock);
            newProduct.setCategory(category);
            newProduct.setImageUrl(""); // Default empty image
            
            long id = dbHelper.addProduct(newProduct);
            
            if (id != -1) {
                Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi thêm sản phẩm", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
