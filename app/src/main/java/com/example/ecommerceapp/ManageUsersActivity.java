package com.example.ecommerceapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.adapters.AdminUserAdapter;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.User;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {
    private RecyclerView rvUsers;
    private AdminUserAdapter adapter;
    private DatabaseHelper dbHelper;
    private Toolbar toolbar;
    private TextView tvTotalUsers, tvAdminCount, tvUserCount;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        toolbar = findViewById(R.id.toolbar);
        rvUsers = findViewById(R.id.rvUsers);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvAdminCount = findViewById(R.id.tvAdminCount);
        tvUserCount = findViewById(R.id.tvUserCount);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý người dùng");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupRecyclerView();
        loadAllUsers();
    }

    private void setupRecyclerView() {
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(this, new AdminUserAdapter.OnUserActionListener() {
            @Override
            public void onPromote(User user) {
                showPromoteDialog(user);
            }

            @Override
            public void onDemote(User user) {
                showDemoteDialog(user);
            }

            @Override
            public void onDelete(User user) {
                showDeleteDialog(user);
            }
        });
        rvUsers.setAdapter(adapter);
    }

    private void showPromoteDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Nâng quyền Admin")
                .setMessage("Bạn có chắc muốn nâng quyền \"" + user.getUsername() + "\" lên Admin không?")
                .setPositiveButton("Nâng quyền", (dialog, which) -> {
                    user.setRole("admin");
                    boolean success = dbHelper.updateUser(user);
                    if (success) {
                        Toast.makeText(this, "Đã nâng quyền thành công", Toast.LENGTH_SHORT).show();
                        loadAllUsers();
                    } else {
                        Toast.makeText(this, "Lỗi khi nâng quyền", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDemoteDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Hạ quyền")
                .setMessage("Bạn có chắc muốn hạ quyền \"" + user.getUsername() + "\" xuống User không?")
                .setPositiveButton("Hạ quyền", (dialog, which) -> {
                    user.setRole("user");
                    boolean success = dbHelper.updateUser(user);
                    if (success) {
                        Toast.makeText(this, "Đã hạ quyền thành công", Toast.LENGTH_SHORT).show();
                        loadAllUsers();
                    } else {
                        Toast.makeText(this, "Lỗi khi hạ quyền", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteDialog(User user) {
        // Prevent deleting current logged in user
        if (user.getId() == currentUserId) {
            Toast.makeText(this, "Không thể xóa tài khoản đang đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa người dùng")
                .setMessage("Bạn có chắc muốn xóa người dùng \"" + user.getUsername() + "\" không?\n\nLưu ý: Điều này sẽ xóa tất cả dữ liệu liên quan đến người dùng này.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean success = dbHelper.deleteUser(user.getId());
                    if (success) {
                        Toast.makeText(this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                        loadAllUsers();
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa người dùng", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadAllUsers() {
        List<User> users = dbHelper.getAllUsers();
        adapter.updateUsers(users);
        
        // Calculate statistics
        int totalUsers = users.size();
        int adminCount = 0;
        int userCount = 0;
        
        for (User user : users) {
            if (user.isAdmin()) {
                adminCount++;
            } else {
                userCount++;
            }
        }
        
        tvTotalUsers.setText(String.valueOf(totalUsers));
        tvAdminCount.setText(String.valueOf(adminCount));
        tvUserCount.setText(String.valueOf(userCount));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllUsers();
    }
}
