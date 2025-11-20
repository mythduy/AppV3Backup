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
import com.example.ecommerceapp.models.User;
import com.example.ecommerceapp.models.Ward;
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
    private Spinner spinnerCountryCode, spinnerProvince, spinnerDistrict, spinnerWard;
    private EditText etEmail, etFullName, etPhone, etAddress, etNote;
    private RadioGroup rgPayment;
    private TextView tvTotal, tvShippingInfo;
    private Button btnPlaceOrder;
    private DatabaseHelper dbHelper;
    private int userId;
    private ArrayList<Integer> selectedCartIds; // Danh sách ID cart items được chọn
    
    // API Service
    private ProvinceApiService apiService;
    
    // Adapters
    private ArrayAdapter<Province> provinceAdapter;
    private ArrayAdapter<District> districtAdapter;
    private ArrayAdapter<Ward> wardAdapter;
    
    // Data lists
    private List<Province> provinceList = new ArrayList<>();
    private List<District> districtList = new ArrayList<>();
    private List<Ward> wardList = new ArrayList<>();

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
        apiService = ApiClient.getProvinceApiService();

        initViews();
        setupToolbar();
        setupSpinners();
        loadProvinces();
        loadUserInfo();
        calculateTotal();

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerCountryCode = findViewById(R.id.spinnerCountryCode);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerWard = findViewById(R.id.spinnerWard);
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etNote = findViewById(R.id.etNote);
        rgPayment = findViewById(R.id.rgPayment);
        tvTotal = findViewById(R.id.tvTotal);
        tvShippingInfo = findViewById(R.id.tvShippingInfo);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh toán");
        }
    }

    private void setupSpinners() {
        // Spinner Country Code
        List<String> countryCodes = new ArrayList<>();
        countryCodes.add("🇻🇳 +84");
        countryCodes.add("🇺🇸 +1");
        countryCodes.add("🇬🇧 +44");
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countryCodes);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(countryAdapter);

        // Spinner Tỉnh thành - sẽ được load từ API
        provinceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, provinceList);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

        // Spinner Quận huyện
        districtAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, districtList);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);

        // Spinner Phường xã
        wardAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wardList);
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(wardAdapter);
        
        // Listeners
        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Province province = provinceList.get(position);
                    loadDistricts(province.getCode());
                } else {
                    districtList.clear();
                    districtAdapter.notifyDataSetChanged();
                    wardList.clear();
                    wardAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    District district = districtList.get(position);
                    loadWards(district.getCode());
                } else {
                    wardList.clear();
                    wardAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void loadProvinces() {
        apiService.getProvinces().enqueue(new Callback<List<Province>>() {
            @Override
            public void onResponse(Call<List<Province>> call, Response<List<Province>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    provinceList.clear();
                    Province defaultProvince = new Province();
                    defaultProvince.setName("Chọn tỉnh thành");
                    provinceList.add(defaultProvince);
                    provinceList.addAll(response.body());
                    provinceAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + response.body().size() + " provinces");
                } else {
                    Toast.makeText(CheckoutActivity.this, "Không thể tải danh sách tỉnh thành", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                Log.e(TAG, "Error loading provinces", t);
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadDistricts(String provinceCode) {
        apiService.getDistricts(provinceCode).enqueue(new Callback<ProvinceApiService.ProvinceWithDistricts>() {
            @Override
            public void onResponse(Call<ProvinceApiService.ProvinceWithDistricts> call, Response<ProvinceApiService.ProvinceWithDistricts> response) {
                if (response.isSuccessful() && response.body() != null) {
                    districtList.clear();
                    District defaultDistrict = new District();
                    defaultDistrict.setName("Chọn quận/huyện");
                    districtList.add(defaultDistrict);
                    districtList.addAll(response.body().districts);
                    districtAdapter.notifyDataSetChanged();
                    
                    wardList.clear();
                    wardAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + response.body().districts.size() + " districts");
                }
            }

            @Override
            public void onFailure(Call<ProvinceApiService.ProvinceWithDistricts> call, Throwable t) {
                Log.e(TAG, "Error loading districts", t);
                Toast.makeText(CheckoutActivity.this, "Lỗi tải quận/huyện", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadWards(String districtCode) {
        apiService.getWards(districtCode).enqueue(new Callback<ProvinceApiService.DistrictWithWards>() {
            @Override
            public void onResponse(Call<ProvinceApiService.DistrictWithWards> call, Response<ProvinceApiService.DistrictWithWards> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wardList.clear();
                    Ward defaultWard = new Ward();
                    defaultWard.setName("Chọn phường/xã");
                    wardList.add(defaultWard);
                    wardList.addAll(response.body().wards);
                    wardAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + response.body().wards.size() + " wards");
                }
            }

            @Override
            public void onFailure(Call<ProvinceApiService.DistrictWithWards> call, Throwable t) {
                Log.e(TAG, "Error loading wards", t);
                Toast.makeText(CheckoutActivity.this, "Lỗi tải phường/xã", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserInfo() {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            etEmail.setText(user.getEmail());
            etFullName.setText(user.getFullName());
            etPhone.setText(user.getPhone());
            // Không load địa chỉ cũ - để khách tự nhập địa chỉ mới
        }
    }

    private void calculateTotal() {
        List<CartItem> allCartItems = dbHelper.getCartItems(userId);
        double total = 0;
        
        // Chỉ tính tổng cho các sản phẩm đã chọn
        for (CartItem item : allCartItems) {
            if (selectedCartIds.contains(item.getId())) {
                total += item.getTotalPrice();
            }
        }
        
        tvTotal.setText("Tổng thanh toán: " + formatPrice(total));
    }

    private void placeOrder() {
        String email = etEmail.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String province = spinnerProvince.getSelectedItem().toString();
        String district = spinnerDistrict.getSelectedItem().toString();
        String ward = spinnerWard.getSelectedItem().toString();
        String note = etNote.getText().toString().trim();

        if (email.isEmpty() || fullName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (province.equals("Chọn tỉnh thành") || district.equals("Chọn quận/huyện") || ward.equals("Chọn phường/xã")) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ tỉnh/quận/phường", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPayment = rgPayment.getCheckedRadioButtonId();
        String paymentMethod;
        
        if (selectedPayment == R.id.rbCOD) {
            paymentMethod = "Thanh toán khi nhận hàng (COD)";
        } else if (selectedPayment == R.id.rbBank) {
            paymentMethod = "Chuyển khoản ngân hàng";
        } else {
            paymentMethod = "COD";
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

        double totalAmount = 0;
        for (CartItem item : selectedCartItems) {
            totalAmount += item.getTotalPrice();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String orderDate = sdf.format(new Date());

        String fullAddress = address + ", " + ward + ", " + district + ", " + province;
        Order order = new Order(0, userId, orderDate, totalAmount,
                Order.STATUS_PENDING, fullAddress, paymentMethod);

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