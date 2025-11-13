package com.example.ecommerceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.models.Product;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> products, OnProductClickListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
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

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivProduct = itemView.findViewById(R.id.ivProduct);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvBadge = itemView.findViewById(R.id.tvBadge);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(products.get(position));
                }
            });

            ivFavorite.setOnClickListener(v -> {
                // TODO: Implement favorite functionality
            });
        }

        void bind(Product product) {
            tvName.setText(product.getName());
            tvPrice.setText(formatPrice(product.getPrice()));

            if (product.getStock() > 0) {
                tvStock.setText("Còn: " + product.getStock());
                tvStock.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.colorGreen));
            } else {
                tvStock.setText("Hết hàng");
                tvStock.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.colorRed));
            }

            // Show badge for new products (last 5 products)
            if (product.getId() > products.size() - 5) {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText("MỚI");
                tvBadge.setBackgroundResource(R.drawable.badge_new);
            } else if (product.getPrice() > 500000) {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText("HOT");
                tvBadge.setBackgroundResource(R.drawable.badge_hot);
            } else {
                tvBadge.setVisibility(View.GONE);
            }

            ivProduct.setImageResource(R.drawable.ic_product_placeholder);
        }

        String formatPrice(double price) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(price);
        }
    }
}
