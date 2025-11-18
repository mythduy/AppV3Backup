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
            Toast.makeText(this, "Forgot Password feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnGoogleLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Google Login coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnFacebookLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Facebook Login coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void applyAnimations() {
        // Animate card entrance
        View headerSection = findViewById(R.id.headerSection);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        headerSection.startAnimation(fadeIn);
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            // Shake animation for error
            Animation shake = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            btnLogin.startAnimation(shake);
            return;
        }

        // Show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        User user = dbHelper.loginUser(username, password);

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
}