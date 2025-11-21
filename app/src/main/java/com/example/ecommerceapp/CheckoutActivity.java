package com.example.ecommerceapp;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.ecommerceapp.api.ApiClient;
import com.example.ecommerceapp.api.ProvinceApiService;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.CartItem;
import com.example.ecommerceapp.models.District;
import com.example.ecommerceapp.models.Order;
import com.example.ecommerceapp.models.Province;
import com.example.ecommerceapp.models.ShippingAddress;
import com.example.ecommerceapp.models.User;
import com.example.ecommerceapp.models.Ward;
import com.example.ecommerceapp.adapters.AddressSelectionAdapter;
import com.example.ecommerceapp.adapters.CheckoutProductAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.Intent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {
    private static final String TAG = "CheckoutActivity";
    
    private Toolbar toolbar;
    private TextView tvDefaultAddress, tvSubtotal, tvShipping, tvTotal;
    private RecyclerView rvSelectedProducts;
    private RadioGroup rgPayment;
    private MaterialButton btnChange, btnAddNewAddress, btnPlaceOrder;
    private CheckoutProductAdapter productAdapter;
    
    private DatabaseHelper dbHelper;
    private int userId;
    private ArrayList<Integer> selectedCartIds;
    private ShippingAddress selectedAddress;
    private double shippingFee = 15000; // 15k phí ship

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        // Nhận danh sách cart items đã chọn
        selectedCartIds = getIntent().getIntegerArrayListExtra("selected_cart_ids");
        if (selectedCartIds == null || selectedCartIds.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào được chọn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        loadDefaultShippingAddress();
        loadSelectedProducts();
        calculateTotal();

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        btnChange.setOnClickListener(v -> showAddressSelectionDialog());
        btnAddNewAddress.setOnClickListener(v -> openAddAddressActivity());
        
        // Initially hide add new button (will show if no address exists)
        btnAddNewAddress.setVisibility(View.GONE);
    }
    
    private void openAddAddressActivity() {
        Intent intent = new Intent(this, AddEditAddressActivity.class);
        intent.putExtra("user_id", userId);
        startActivityForResult(intent, 200);
    }
    
    private void loadSelectedProducts() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (int cartId : selectedCartIds) {
            CartItem item = dbHelper.getCartItemById(cartId);
            if (item != null) {
                selectedItems.add(item);
            }
        }
        productAdapter.setCartItems(selectedItems);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvDefaultAddress = findViewById(R.id.tvDefaultAddress);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShipping = findViewById(R.id.tvShipping);
        tvTotal = findViewById(R.id.tvTotal);
        rvSelectedProducts = findViewById(R.id.rvSelectedProducts);
        rgPayment = findViewById(R.id.rgPayment);
        btnChange = findViewById(R.id.btnChange);
        btnAddNewAddress = findViewById(R.id.btnAddNewAddress);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        
        // Setup RecyclerView
        rvSelectedProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new CheckoutProductAdapter(this);
        rvSelectedProducts.setAdapter(productAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Checkout");
        }
    }



    private void loadDefaultShippingAddress() {
        selectedAddress = dbHelper.getDefaultShippingAddress(userId);
        if (selectedAddress != null) {
            // Reset text color to default
            tvDefaultAddress.setTextColor(getResources().getColor(R.color.colorTextSecondary));
            tvDefaultAddress.setText(selectedAddress.getFullAddress());
            
            // Show appropriate badge
            if (selectedAddress.isDefault()) {
                tvDefaultAddress.append(" (Mặc định)");
            }
            
            // Make sure buttons are visible when address is loaded
            btnChange.setVisibility(View.VISIBLE);
            btnAddNewAddress.setVisibility(View.GONE);
        } else {
            tvDefaultAddress.setText("⚠️ Chưa có địa chỉ giao hàng. Vui lòng thêm địa chỉ để tiếp tục.");
            tvDefaultAddress.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            
            // Show add button when no address
            btnChange.setVisibility(View.GONE);
            btnAddNewAddress.setVisibility(View.VISIBLE);
        }
    }

    private void showAddressSelectionDialog() {
        List<ShippingAddress> addresses = dbHelper.getShippingAddresses(userId);
        
        if (addresses.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Chưa có địa chỉ")
                .setMessage("Bạn chưa có địa chỉ nào. Bạn có muốn thêm địa chỉ mới không?")
                .setPositiveButton("Thêm mới", (dialog, which) -> {
                    Intent intent = new Intent(this, AddEditAddressActivity.class);
                    intent.putExtra("user_id", userId);
                    startActivityForResult(intent, 200);
                })
                .setNegativeButton("Hủy", null)
                .show();
            return;
        }
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_choose_address, null);
        RecyclerView rvAddresses = dialogView.findViewById(R.id.rvAddresses);
        MaterialButton btnAddNew = dialogView.findViewById(R.id.btnAddNewAddress);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelDialog);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirmAddress);
        
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        
        AddressSelectionAdapter adapter = new AddressSelectionAdapter(addresses, address -> {
            // Enable confirm button when address is selected
            btnConfirm.setEnabled(true);
            btnConfirm.setText("Chọn địa chỉ này");
        });
        rvAddresses.setAdapter(adapter);
        
        // Disable confirm button initially if no address is pre-selected
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Chọn");
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        
        // Button "Thêm mới" - Open AddEditAddressActivity
        btnAddNew.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, AddEditAddressActivity.class);
            intent.putExtra("user_id", userId);
            startActivityForResult(intent, 200);
        });
        
        // Button "Hủy" - Just close dialog without changing anything
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        
        // Button "Ch\u1ecdn" - Apply selected address
        btnConfirm.setOnClickListener(v -> {
            ShippingAddress selected = adapter.getSelectedAddress();
            if (selected != null) {
                selectedAddress = selected;
                
                // Reset text color to default
                tvDefaultAddress.setTextColor(getResources().getColor(R.color.colorTextSecondary));
                tvDefaultAddress.setText(selected.getFullAddress());
                
                // Show appropriate badge
                if (selected.isDefault()) {
                    tvDefaultAddress.append(" (M\u1eb7c \u0111\u1ecbnh)");
                }
                
                Toast.makeText(this, "\u2705 \u0110\u00e3 ch\u1ecdn \u0111\u1ecba ch\u1ec9 giao h\u00e0ng", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "\u26a0\ufe0f Vui l\u00f2ng ch\u1ecdn m\u1ed9t \u0111\u1ecba ch\u1ec9", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            // Reload address after adding/editing
            loadDefaultShippingAddress();
            
            // Show success message
            if (selectedAddress != null) {
                Toast.makeText(this, "✅ Đã cập nhật địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void calculateTotal() {
        List<CartItem> allCartItems = dbHelper.getCartItems(userId);
        double subtotal = 0;
        
        // Chỉ tính tổng cho các sản phẩm đã chọn
        for (CartItem item : allCartItems) {
            if (selectedCartIds.contains(item.getId())) {
                subtotal += item.getTotalPrice();
            }
        }
        
        double total = subtotal + shippingFee;
        
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvSubtotal.setText(formatter.format(subtotal / 1000) + "k");
        tvShipping.setText(formatter.format(shippingFee / 1000) + "k");
        tvTotal.setText(formatter.format(total / 1000) + "k");
    }

    private void placeOrder() {
        if (selectedAddress == null) {
            Toast.makeText(this, "⚠️ Vui lòng thêm địa chỉ giao hàng trước khi đặt hàng", Toast.LENGTH_LONG).show();
            
            // Prompt user to add address
            new MaterialAlertDialogBuilder(this)
                .setTitle("Chưa có địa chỉ giao hàng")
                .setMessage("Bạn cần thêm địa chỉ giao hàng để hoàn tất đơn hàng. Thêm ngay?")
                .setPositiveButton("Thêm địa chỉ", (dialog, which) -> {
                    openAddAddressActivity();
                })
                .setNegativeButton("Để sau", null)
                .show();
            return;
        }

        int selectedPayment = rgPayment.getCheckedRadioButtonId();
        String paymentMethod;
        
        if (selectedPayment == R.id.rbCreditCard) {
            paymentMethod = "Credit/Debit Card";
        } else if (selectedPayment == R.id.rbEWallet) {
            paymentMethod = "E-Wallet (Momo/ZaloPay)";
        } else {
            paymentMethod = "Cash on Delivery (COD)";
        }

        List<CartItem> allCartItems = dbHelper.getCartItems(userId);
        
        // Lọc chỉ lấy các sản phẩm đã chọn
        List<CartItem> selectedCartItems = new ArrayList<>();
        for (CartItem item : allCartItems) {
            if (selectedCartIds.contains(item.getId())) {
                selectedCartItems.add(item);
            }
        }
        
        if (selectedCartItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào được chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        double subtotal = 0;
        for (CartItem item : selectedCartItems) {
            subtotal += item.getTotalPrice();
        }
        
        double totalAmount = subtotal + shippingFee;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String orderDate = sdf.format(new Date());

        Order order = new Order(0, userId, orderDate, totalAmount,
                Order.STATUS_PENDING, selectedAddress.getFullAddress(), paymentMethod);

        long orderId = dbHelper.createOrder(order, selectedCartItems);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}