package com.example.ecommerceapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.User;
import com.example.ecommerceapp.utils.AppConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword,
            etFullName, etPhone, etAddress;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private MaterialCheckBox cbTerms;
    private MaterialCardView btnGoogleSignup, btnFacebookSignup;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        // Initialize Material Components
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        cbTerms = findViewById(R.id.cbTerms);
        btnGoogleSignup = findViewById(R.id.btnGoogleSignup);
        btnFacebookSignup = findViewById(R.id.btnFacebookSignup);

        // Apply animations
        applyAnimations();

        // Add password strength indicator
        addPasswordValidation();

        // Set up click listeners
        btnRegister.setOnClickListener(v -> register());
        
        tvLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnGoogleSignup.setOnClickListener(v -> {
            Toast.makeText(this, "Google Signup coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnFacebookSignup.setOnClickListener(v -> {
            Toast.makeText(this, "Facebook Signup coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void applyAnimations() {
        View headerSection = findViewById(R.id.headerSection);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        headerSection.startAnimation(fadeIn);
    }

    private void addPasswordValidation() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                
                // Get parent TextInputLayout
                View parent = (View) etPassword.getParent();
                if (parent != null) {
                    parent = (View) parent.getParent();
                    if (parent instanceof TextInputLayout) {
                        TextInputLayout passwordLayout = (TextInputLayout) parent;
                        
                        if (password.isEmpty()) {
                            passwordLayout.setError(null);
                            passwordLayout.setHelperText("Ít nhất 8 ký tự, có chữ HOA, chữ thường và số");
                        } else if (password.length() < AppConstants.MIN_PASSWORD_LENGTH) {
                            passwordLayout.setError("Cần thêm " + (AppConstants.MIN_PASSWORD_LENGTH - password.length()) + " ký tự");
                        } else if (!password.matches(".*[a-z].*")) {
                            passwordLayout.setError("Cần có chữ thường (a-z)");
                        } else if (!password.matches(".*[A-Z].*")) {
                            passwordLayout.setError("Cần có chữ HOA (A-Z)");
                        } else if (!password.matches(".*\\d.*")) {
                            passwordLayout.setError("Cần có số (0-9)");
                        } else if (!password.matches(AppConstants.PASSWORD_PATTERN)) {
                            passwordLayout.setError("Mật khẩu chưa đủ mạnh");
                        } else {
                            passwordLayout.setError(null);
                            passwordLayout.setHelperText("✓ Mật khẩu mạnh");
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void register() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validate all fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() ||
                fullName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "⚠️ Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            Animation shake = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            btnRegister.startAnimation(shake);
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "⚠️ Email không hợp lệ", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        // Validate password strength
        if (!password.matches(AppConstants.PASSWORD_PATTERN)) {
            Toast.makeText(this, "⚠️ Mật khẩu phải có ít nhất " + AppConstants.MIN_PASSWORD_LENGTH + " ký tự, bao gồm chữ HOA, chữ thường và số", Toast.LENGTH_LONG).show();
            etPassword.requestFocus();
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "⚠️ Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            Animation shake = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            etConfirmPassword.startAnimation(shake);
            return;
        }

        // Validate terms acceptance
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "⚠️ Vui lòng đồng ý với điều khoản sử dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang đăng ký...");

        // Perform registration
        User user = new User(0, username, email, password, fullName, phone, address);
        long result = dbHelper.registerUser(user);

        if (result != -1) {
            Toast.makeText(this, "✅ Đăng ký thành công! Vui lòng đăng nhập", Toast.LENGTH_LONG).show();
            
            // Smooth transition back to login
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            btnRegister.setEnabled(true);
            btnRegister.setText("Sign Up");
            Toast.makeText(this, "❌ Đăng ký thất bại. Tên đăng nhập hoặc email đã tồn tại",
                    Toast.LENGTH_LONG).show();
            
            // Shake animation for error
            Animation shake = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            btnRegister.startAnimation(shake);
        }
    }
}