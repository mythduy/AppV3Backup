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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ecommerceapp.R;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private Context context;
    private List<String> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryAdapter(Context context, List<String> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategory.setText(category);
        
        // Load category image - try assets path directly
        try {
            String fileName = getCategoryFileName(category);
            if (fileName != null) {
                java.io.InputStream is = context.getAssets().open("images/categories/" + fileName);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
                holder.ivCategory.setImageBitmap(bitmap);
                is.close();
            } else {
                holder.ivCategory.setImageResource(getCategoryIcon(category));
            }
        } catch (Exception e) {
            // Fallback to icon if image not found
            holder.ivCategory.setImageResource(getCategoryIcon(category));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }
    
    private String getCategoryFileName(String category) {
        switch (category) {
            case "Vi điều khiển":
                return "category_microcontroller.jpg";
            case "Máy tính nhúng":
                return "category_embedded.jpg";
            case "Cảm biến":
                return "category_sensor.jpg";
            case "Module":
            case "Module WiFi":
                return "category_module.jpg";
            case "LED":
                return "category_led.jpg";
            case "Màn hình":
                return "category_display.jpg";
            case "Động cơ":
                return "category_motor.jpg";
            case "Linh kiện":
                return "category_component.jpg";
            case "Nguồn":
                return "category_power.jpg";
            default:
                return null;
        }
    }
    


    @Override
    public int getItemCount() {
        return categories.size();
    }

    private int getCategoryIcon(String category) {
        switch (category) {
            case "Vi điều khiển":
                return R.drawable.ic_microcontroller;
            case "Máy tính nhúng":
                return R.drawable.ic_microcontroller;
            case "Cảm biến":
                return R.drawable.ic_sensor;
            case "Module":
            case "Module WiFi":
                return R.drawable.ic_module;
            case "LED":
                return R.drawable.ic_led;
            case "Màn hình":
                return R.drawable.ic_display;
            case "Động cơ":
                return R.drawable.ic_motor;
            case "Linh kiện":
                return R.drawable.ic_inventory;
            case "Nguồn":
                return R.drawable.ic_category_default;
            default:
                return R.drawable.ic_category_default;
        }
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategory;
        TextView tvCategory;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategory = itemView.findViewById(R.id.ivCategory);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}