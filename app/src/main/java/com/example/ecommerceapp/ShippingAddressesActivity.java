package com.example.ecommerceapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.card.MaterialCardView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.ShippingAddressAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.ShippingAddress;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.List;

public class ShippingAddressesActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView rvAddresses;
    private android.widget.LinearLayout layoutEmpty;
    private ShippingAddressAdapter adapter;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_addresses);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadAddresses();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvAddresses = findViewById(R.id.rvAddresses);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        MaterialButton btnAddAddress = findViewById(R.id.btnAddAddress);

        btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Địa chỉ của Tôi");
        }
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void setupRecyclerView() {
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShippingAddressAdapter(this);
        
        adapter.setOnAddressClickListener(new ShippingAddressAdapter.OnAddressClickListener() {
            @Override
            public void onSetDefault(ShippingAddress address) {
                setDefaultAddress(address);
            }

            @Override
            public void onEdit(ShippingAddress address) {
                showEditAddressDialog(address);
            }

            @Override
            public void onDelete(ShippingAddress address) {
                confirmDelete(address);
            }
        });
        
        rvAddresses.setAdapter(adapter);
    }

    private void loadAddresses() {
        List<ShippingAddress> addresses = dbHelper.getShippingAddresses(userId);
        
        if (addresses.isEmpty()) {
            rvAddresses.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvAddresses.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            adapter.updateAddresses(addresses);
        }
    }

    private void showAddAddressDialog() {
        android.content.Intent intent = new android.content.Intent(this, AddEditAddressActivity.class);
        intent.putExtra("user_id", userId);
        startActivityForResult(intent, 100);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showEditAddressDialog(ShippingAddress address) {
        android.content.Intent intent = new android.content.Intent(this, AddEditAddressActivity.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("address_id", address.getId());
        intent.putExtra("full_name", address.getFullName());
        intent.putExtra("phone", address.getPhone());
        intent.putExtra("province", address.getProvince());
        intent.putExtra("district", address.getDistrict());
        intent.putExtra("ward", address.getWard());
        intent.putExtra("address_detail", address.getAddressDetail());
        intent.putExtra("is_default", address.isDefault());
        startActivityForResult(intent, 101);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setDefaultAddress(ShippingAddress address) {
        boolean success = dbHelper.setDefaultAddress(userId, address.getId());
        if (success) {
            Toast.makeText(this, "✅ Đã đặt làm địa chỉ mặc định", Toast.LENGTH_SHORT).show();
            loadAddresses();
        } else {
            Toast.makeText(this, "❌ Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete(ShippingAddress address) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa địa chỉ")
            .setMessage("Bạn có chắc muốn xóa địa chỉ này?")
            .setPositiveButton("Xóa", (dialog, which) -> deleteAddress(address))
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteAddress(ShippingAddress address) {
        boolean success = dbHelper.deleteShippingAddress(address.getId());
        if (success) {
            Toast.makeText(this, "✅ Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
            loadAddresses();
        } else {
            Toast.makeText(this, "❌ Lỗi khi xóa", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 100 || requestCode == 101) && resultCode == RESULT_OK) {
            loadAddresses();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
