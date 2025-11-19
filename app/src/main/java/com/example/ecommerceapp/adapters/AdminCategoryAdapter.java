package com.example.ecommerceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import java.util.ArrayList;
import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {
    private Context context;
    private List<String> categories = new ArrayList<>();
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEdit(String category);
        void onDelete(String category);
        void onUploadImage(String category);
    }

    public AdminCategoryAdapter(Context context, OnCategoryActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<String> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        ImageButton btnEdit, btnDelete, btnUploadImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnUploadImage = itemView.findViewById(R.id.btnUploadImage);

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEdit(categories.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDelete(categories.get(position));
                }
            });
            
            btnUploadImage.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUploadImage(categories.get(position));
                }
            });
        }

        void bind(String category) {
            tvCategoryName.setText(category);
            
            // Load category image
            CategoryImageManager imageManager = new CategoryImageManager(context);
            String imagePath = imageManager.getCategoryImagePath(category);
            
            if (imagePath != null && new File(imagePath).exists()) {
                // Use signature to force Glide to reload if file changed
                File imageFile = new File(imagePath);
                Glide.with(context)
                    .load(imageFile)
                    .signature(new ObjectKey(imageFile.lastModified()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .placeholder(R.drawable.ic_category)
                    .into(ivCategoryIcon);
            } else {
                ivCategoryIcon.setImageResource(R.drawable.ic_category);
            }
        }
    }
}
