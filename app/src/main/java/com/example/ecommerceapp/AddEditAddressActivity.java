package com.example.ecommerceapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecommerceapp.api.ApiClient;
import com.example.ecommerceapp.api.ProvinceApiService;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.District;
import com.example.ecommerceapp.models.Province;
import com.example.ecommerceapp.models.Ward;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditAddressActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextInputEditText etFullName, etPhone, etAddressDetail;
    private Spinner spinnerProvince, spinnerDistrict, spinnerWard;
    private CheckBox cbSetDefault;
    private MaterialButton btnSave;
    private DatabaseHelper dbHelper;
    private ProvinceApiService apiService;
    private int userId;
    private int addressId = -1;
    private boolean isEditMode = false;
    
    private ArrayAdapter<Province> provinceAdapter;
    private ArrayAdapter<District> districtAdapter;
    private ArrayAdapter<Ward> wardAdapter;
    
    private List<Province> provinceList = new ArrayList<>();
    private List<District> districtList = new ArrayList<>();
    private List<Ward> wardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_address);

        userId = getIntent().getIntExtra("user_id", -1);
        addressId = getIntent().getIntExtra("address_id", -1);
        isEditMode = (addressId != -1);
        
        dbHelper = new DatabaseHelper(this);
        apiService = ApiClient.getProvinceApiService();

        initViews();
        setupToolbar();
        setupSpinners();
        loadProvinces();
        
        if (isEditMode) {
            fillEditData();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etAddressDetail = findViewById(R.id.etAddressDetail);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerWard = findViewById(R.id.spinnerWard);
        cbSetDefault = findViewById(R.id.cbSetDefault);
        btnSave = findViewById(R.id.btnSave);
        MaterialButton btnDelete = findViewById(R.id.btnDelete);

        btnSave.setOnClickListener(v -> saveAddress());
        btnDelete.setOnClickListener(v -> confirmDelete());
        
        // Show delete button only in edit mode
        if (isEditMode) {
            btnDelete.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Chỉnh sửa địa chỉ" : "Thêm địa chỉ mới");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSpinners() {
        provinceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, provinceList);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

        districtAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, districtList);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);

        wardAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wardList);
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(wardAdapter);

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
                }
            }

            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                Toast.makeText(AddEditAddressActivity.this, 
                    "Lỗi tải danh sách tỉnh thành", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDistricts(String provinceCode) {
        apiService.getDistricts(provinceCode).enqueue(new Callback<ProvinceApiService.ProvinceWithDistricts>() {
            @Override
            public void onResponse(Call<ProvinceApiService.ProvinceWithDistricts> call, Response<ProvinceApiService.ProvinceWithDistricts> response) {
                if (response.isSuccessful() && response.body() != null && response.body().districts != null) {
                    districtList.clear();
                    District defaultDistrict = new District();
                    defaultDistrict.setName("Chọn quận huyện");
                    districtList.add(defaultDistrict);
                    districtList.addAll(response.body().districts);
                    districtAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ProvinceApiService.ProvinceWithDistricts> call, Throwable t) {
                Toast.makeText(AddEditAddressActivity.this,
                    "Lỗi tải danh sách quận huyện", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWards(String districtCode) {
        apiService.getWards(districtCode).enqueue(new Callback<ProvinceApiService.DistrictWithWards>() {
            @Override
            public void onResponse(Call<ProvinceApiService.DistrictWithWards> call, Response<ProvinceApiService.DistrictWithWards> response) {
                if (response.isSuccessful() && response.body() != null && response.body().wards != null) {
                    wardList.clear();
                    Ward defaultWard = new Ward();
                    defaultWard.setName("Chọn phường xã");
                    wardList.add(defaultWard);
                    wardList.addAll(response.body().wards);
                    wardAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ProvinceApiService.DistrictWithWards> call, Throwable t) {
                Toast.makeText(AddEditAddressActivity.this,
                    "Lỗi tải danh sách phường xã", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillEditData() {
        etFullName.setText(getIntent().getStringExtra("full_name"));
        etPhone.setText(getIntent().getStringExtra("phone"));
        etAddressDetail.setText(getIntent().getStringExtra("address_detail"));
        cbSetDefault.setChecked(getIntent().getBooleanExtra("is_default", false));
        
        final String province = getIntent().getStringExtra("province");
        final String district = getIntent().getStringExtra("district");
        final String ward = getIntent().getStringExtra("ward");
        
        spinnerProvince.post(() -> {
            for (int i = 0; i < provinceList.size(); i++) {
                if (provinceList.get(i).getName().equals(province)) {
                    spinnerProvince.setSelection(i);
                    
                    spinnerDistrict.postDelayed(() -> {
                        for (int j = 0; j < districtList.size(); j++) {
                            if (districtList.get(j).getName().equals(district)) {
                                spinnerDistrict.setSelection(j);
                                
                                spinnerWard.postDelayed(() -> {
                                    for (int k = 0; k < wardList.size(); k++) {
                                        if (wardList.get(k).getName().equals(ward)) {
                                            spinnerWard.setSelection(k);
                                            break;
                                        }
                                    }
                                }, 500);
                                break;
                            }
                        }
                    }, 500);
                    break;
                }
            }
        });
    }

    private void saveAddress() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String addressDetail = etAddressDetail.getText().toString().trim();
        
        if (fullName.isEmpty() || phone.isEmpty() || addressDetail.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerProvince.getSelectedItemPosition() == 0 ||
            spinnerDistrict.getSelectedItemPosition() == 0 ||
            spinnerWard.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        Province province = provinceList.get(spinnerProvince.getSelectedItemPosition());
        District district = districtList.get(spinnerDistrict.getSelectedItemPosition());
        Ward ward = wardList.get(spinnerWard.getSelectedItemPosition());
        boolean isDefault = cbSetDefault.isChecked();

        if (isEditMode) {
            boolean success = dbHelper.updateShippingAddress(addressId, fullName, phone,
                    province.getName(), district.getName(), ward.getName(),
                    addressDetail, isDefault);
            
            if (success) {
                Toast.makeText(this, "✅ Đã cập nhật địa chỉ", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "❌ Lỗi khi cập nhật địa chỉ", Toast.LENGTH_SHORT).show();
            }
        } else {
            long result = dbHelper.addShippingAddress(userId, fullName, phone,
                    province.getName(), district.getName(), ward.getName(),
                    addressDetail, isDefault);

            if (result != -1) {
                Toast.makeText(this, "✅ Đã thêm địa chỉ mới", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "❌ Lỗi khi thêm địa chỉ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void confirmDelete() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa địa chỉ")
                .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAddress())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteAddress() {
        if (addressId != -1) {
            boolean success = dbHelper.deleteShippingAddress(addressId);
            if (success) {
                Toast.makeText(this, "✅ Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "❌ Lỗi khi xóa địa chỉ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
