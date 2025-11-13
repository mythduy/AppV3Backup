package com.example.ecommerceapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.CartItem;
import com.example.ecommerceapp.models.Order;
import com.example.ecommerceapp.models.User;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {
    private EditText etAddress, etPhone;
    private RadioGroup rgPayment;
    private TextView tvTotal;
    private Button btnPlaceOrder;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        rgPayment = findViewById(R.id.rgPayment);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        loadUserInfo();
        calculateTotal();

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void loadUserInfo() {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            etAddress.setText(user.getAddress());
            etPhone.setText(user.getPhone());
        }
    }

    private void calculateTotal() {
        List<CartItem> cartItems = dbHelper.getCartItems(userId);
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        tvTotal.setText("Tổng thanh toán: " + formatPrice(total));
    }

    private void placeOrder() {
        String address = etAddress.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPayment = rgPayment.getCheckedRadioButtonId();
        String paymentMethod;

        if (selectedPayment == R.id.rbCOD) {
            paymentMethod = "COD";
        } else if (selectedPayment == R.id.rbBank) {
            paymentMethod = "Chuyển khoản";
        } else {
            paymentMethod = "Ví điện tử";
        }

        List<CartItem> cartItems = dbHelper.getCartItems(userId);
        double totalAmount = 0;
        for (CartItem item : cartItems) {
            totalAmount += item.getTotalPrice();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String orderDate = sdf.format(new Date());

        Order order = new Order(0, userId, orderDate, totalAmount,
                "Đang xử lý", address, paymentMethod);

        long orderId = dbHelper.createOrder(order, cartItems);

        if (orderId != -1) {
            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Đặt hàng thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
}