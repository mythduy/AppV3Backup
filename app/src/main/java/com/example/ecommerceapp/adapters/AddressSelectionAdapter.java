package com.example.ecommerceapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.models.ShippingAddress;
import java.util.List;

public class AddressSelectionAdapter extends RecyclerView.Adapter<AddressSelectionAdapter.ViewHolder> {
    private List<ShippingAddress> addresses;
    private int selectedPosition = -1;
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(ShippingAddress address);
    }

    public AddressSelectionAdapter(List<ShippingAddress> addresses, OnAddressSelectedListener listener) {
        this.addresses = addresses;
        this.listener = listener;
        // Set default address as selected
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).isDefault()) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShippingAddress address = addresses.get(position);
        
        holder.tvName.setText(address.getFullName());
        holder.tvPhone.setText(address.getPhone());
        holder.tvAddress.setText(address.getFullAddress());
        
        if (address.isDefault()) {
            holder.tvDefault.setVisibility(View.VISIBLE);
        } else {
            holder.tvDefault.setVisibility(View.GONE);
        }
        
        boolean isSelected = position == selectedPosition;
        holder.radioButton.setChecked(isSelected);
        
        // Update card appearance based on selection
        com.google.android.material.card.MaterialCardView cardView = 
            (com.google.android.material.card.MaterialCardView) holder.itemView;
        if (isSelected) {
            cardView.setStrokeColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
            cardView.setStrokeWidth(4);
        } else {
            cardView.setStrokeColor(android.graphics.Color.TRANSPARENT);
            cardView.setStrokeWidth(2);
        }
        
        holder.itemView.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION) {
                int oldPosition = selectedPosition;
                selectedPosition = clickedPosition;
                
                // Update both old and new positions
                if (oldPosition != -1) {
                    notifyItemChanged(oldPosition);
                }
                notifyItemChanged(selectedPosition);
                
                if (listener != null) {
                    listener.onAddressSelected(address);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public ShippingAddress getSelectedAddress() {
        if (selectedPosition >= 0 && selectedPosition < addresses.size()) {
            return addresses.get(selectedPosition);
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;
        TextView tvName, tvPhone, tvAddress, tvDefault;

        ViewHolder(View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radioButton);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDefault = itemView.findViewById(R.id.tvDefault);
        }
    }
}
