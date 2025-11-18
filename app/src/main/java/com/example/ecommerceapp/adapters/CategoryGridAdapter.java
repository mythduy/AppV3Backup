package com.example.ecommerceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryGridAdapter extends RecyclerView.Adapter<CategoryGridAdapter.CategoryViewHolder> {
    private Context context;
    private List<String> categories;
    private Map<String, Integer> categoryCount;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryGridAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.categories = new ArrayList<>();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_grid, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<String> newCategories, Map<String, Integer> newCategoryCount) {
        this.categories = newCategories;
        this.categoryCount = newCategoryCount;
        notifyDataSetChanged();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategory;
        TextView tvCategoryName, tvProductCount;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategory = itemView.findViewById(R.id.ivCategory);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvProductCount = itemView.findViewById(R.id.tvProductCount);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCategoryClick(categories.get(position));
                }
            });
        }

        void bind(String category) {
            tvCategoryName.setText(category);
            
            int count = categoryCount != null && categoryCount.containsKey(category) 
                    ? categoryCount.get(category) : 0;
            tvProductCount.setText(count + " sản phẩm");
            
            // Load category image from assets
            try {
                String fileName = getCategoryFileName(category);
                if (fileName != null) {
                    java.io.InputStream is = context.getAssets().open("images/categories/" + fileName);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
                    ivCategory.setImageBitmap(bitmap);
                    is.close();
                } else {
                    ivCategory.setImageResource(getCategoryIcon(category));
                }
            } catch (Exception e) {
                ivCategory.setImageResource(getCategoryIcon(category));
            }
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

        private int getCategoryIcon(String category) {
            switch (category) {
                case "Vi điều khiển":
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
                    return R.drawable.ic_category_default;
                case "Nguồn":
                    return R.drawable.ic_category_default;
                case "Máy tính nhúng":
                    return R.drawable.ic_microcontroller;
                default:
                    return R.drawable.ic_category_default;
            }
        }
    }
}
