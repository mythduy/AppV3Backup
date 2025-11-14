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
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.models.User;
import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {
    private Context context;
    private List<User> users = new ArrayList<>();
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onPromote(User user);
        void onDemote(User user);
        void onDelete(User user);
    }

    public AdminUserAdapter(Context context, OnUserActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername, tvFullName, tvEmail, tvRole;
        ImageButton btnPromoteDemote, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnPromoteDemote = itemView.findViewById(R.id.btnPromoteDemote);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnPromoteDemote.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    User user = users.get(position);
                    if (user.isAdmin()) {
                        listener.onDemote(user);
                    } else {
                        listener.onPromote(user);
                    }
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDelete(users.get(position));
                }
            });
        }

        void bind(User user) {
            tvUsername.setText("@" + user.getUsername());
            tvFullName.setText(user.getFullName());
            tvEmail.setText(user.getEmail());
            
            if (user.isAdmin()) {
                tvRole.setText("ADMIN");
                tvRole.setBackgroundResource(R.drawable.badge_hot);
                tvRole.setTextColor(androidx.core.content.ContextCompat.getColor(context, android.R.color.white));
                btnPromoteDemote.setImageResource(android.R.drawable.arrow_down_float);
                btnPromoteDemote.setContentDescription("Hạ quyền");
            } else {
                tvRole.setText("USER");
                tvRole.setBackgroundResource(R.drawable.bg_chip);
                tvRole.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.colorPrimary));
                btnPromoteDemote.setImageResource(android.R.drawable.arrow_up_float);
                btnPromoteDemote.setContentDescription("Nâng quyền");
            }

            ivAvatar.setImageResource(R.drawable.ic_profile);
        }
    }
}
