package com.example.ecommerceapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.ecommerceapp.utils.LogUtil;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import android.app.DatePickerDialog;
import java.util.Calendar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {
    private com.google.android.material.imageview.ShapeableImageView ivAvatar;
    private FloatingActionButton fabChangePhoto;
    private EditText etFullName, etBio, etEmail, etPhone, etDateOfBirth;
    private RadioGroup rgGender;
    private com.google.android.material.button.MaterialButton btnSave;
    private com.google.android.material.appbar.MaterialToolbar toolbar;
    private DatabaseHelper dbHelper;
    private int userId;
    private String avatarUrl;

    // Activity Result Launchers
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        initializeViews();
        setupActivityResultLaunchers();
        setupToolbar();
        loadUserInfo();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        ivAvatar = findViewById(R.id.ivAvatar);
        fabChangePhoto = findViewById(R.id.fabChangePhoto);
        etFullName = findViewById(R.id.etFullName);
        etBio = findViewById(R.id.etBio);
        rgGender = findViewById(R.id.rgGender);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupActivityResultLaunchers() {
        // Permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "Cần quyền truy cập ảnh để thay đổi avatar", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Image picker launcher
        pickImageLauncher = registerForActivityResult(
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
    }
    
    private void handleImageSelected(Uri imageUri) {
        try {
            // Read image from URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            // Save to internal storage
            avatarUrl = saveAvatarToInternalStorage(bitmap);
            
            // Display image
            Glide.with(this)
                    .load(new File(avatarUrl))
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(ivAvatar);
            
            Toast.makeText(this, "✅ Đã chọn ảnh thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            LogUtil.e("EditProfile", "Error selecting image", e);
            Toast.makeText(this, "❌ Lỗi khi chọn ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private String saveAvatarToInternalStorage(Bitmap bitmap) {
        try {
            // Create avatars directory
            File directory = new File(getFilesDir(), "user_avatars");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Delete old avatar if exists
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                File oldFile = new File(avatarUrl);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
            
            // Save new avatar
            String filename = "avatar_user_" + userId + ".jpg";
            File file = new File(directory, filename);
            
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            
            return file.getAbsolutePath();
        } catch (Exception e) {
            LogUtil.e("EditProfile", "Error saving avatar to storage", e);
            return null;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        fabChangePhoto.setOnClickListener(v -> checkPermissionAndPickImage());
        btnSave.setOnClickListener(v -> updateProfile());
        etDateOfBirth.setOnClickListener(v -> showDatePicker());
    }
    
    private void showDatePicker() {
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                String date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                etDateOfBirth.setText(date);
            },
            2000, 0, 1
        );
        datePickerDialog.show();
    }

    private void checkPermissionAndPickImage() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void loadUserInfo() {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            etFullName.setText(user.getFullName());
            etBio.setText(user.getBio());
            etEmail.setText(user.getEmail());
            etPhone.setText(user.getPhone());
            etDateOfBirth.setText(user.getDateOfBirth());
            
            // Set gender
            String gender = user.getGender();
            if ("Male".equals(gender)) {
                rgGender.check(R.id.rbMale);
            } else if ("Female".equals(gender)) {
                rgGender.check(R.id.rbFemale);
            } else {
                rgGender.check(R.id.rbOther);
            }
            
            avatarUrl = user.getAvatarUrl();

            // Load avatar from file
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                File avatarFile = new File(avatarUrl);
                if (avatarFile.exists()) {
                    Glide.with(this)
                            .load(avatarFile)
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            }
        }
    }

    private void updateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        String gender;
        if (selectedGenderId == R.id.rbMale) {
            gender = "Male";
        } else if (selectedGenderId == R.id.rbFemale) {
            gender = "Female";
        } else {
            gender = "Other";
        }

        User user = new User();
        user.setId(userId);
        user.setFullName(fullName);
        user.setBio(bio);
        user.setGender(gender);
        user.setDateOfBirth(dateOfBirth);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAvatarUrl(avatarUrl);

        boolean result = dbHelper.updateUser(user);

        if (result) {
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}
