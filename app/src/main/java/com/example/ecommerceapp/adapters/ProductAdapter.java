package com.example.ecommerceapp.adapters;

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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ecommerceapp.LoginActivity;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> products;
    private OnProductClickListener listener;
    private DatabaseHelper dbHelper;
    private OnCartUpdateListener cartUpdateListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    public ProductAdapter(Context context, List<Product> products, OnProductClickListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
        this.dbHelper = new DatabaseHelper(context);
    }

    public void setOnCartUpdateListener(OnCartUpdateListener listener) {
        this.cartUpdateListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
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

    class ProductViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivProduct, ivFavorite;
        TextView tvName, tvPrice, tvStock, tvBadge;
        ImageButton btnAddToCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivProduct = itemView.findViewById(R.id.ivProduct);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(products.get(position));
                }
            });

            ivFavorite.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    toggleWishlist(products.get(position));
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
            
            // Show discounted price if available
            if (product.getDiscount() > 0) {
                double finalPrice = product.getFinalPrice();
                tvPrice.setText(formatPrice(finalPrice));
            } else {
                tvPrice.setText(formatPrice(product.getPrice()));
            }

            if (product.getStock() > 0) {
                tvStock.setText("Còn: " + product.getStock());
                tvStock.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.colorGreen));
            } else {
                tvStock.setText("Hết hàng");
                tvStock.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.colorRed));
            }

            // Show badge based on product flags from database
            if (product.isNew()) {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText("MỚI");
                tvBadge.setBackgroundResource(R.drawable.badge_new);
            } else if (product.isHot()) {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText("HOT");
                tvBadge.setBackgroundResource(R.drawable.badge_hot);
            } else {
                tvBadge.setVisibility(View.GONE);
            }

            // Load product image - support both file path and asset URL
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                // Check if it's a file path (not starting with file://)
                java.io.File imageFile = new java.io.File(product.getImageUrl());
                if (imageFile.exists() && !product.getImageUrl().startsWith("file://")) {
                    // Load from internal storage
                    Glide.with(context)
                        .load(imageFile)
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .centerCrop()
                        .into(ivProduct);
                } else {
                    // Load as URL (supports file:///android_asset/ and http/https URLs)
                    Glide.with(context)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(ivProduct);
                }
            } else {
                ivProduct.setImageResource(R.drawable.ic_product_placeholder);
            }
            
            // Update wishlist icon
            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId != -1 && dbHelper.isInWishlist(userId, product.getId())) {
                // Trong wishlist: dùng filled icon màu đỏ
                ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
                ivFavorite.clearColorFilter();
            } else {
                // Không trong wishlist: dùng border icon màu xám
                ivFavorite.setImageResource(R.drawable.ic_favorite_border);
                ivFavorite.setColorFilter(androidx.core.content.ContextCompat.getColor(context, R.color.colorTextSecondary));
            }
        }

        String formatPrice(double price) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(price);
        }

        void addToCart(Product product) {
            if (product.getStock() <= 0) {
                Toast.makeText(context, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);

            // Nếu chưa đăng nhập, chuyển đến trang đăng nhập
            if (userId == -1) {
                Toast.makeText(context, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                return;
            }

            long result = dbHelper.addToCart(userId, product.getId(), 1);
            if (result != -1) {
                Toast.makeText(context, "Đã thêm \"" + product.getName() + "\" vào giỏ hàng", Toast.LENGTH_SHORT).show();
                // Callback để cập nhật badge
                if (cartUpdateListener != null) {
                    cartUpdateListener.onCartUpdated();
                }
            } else {
                Toast.makeText(context, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        }

        void toggleWishlist(Product product) {
            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);

            if (userId == -1) {
                Toast.makeText(context, "Vui lòng đăng nhập để sử dụng tính năng yêu thích", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                return;
            }

            if (dbHelper.isInWishlist(userId, product.getId())) {
                // Remove from wishlist
                boolean success = dbHelper.removeFromWishlist(userId, product.getId());
                if (success) {
                    Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    notifyItemChanged(getAdapterPosition());
                }
            } else {
                // Add to wishlist
                long result = dbHelper.addToWishlist(userId, product.getId());
                if (result != -1) {
                    Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    notifyItemChanged(getAdapterPosition());
                } else {
                    Toast.makeText(context, "Sản phẩm đã có trong danh sách yêu thích", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
