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
import com.example.ecommerceapp.models.ShippingAddress;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class ShippingAddressAdapter extends RecyclerView.Adapter<ShippingAddressAdapter.ViewHolder> {
    private Context context;
    private List<ShippingAddress> addresses;
    private OnAddressClickListener listener;

    public interface OnAddressClickListener {
        void onSetDefault(ShippingAddress address);
        void onEdit(ShippingAddress address);
        void onDelete(ShippingAddress address);
    }

    public ShippingAddressAdapter(Context context) {
        this.context = context;
        this.addresses = new ArrayList<>();
    }

    public void setOnAddressClickListener(OnAddressClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShippingAddress address = addresses.get(position);
        
        holder.tvFullName.setText(address.getFullName());
        holder.tvPhone.setText("| " + address.getPhone());
        
        // Build full address with ward, district, province
        String fullAddress = address.getAddressDetail();
        if (address.getWard() != null && !address.getWard().isEmpty()) {
            fullAddress += "\n" + address.getWard();
        }
        if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
            fullAddress += ", " + address.getDistrict();
        }
        if (address.getProvince() != null && !address.getProvince().isEmpty()) {
            fullAddress += ", " + address.getProvince();
        }
        holder.tvAddress.setText(fullAddress);
        
        if (address.isDefault()) {
            holder.tvDefaultBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvDefaultBadge.setVisibility(View.GONE);
        }
        
        // Handle item click to edit directly
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public void updateAddresses(List<ShippingAddress> newAddresses) {
        this.addresses.clear();
        if (newAddresses != null) {
            this.addresses.addAll(newAddresses);
        }
        notifyDataSetChanged();
    }
    


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvPhone, tvAddress;
        com.google.android.material.card.MaterialCardView tvDefaultBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDefaultBadge = itemView.findViewById(R.id.tvDefaultBadge);
        }
    }
}
