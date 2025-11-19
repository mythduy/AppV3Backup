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
import com.bumptech.glide.signature.ObjectKey;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.utils.CategoryImageManager;
import java.io.File;
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
        
        // Load category image from internal storage using CategoryImageManager
        CategoryImageManager imageManager = new CategoryImageManager(context);
        String imagePath = imageManager.getCategoryImagePath(category);
        
        if (imagePath != null && new File(imagePath).exists()) {
            // Load from internal storage - full image with cache busting
            File imageFile = new File(imagePath);
            Glide.with(context)
                .load(imageFile)
                .signature(new ObjectKey(imageFile.lastModified())) // Force reload if file changed
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_category_default)
                .error(R.drawable.ic_category_default)
                .centerCrop()
                .into(holder.ivCategory);
        } else {
            // Fallback to icon
            holder.ivCategory.setImageResource(getCategoryIcon(category));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
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