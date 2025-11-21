package com.example.ecommerceapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.ecommerceapp.utils.LogUtil;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class AddEditProductActivity extends AppCompatActivity {
    private EditText etName, etSku, etDescription, etPrice, etDiscount, etStock, etWarranty;
    private ImageView ivProductPreview;
    private MaterialButton btnPickFromGallery, btnTakePhoto;
    private Spinner spinnerCategory;
    private CheckBox cbIsNew, cbIsHot, cbIsFeatured;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private Product product;
    private boolean isEditMode = false;
    private String selectedImagePath = null;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        dbHelper = new DatabaseHelper(this);
        
        // Initialize activity result launchers
        setupActivityResultLaunchers();

        Toolbar toolbar = findViewById(R.id.toolbar);
        etName = findViewById(R.id.etName);
        etSku = findViewById(R.id.etSku);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etDiscount = findViewById(R.id.etDiscount);
        etStock = findViewById(R.id.etStock);
        etWarranty = findViewById(R.id.etWarranty);
        ivProductPreview = findViewById(R.id.ivProductPreview);
        btnPickFromGallery = findViewById(R.id.btnPickFromGallery);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        cbIsNew = findViewById(R.id.cbIsNew);
        cbIsHot = findViewById(R.id.cbIsHot);
        cbIsFeatured = findViewById(R.id.cbIsFeatured);
        btnSave = findViewById(R.id.btnSave);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupCategorySpinner();
        setupImagePickerListeners();

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

    private void setupActivityResultLaunchers() {
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        handleImageSelected(imageUri);
                    }
                }
            }
        );
        
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            handleCameraImage(imageBitmap);
                        }
                    }
                }
            }
        );
        
        // Permission launcher
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Cần cấp quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void setupImagePickerListeners() {
        btnPickFromGallery.setOnClickListener(v -> openGallery());
        btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndOpen());
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }
    
    private void checkCameraPermissionAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        } else {
            openCamera();
        }
    }
    
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng camera", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void handleImageSelected(Uri imageUri) {
        try {
            // Copy image to internal storage
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            // Save and display
            selectedImagePath = saveImageToInternalStorage(bitmap);
            ivProductPreview.setImageBitmap(bitmap);
            
            Toast.makeText(this, "✅ Đã chọn ảnh thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            LogUtil.e("AddEditProduct", "Error handling gallery image", e);
            Toast.makeText(this, "❌ Lỗi khi chọn ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void handleCameraImage(Bitmap bitmap) {
        try {
            selectedImagePath = saveImageToInternalStorage(bitmap);
            ivProductPreview.setImageBitmap(bitmap);
            Toast.makeText(this, "✅ Đã chụp ảnh thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            LogUtil.e("AddEditProduct", "Error handling camera image", e);
            Toast.makeText(this, "❌ Lỗi khi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            // Create images directory if not exists
            File directory = new File(getFilesDir(), "product_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Generate unique filename
            String filename = "product_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, filename);
            
            // Compress and save
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();
            
            return file.getAbsolutePath();
        } catch (Exception e) {
            LogUtil.e("AddEditProduct", "Error saving image to internal storage", e);
            return null;
        }
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
            etSku.setText(product.getSku());
            etDescription.setText(product.getDescription());
            etPrice.setText(String.valueOf(product.getPrice()));
            etDiscount.setText(String.valueOf(product.getDiscount()));
            etStock.setText(String.valueOf(product.getStock()));
            etWarranty.setText(product.getWarranty());
            
            // Load existing image
            selectedImagePath = product.getImageUrl();
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                File imageFile = new File(selectedImagePath);
                if (imageFile.exists()) {
                    Glide.with(this)
                        .load(imageFile)
                        .placeholder(R.drawable.ic_product_placeholder)
                        .into(ivProductPreview);
                } else {
                    ivProductPreview.setImageResource(R.drawable.ic_product_placeholder);
                }
            }
            
            cbIsNew.setChecked(product.isNew());
            cbIsHot.setChecked(product.isHot());
            cbIsFeatured.setChecked(product.isFeatured());

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
        String sku = etSku.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String discountStr = etDiscount.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String warranty = etWarranty.getText().toString().trim();
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

        double price, discount = 0;
        int stock;
        
        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
            
            if (!discountStr.isEmpty()) {
                discount = Double.parseDouble(discountStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá, số lượng và giảm giá phải là số hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate ranges
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

        if (discount < 0 || discount > 100) {
            etDiscount.setError("Giảm giá phải từ 0-100%");
            etDiscount.requestFocus();
            return;
        }

        boolean success;
        if (isEditMode) {
            product.setName(name);
            product.setSku(sku.isEmpty() ? product.getFormattedSku() : sku);
            product.setDescription(description);
            product.setPrice(price);
            product.setDiscount(discount);
            product.setStock(stock);
            product.setWarranty(warranty.isEmpty() ? "12 tháng" : warranty);
            if (selectedImagePath != null) {
                product.setImageUrl(selectedImagePath);
            }
            product.setCategory(category);
            product.setNew(cbIsNew.isChecked());
            product.setHot(cbIsHot.isChecked());
            product.setFeatured(cbIsFeatured.isChecked());
            
            success = dbHelper.updateProduct(product);
            
            if (success) {
                Toast.makeText(this, "✅ Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "❌ Lỗi khi cập nhật sản phẩm", Toast.LENGTH_SHORT).show();
            }
        } else {
            Product newProduct = new Product();
            newProduct.setName(name);
            newProduct.setSku(sku);
            newProduct.setDescription(description);
            newProduct.setPrice(price);
            newProduct.setDiscount(discount);
            newProduct.setStock(stock);
            newProduct.setRating(0.0); // Rating will be calculated from reviews
            newProduct.setWarranty(warranty.isEmpty() ? "12 tháng" : warranty);
            newProduct.setImageUrl(selectedImagePath != null ? selectedImagePath : "");
            newProduct.setCategory(category);
            newProduct.setNew(cbIsNew.isChecked());
            newProduct.setHot(cbIsHot.isChecked());
            newProduct.setFeatured(cbIsFeatured.isChecked());
            
            long id = dbHelper.addProduct(newProduct);
            
            if (id != -1) {
                Toast.makeText(this, "✅ Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "❌ Lỗi khi thêm sản phẩm", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
