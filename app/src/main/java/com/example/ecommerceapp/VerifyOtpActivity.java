package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.utils.AppConstants;
import com.example.ecommerceapp.utils.LogUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class VerifyOtpActivity extends AppCompatActivity {
    private TextInputEditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private MaterialButton btnVerifyOtp, btnResendOtp;
    private TextView tvEmail, tvTimer;
    private DatabaseHelper dbHelper;
    
    private String email;
    private String correctOtp;
    private int userId;
    private CountDownTimer countDownTimer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        
        dbHelper = new DatabaseHelper(this);
        
        // Get data from intent
        email = getIntent().getStringExtra("email");
        correctOtp = getIntent().getStringExtra("otp");
        userId = getIntent().getIntExtra("userId", -1);
        
        if (email == null || email.isEmpty() || correctOtp == null || correctOtp.isEmpty() || userId == -1) {
            Toast.makeText(this, "Lỗi: Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupOtpInputs();
        setupClickListeners();
        startTimer();
    }
    
    private void initViews() {
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        etOtp5 = findViewById(R.id.etOtp5);
        etOtp6 = findViewById(R.id.etOtp6);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        tvEmail = findViewById(R.id.tvEmail);
        tvTimer = findViewById(R.id.tvTimer);
        
        // Mask email for security
        tvEmail.setText(maskEmail(email));
    }
    
    private void setupOtpInputs() {
        // Auto move to next field
        setupOtpField(etOtp1, null, etOtp2);
        setupOtpField(etOtp2, etOtp1, etOtp3);
        setupOtpField(etOtp3, etOtp2, etOtp4);
        setupOtpField(etOtp4, etOtp3, etOtp5);
        setupOtpField(etOtp5, etOtp4, etOtp6);
        setupOtpField(etOtp6, etOtp5, null);
    }
    
    private void setupOtpField(TextInputEditText current, TextInputEditText previous, TextInputEditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && next != null) {
                    next.requestFocus();
                } else if (s.length() == 0 && previous != null) {
                    previous.requestFocus();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupClickListeners() {
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        
        btnResendOtp.setOnClickListener(v -> {
            // Resend OTP
            Intent intent = new Intent(VerifyOtpActivity.this, ForgotPasswordActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Vui lòng yêu cầu gửi lại mã OTP", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }
    
    private void verifyOtp() {
        String otp = etOtp1.getText().toString() +
                     etOtp2.getText().toString() +
                     etOtp3.getText().toString() +
                     etOtp4.getText().toString() +
                     etOtp5.getText().toString() +
                     etOtp6.getText().toString();
        
        if (otp.length() != 6) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ 6 số OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate OTP contains only digits
        if (!otp.matches("\\d{6}")) {
            Toast.makeText(this, "OTP chỉ chứa số", Toast.LENGTH_SHORT).show();
            return;
        }
        
        LogUtil.d(AppConstants.TAG_OTP, "Verifying OTP for userId: " + userId);
        
        if (otp.equals(correctOtp)) {
            // OTP correct, go to reset password screen
            Toast.makeText(this, "✅ Xác thực thành công!", Toast.LENGTH_SHORT).show();
            
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            
            Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("email", email);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {
            Toast.makeText(this, "❌ Mã OTP không đúng. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            clearOtpFields();
        }
    }
    
    private void clearOtpFields() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();
    }
    
    private void startTimer() {
        btnResendOtp.setEnabled(false);
        
        countDownTimer = new CountDownTimer(AppConstants.OTP_TIMEOUT_MS, AppConstants.OTP_TIMER_INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("Mã OTP hết hạn sau: %02d:%02d", minutes, seconds));
            }
            
            @Override
            public void onFinish() {
                tvTimer.setText("Mã OTP đã hết hạn");
                tvTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                btnResendOtp.setEnabled(true);
                Toast.makeText(VerifyOtpActivity.this, "Mã OTP đã hết hạn. Vui lòng gửi lại.", Toast.LENGTH_LONG).show();
            }
        }.start();
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 1) {
            return username.charAt(0) + "***@" + domain;
        } else if (username.length() == 2) {
            return username.charAt(0) + "*@" + domain;
        }
        
        String masked = username.charAt(0) + "***" + username.charAt(username.length() - 1) + "@" + domain;
        return masked;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
