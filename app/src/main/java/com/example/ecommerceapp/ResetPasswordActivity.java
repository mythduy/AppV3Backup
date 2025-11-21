package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.utils.AppConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends AppCompatActivity {
    private TextInputEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnResetPassword;
    private DatabaseHelper dbHelper;
    
    private int userId;
    private String email;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        
        dbHelper = new DatabaseHelper(this);
        
        userId = getIntent().getIntExtra("userId", -1);
        email = getIntent().getStringExtra("email");
        
        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không xác định được user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupClickListeners();
    }
    
    private void initViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
    }
    
    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(v -> resetPassword());
        
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }
    
    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // Validation
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!newPassword.matches(AppConstants.PASSWORD_PATTERN)) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất " + AppConstants.MIN_PASSWORD_LENGTH + " ký tự, bao gồm chữ HOA, chữ thường và số", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Đang xử lý...");
        
        // Update password
        boolean success = dbHelper.updateUserPassword(userId, newPassword);
        
        if (success) {
            Toast.makeText(this, "✅ Đặt lại mật khẩu thành công!", Toast.LENGTH_LONG).show();
            
            // Show success dialog
            new android.app.AlertDialog.Builder(this)
                .setTitle("🎉 Thành công!")
                .setMessage("Mật khẩu của bạn đã được đặt lại thành công.\n\nVui lòng đăng nhập bằng mật khẩu mới.")
                .setPositiveButton("Đăng nhập ngay", (dialog, which) -> {
                    // Go back to login screen
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .setCancelable(false)
                .show();
        } else {
            btnResetPassword.setEnabled(true);
            btnResetPassword.setText("Đặt lại mật khẩu");
            Toast.makeText(this, "❌ Lỗi khi đặt lại mật khẩu. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }
}
