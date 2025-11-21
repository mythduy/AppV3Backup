package com.example.ecommerceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.User;
import com.example.ecommerceapp.utils.AppConstants;
import com.example.ecommerceapp.utils.LogUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private MaterialCheckBox cbRememberMe;
    private MaterialCardView btnGoogleLogin, btnFacebookLogin;
    private CardView loginCard;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        // Initialize Material Components
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin);

        // Apply animations
        applyAnimations();

        // Set up click listeners
        btnLogin.setOnClickListener(v -> login());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnGoogleLogin.setOnClickListener(v -> {
            loginWithGoogle();
        });

        btnFacebookLogin.setOnClickListener(v -> {
            loginWithFacebook();
        });
    }

    private void applyAnimations() {
        // Animate card entrance
        View headerSection = findViewById(R.id.headerSection);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        headerSection.startAnimation(fadeIn);
    }

    private void login() {
        String usernameOrEmail = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            // Shake animation for error
            Animation shake = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            btnLogin.startAnimation(shake);
            return;
        }

        // Show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        LogUtil.d(AppConstants.TAG_LOGIN, "Attempting login with: " + usernameOrEmail);
        
        User user = dbHelper.loginUser(usernameOrEmail, password);
        
        LogUtil.d(AppConstants.TAG_LOGIN, "Login result: " + (user != null ? "SUCCESS - User: " + user.getUsername() : "FAILED"));

        if (user != null) {
            // Save user session with remember me option
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("user_id", user.getId());
            editor.putString("username", user.getUsername());
            editor.putBoolean("remember_me", cbRememberMe.isChecked());
            editor.apply();

            Toast.makeText(this, "Đăng nhập thành công! 🎉", Toast.LENGTH_SHORT).show();
            
            // Navigate to MainActivity with smooth transition
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setText("Đăng nhập");
            Toast.makeText(this, "❌ Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            
            // Shake animation for error feedback
            Animation shake = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            etPassword.startAnimation(shake);
            etPassword.setText("");
        }
    }

    private void loginWithGoogle() {
        // Demo: Tạo/đăng nhập tài khoản Google tự động
        Toast.makeText(this, "Đang đăng nhập với Google... 🔍", Toast.LENGTH_SHORT).show();
        
        // Tạo tài khoản demo với Google
        String googleUsername = "google_user_" + System.currentTimeMillis();
        String googleEmail = googleUsername + "@gmail.com";
        
        // Kiểm tra nếu đã có tài khoản Google demo
        User existingUser = dbHelper.loginUser("google_demo", "google123");
        
        if (existingUser == null) {
            // Tạo tài khoản mới
            User newUser = new User();
            newUser.setUsername("google_demo");
            newUser.setEmail("demo@gmail.com");
            newUser.setPassword("google123");
            newUser.setFullName("Google User");
            newUser.setPhone("0900000001");
            newUser.setAddress("Google Account");
            newUser.setRole("user");
            
            long userId = dbHelper.registerUser(newUser);
            if (userId != -1) {
                newUser.setId((int)userId);
                loginSuccess(newUser, "Google");
            } else {
                Toast.makeText(this, "Lỗi đăng nhập Google", Toast.LENGTH_SHORT).show();
            }
        } else {
            loginSuccess(existingUser, "Google");
        }
    }

    private void loginWithFacebook() {
        // Demo: Tạo/đăng nhập tài khoản Facebook tự động
        Toast.makeText(this, "Đang đăng nhập với Facebook... 👤", Toast.LENGTH_SHORT).show();
        
        // Kiểm tra nếu đã có tài khoản Facebook demo
        User existingUser = dbHelper.loginUser("facebook_demo", "facebook123");
        
        if (existingUser == null) {
            // Tạo tài khoản mới
            User newUser = new User();
            newUser.setUsername("facebook_demo");
            newUser.setEmail("demo@facebook.com");
            newUser.setPassword("facebook123");
            newUser.setFullName("Facebook User");
            newUser.setPhone("0900000002");
            newUser.setAddress("Facebook Account");
            newUser.setRole("user");
            
            long userId = dbHelper.registerUser(newUser);
            if (userId != -1) {
                newUser.setId((int)userId);
                loginSuccess(newUser, "Facebook");
            } else {
                Toast.makeText(this, "Lỗi đăng nhập Facebook", Toast.LENGTH_SHORT).show();
            }
        } else {
            loginSuccess(existingUser, "Facebook");
        }
    }

    private void loginSuccess(User user, String provider) {
        // Save user session
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("user_id", user.getId());
        editor.putString("username", user.getUsername());
        editor.putBoolean("remember_me", true);
        editor.apply();

        Toast.makeText(this, "Đăng nhập " + provider + " thành công! 🎉", Toast.LENGTH_SHORT).show();
        
        // Navigate to MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}