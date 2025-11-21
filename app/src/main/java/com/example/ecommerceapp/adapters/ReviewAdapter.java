package com.example.ecommerceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.models.Review;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private Context context;
    private List<Review> reviews;

    public ReviewAdapter(Context context) {
        this.context = context;
        this.reviews = new ArrayList<>();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        
        holder.tvUserName.setText(review.getUserName());
        holder.tvReviewDate.setText(review.getFormattedDate());
        holder.tvComment.setText(review.getComment());
        holder.ratingBar.setRating(review.getRating());
        holder.tvRating.setText(String.format("%.1f", review.getRating()));
        
        // Load user avatar with proper null checks
        String avatarUrl = review.getUserAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            java.io.File avatarFile = new java.io.File(avatarUrl);
            if (avatarFile.exists()) {
                // Load from file path
                Glide.with(context)
                    .load(avatarFile)
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(holder.ivUserAvatar);
            } else {
                // File not found, use placeholder
                holder.ivUserAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
            }
        } else {
            // No avatar URL, use placeholder
            holder.ivUserAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void updateReviews(List<Review> newReviews) {
        this.reviews.clear();
        if (newReviews != null) {
            this.reviews.addAll(newReviews);
        }
        notifyDataSetChanged();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivUserAvatar;
        TextView tvUserName, tvReviewDate, tvComment, tvRating;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvRating = itemView.findViewById(R.id.tvRating);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
