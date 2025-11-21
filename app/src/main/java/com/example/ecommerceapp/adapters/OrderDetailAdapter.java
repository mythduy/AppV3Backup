package com.example.ecommerceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.models.OrderItem;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderItemViewHolder> {
    private Context context;
    private List<OrderItem> orderItems;

    public OrderDetailAdapter(Context context) {
        this.context = context;
        this.orderItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public void updateOrderItems(List<OrderItem> newItems) {
        this.orderItems = newItems;
        notifyDataSetChanged();
    }

    class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName, tvQuantity, tvPrice, tvTotal;

        OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }

        void bind(OrderItem item) {
            tvProductName.setText(item.getProductName());
            tvQuantity.setText("x" + item.getQuantity());
            tvPrice.setText(formatPrice(item.getPrice()));
            tvTotal.setText(formatPrice(item.getTotalPrice()));
            
            // Load product image with Glide
            String imageUrl = item.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                File imageFile = new File(imageUrl);
                if (imageFile.exists()) {
                    Glide.with(context)
                        .load(imageFile)
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .centerCrop()
                        .into(ivProduct);
                } else {
                    // Try loading from assets
                    try {
                        Glide.with(context)
                            .load("file:///android_asset/" + imageUrl)
                            .placeholder(R.drawable.ic_product_placeholder)
                            .error(R.drawable.ic_product_placeholder)
                            .centerCrop()
                            .into(ivProduct);
                    } catch (Exception e) {
                        ivProduct.setImageResource(R.drawable.ic_product_placeholder);
                    }
                }
            } else {
                ivProduct.setImageResource(R.drawable.ic_product_placeholder);
            }
        }

        String formatPrice(double price) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(price);
        }
    }
}
