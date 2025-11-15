package com.example.ecommerceapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.LoginActivity;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {
    private Context context;
    private List<Product> products;
    private OnProductClickListener listener;
    private OnWishlistUpdateListener updateListener;
    private DatabaseHelper dbHelper;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnWishlistUpdateListener {
        void onWishlistUpdated();
    }

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    private OnCartUpdateListener cartUpdateListener;

    public WishlistAdapter(Context context, OnProductClickListener listener, OnWishlistUpdateListener updateListener) {
        this.context = context;
        this.products = new ArrayList<>();
        this.listener = listener;
        this.updateListener = updateListener;
        this.dbHelper = new DatabaseHelper(context);
    }

    public void setOnCartUpdateListener(OnCartUpdateListener listener) {
        this.cartUpdateListener = listener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    class WishlistViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivProduct;
        TextView tvName, tvPrice, tvStock;
        ImageButton btnRemove, btnAddToCart;

        WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStock = itemView.findViewById(R.id.tvStock);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(products.get(position));
                }
            });

            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    removeFromWishlist(products.get(position));
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    addToCart(products.get(position));
                }
            });
        }

        void bind(Product product) {
            tvName.setText(product.getName());
            tvPrice.setText(formatPrice(product.getPrice()));

            if (product.getStock() > 0) {
                tvStock.setText("Còn: " + product.getStock());
                tvStock.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.colorGreen));
                btnAddToCart.setEnabled(true);
            } else {
                tvStock.setText("Hết hàng");
                tvStock.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.colorRed));
                btnAddToCart.setEnabled(false);
            }

            ivProduct.setImageResource(R.drawable.ic_product_placeholder);
        }

        String formatPrice(double price) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(price);
        }

        void removeFromWishlist(Product product) {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa khỏi yêu thích")
                    .setMessage("Bạn có chắc muốn xóa sản phẩm này khỏi danh sách yêu thích?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        int userId = prefs.getInt("user_id", -1);
                        
                        boolean success = dbHelper.removeFromWishlist(userId, product.getId());
                        if (success) {
                            Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                            if (updateListener != null) {
                                updateListener.onWishlistUpdated();
                            }
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        void addToCart(Product product) {
            if (product.getStock() <= 0) {
                Toast.makeText(context, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);

            if (userId == -1) {
                Toast.makeText(context, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                return;
            }

            long result = dbHelper.addToCart(userId, product.getId(), 1);
            if (result != -1) {
                Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                // Callback để cập nhật badge
                if (cartUpdateListener != null) {
                    cartUpdateListener.onCartUpdated();
                }
            } else {
                Toast.makeText(context, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
