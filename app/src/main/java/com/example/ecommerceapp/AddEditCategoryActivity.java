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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.utils.CategoryImageManager;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.InputStream;

public class AddEditCategoryActivity extends AppCompatActivity {
    private EditText etCategoryName;
    private ImageView ivCategoryPreview;
    private MaterialButton btnPickFromGallery, btnTakePhoto;
    private Button btnSave;
    private Toolbar toolbar;
    
    private DatabaseHelper dbHelper;
    private CategoryImageManager imageManager;
    private String categoryName;
    private String selectedImagePath;
    private Bitmap selectedBitmap; // Store bitmap directly
    private boolean isEditMode = false;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_category);

        dbHelper = new DatabaseHelper(this);
        imageManager = new CategoryImageManager(this);
        
        setupActivityResultLaunchers();
        initViews();
        setupToolbar();
        setupListeners();
        
        // Check if editing
        categoryName = getIntent().getStringExtra("category_name");
        if (categoryName != null && !categoryName.isEmpty()) {
            isEditMode = true;
            loadCategoryData();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etCategoryName = findViewById(R.id.etCategoryName);
        ivCategoryPreview = findViewById(R.id.ivCategoryPreview);
        btnPickFromGallery = findViewById(R.id.btnPickFromGallery);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Chỉnh sửa danh mục" : "Thêm danh mục");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupActivityResultLaunchers() {
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
        
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.os.Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            handleCameraImage(imageBitmap);
                        }
                    }
                }
            }
        );
        
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

    private void setupListeners() {
        btnPickFromGallery.setOnClickListener(v -> openGallery());
        btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndOpen());
        btnSave.setOnClickListener(v -> saveCategory());
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
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            // Store bitmap directly
            selectedBitmap = bitmap;
            ivCategoryPreview.setImageBitmap(bitmap);
            
            Toast.makeText(this, "✅ Đã chọn ảnh thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Lỗi khi chọn ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCameraImage(Bitmap bitmap) {
        // Store bitmap directly
        selectedBitmap = bitmap;
        ivCategoryPreview.setImageBitmap(bitmap);
        Toast.makeText(this, "✅ Đã chụp ảnh thành công", Toast.LENGTH_SHORT).show();
    }

    private void loadCategoryData() {
        etCategoryName.setText(categoryName);
        etCategoryName.setEnabled(false); // Don't allow changing category name
        
        // Load existing image with cache busting
        String imagePath = imageManager.getCategoryImagePath(categoryName);
        if (imagePath != null && new File(imagePath).exists()) {
            File imageFile = new File(imagePath);
            Glide.with(this)
                .load(imageFile)
                .signature(new ObjectKey(imageFile.lastModified()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_category_default)
                .into(ivCategoryPreview);
            selectedImagePath = imagePath;
        }
    }

    private void saveCategory() {
        String newCategoryName = etCategoryName.getText().toString().trim();
        
        if (newCategoryName.isEmpty()) {
            etCategoryName.setError("Vui lòng nhập tên danh mục");
            etCategoryName.requestFocus();
            return;
        }

        // Save image if selected
        if (selectedBitmap != null) {
            String imagePath = imageManager.saveCategoryImage(
                isEditMode ? categoryName : newCategoryName, 
                selectedBitmap
            );
            
            if (imagePath != null) {
                selectedImagePath = imagePath;
                Toast.makeText(this, "✅ Đã lưu ảnh thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        boolean success;
        if (isEditMode) {
            // Just update image, category name stays the same
            if (selectedBitmap != null) {
                Toast.makeText(this, "✅ Đã cập nhật ảnh danh mục", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Vui lòng chọn ảnh mới", Toast.LENGTH_SHORT).show();
                return;
            }
            success = true;
        } else {
            // Add new category
            success = dbHelper.addCategory(newCategoryName);
            if (success) {
                Toast.makeText(this, "✅ Đã thêm danh mục", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Danh mục đã tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (success) {
            finish();
        }
    }
}
