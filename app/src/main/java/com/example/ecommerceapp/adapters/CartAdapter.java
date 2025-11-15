package com.example.ecommerceapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.CartItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<CartItem> cartItems;
    private DatabaseHelper dbHelper;
    private OnCartUpdateListener listener;

    public interface OnCartUpdateListener {
        void onCartUpdated();
        void onSelectionChanged();
    }

    public CartAdapter(Context context, DatabaseHelper dbHelper, OnCartUpdateListener listener) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateCartItems(List<CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        ImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity, tvTotal;
        Button btnMinus, btnPlus, btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        void bind(CartItem item) {
            tvName.setText(item.getProductName());
            tvPrice.setText(formatPrice(item.getProductPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvTotal.setText(formatPrice(item.getTotalPrice()));
            ivProduct.setImageResource(R.drawable.ic_product_placeholder);
            cbSelect.setChecked(item.isSelected());

            // Checkbox listener
            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
                if (listener != null) {
                    listener.onSelectionChanged();
                }
            });

            btnMinus.setOnClickListener(v -> {
                int newQty = item.getQuantity() - 1;
                if (newQty > 0) {
                    dbHelper.updateCartItemQuantity(item.getId(), newQty);
                    listener.onCartUpdated();
                }
            });

            btnPlus.setOnClickListener(v -> {
                int newQty = item.getQuantity() + 1;
                dbHelper.updateCartItemQuantity(item.getId(), newQty);
                listener.onCartUpdated();
            });

            btnRemove.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Xóa sản phẩm")
                        .setMessage("Bạn có chắc muốn xóa sản phẩm này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            dbHelper.removeFromCart(item.getId());
                            listener.onCartUpdated();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        String formatPrice(double price) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(price);
        }
    }
}

