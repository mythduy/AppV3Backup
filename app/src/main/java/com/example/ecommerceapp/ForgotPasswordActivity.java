package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.User;
import com.example.ecommerceapp.utils.AppConstants;
import com.example.ecommerceapp.utils.EmailConfig;
import com.example.ecommerceapp.utils.LogUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Properties;
import java.util.Random;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText etEmail;
    private MaterialButton btnSendOtp, btnBackToLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();
        
        // Pre-fill email if provided
        String email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            etEmail.setText(email);
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
    }

    private void setupClickListeners() {
        btnSendOtp.setOnClickListener(v -> sendOtp());
        
        btnBackToLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void sendOtp() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        btnSendOtp.setEnabled(false);
        btnSendOtp.setText("Đang xử lý...");

        // Check if email exists in database
        User user = dbHelper.getUserByEmail(email);

        if (user == null) {
            btnSendOtp.setEnabled(true);
            btnSendOtp.setText("Gửi mã OTP");
            Toast.makeText(this, "❌ Email không tồn tại trong hệ thống", Toast.LENGTH_LONG).show();
            return;
        }

        // Generate 6-digit OTP
        String otp = generateOtp();
        
        LogUtil.d(AppConstants.TAG_OTP, "OTP generated for " + email);
        
        // Send OTP email
        sendOtpEmail(email, user.getUsername(), otp, user.getId());
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = AppConstants.OTP_MIN_VALUE + random.nextInt(AppConstants.OTP_MAX_VALUE - AppConstants.OTP_MIN_VALUE + 1);
        return String.valueOf(otp);
    }

    private void sendOtpEmail(String toEmail, String username, String otp, int userId) {
        // Get email credentials from config
        final String fromEmail = EmailConfig.getFromEmail();
        final String fromPassword = EmailConfig.getFromPassword();
        
        LogUtil.d(AppConstants.TAG_EMAIL, "Starting OTP email send...");
        LogUtil.d(AppConstants.TAG_EMAIL, "To: " + toEmail); // Don't log OTP in production
        
        // Show loading
        Toast.makeText(this, "Đang gửi mã OTP... ⏳", Toast.LENGTH_SHORT).show();
        
        // Send email in background thread
        new AsyncTask<Void, Void, Boolean>() {
            Exception error = null;
            String errorType = "";
            
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    // Gmail SMTP configuration
                    Properties props = new Properties();
                    props.put("mail.smtp.host", EmailConfig.getSmtpHost());
                    props.put("mail.smtp.port", EmailConfig.getSmtpPort());
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.starttls.required", "true");
                    props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                    props.put("mail.smtp.connectiontimeout", String.valueOf(AppConstants.EMAIL_CONNECTION_TIMEOUT_MS));
                    props.put("mail.smtp.timeout", String.valueOf(AppConstants.EMAIL_SEND_TIMEOUT_MS));
                    
                    // Create session with authentication
                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(fromEmail, fromPassword);
                        }
                    });
                    
                    // Only enable debug in debug mode
                    session.setDebug(AppConstants.DEBUG_MODE);
                    
                    // Create email message
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(fromEmail));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                    message.setSubject("Mã OTP - Reset Password - Electronics Shop");
                    
                    // Email body
                    String emailBody = "Xin chào " + username + ",\n\n" +
                            "Bạn đã yêu cầu reset mật khẩu cho tài khoản Electronics Shop.\n\n" +
                            "🔐 Mã OTP của bạn là:\n\n" +
                            "       " + otp + "\n\n" +
                            "⚠️ Lưu ý quan trọng:\n" +
                            "   • Mã OTP này có hiệu lực trong 5 phút\n" +
                            "   • KHÔNG chia sẻ mã này với bất kỳ ai\n" +
                            "   • Nếu bạn không yêu cầu reset mật khẩu, vui lòng bỏ qua email này\n\n" +
                            "Vui lòng nhập mã OTP vào ứng dụng để tiếp tục đặt lại mật khẩu.\n\n" +
                            "Trân trọng,\n" +
                            "Electronics Shop Team";
                    
                    message.setText(emailBody);
                    
                    LogUtil.d(AppConstants.TAG_EMAIL, "Sending OTP email via SMTP...");
                    
                    // Send email
                    Transport.send(message);
                    
                    LogUtil.d(AppConstants.TAG_EMAIL, "✅ OTP email sent successfully!");
                    return true;
                    
                } catch (javax.mail.AuthenticationFailedException e) {
                    errorType = "Authentication Failed";
                    error = e;
                    LogUtil.e(AppConstants.TAG_EMAIL_ERROR, "❌ AUTHENTICATION ERROR: " + e.getMessage(), e);
                    return false;
                } catch (javax.mail.MessagingException e) {
                    errorType = "Messaging Error";
                    error = e;
                    LogUtil.e(AppConstants.TAG_EMAIL_ERROR, "❌ MESSAGING ERROR: " + e.getMessage(), e);
                    return false;
                } catch (Exception e) {
                    errorType = "General Error";
                    error = e;
                    LogUtil.e(AppConstants.TAG_EMAIL_ERROR, "❌ GENERAL ERROR: " + e.getClass().getName() + " - " + e.getMessage(), e);
                    return false;
                }
            }
            
            @Override
            protected void onPostExecute(Boolean success) {
                btnSendOtp.setEnabled(true);
                btnSendOtp.setText("Gửi mã OTP");
                
                if (success) {
                    Toast.makeText(ForgotPasswordActivity.this, 
                        "✅ Mã OTP đã được gửi đến email của bạn!", 
                        Toast.LENGTH_LONG).show();
                    
                    // Go to OTP verification screen
                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
                    intent.putExtra("email", toEmail);
                    intent.putExtra("otp", otp);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else {
                    String errorMsg = errorType + ": " + (error != null ? error.getMessage() : "Unknown error");
                    LogUtil.e(AppConstants.TAG_EMAIL_ERROR, "Final error: " + errorMsg);
                    
                    Toast.makeText(ForgotPasswordActivity.this, 
                        "❌ Lỗi gửi email: " + errorType, 
                        Toast.LENGTH_LONG).show();
                    
                    // Show fallback dialog with OTP
                    showOtpDialog(otp, userId, toEmail);
                }
            }
        }.execute();
    }

    private void showOtpDialog(String otp, int userId, String email) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("🔐 Mã OTP")
            .setMessage("Không thể gửi email tự động.\n\n" +
                    "Mã OTP của bạn là:\n\n" +
                    "       " + otp + "\n\n" +
                    "Vui lòng ghi nhớ mã này.")
            .setPositiveButton("Nhập OTP", (dialog, which) -> {
                Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("otp", otp);
                intent.putExtra("userId", userId);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            })
            .setNegativeButton("Sao chép OTP", (dialog, which) -> {
                android.content.ClipboardManager clipboard = 
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("otp", otp);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Đã sao chép OTP", Toast.LENGTH_SHORT).show();
                
                // Still go to OTP screen
                Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("otp", otp);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            })
            .setCancelable(false)
            .show();
    }
}
