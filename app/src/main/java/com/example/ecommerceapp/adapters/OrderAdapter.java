package com.example.ecommerceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.models.Order;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context) {
        this.context = context;
    }

    public OrderAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvDate, tvTotal, tvPayment;
        com.google.android.material.chip.Chip chipStatus;
        com.google.android.material.button.MaterialButton btnViewDetail;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            tvPayment = itemView.findViewById(R.id.tvPayment);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
        }

        void bind(Order order) {
            tvOrderId.setText("Đơn hàng #" + order.getId());
            tvDate.setText(order.getOrderDate());
            tvTotal.setText(formatPrice(order.getTotalAmount()));
            tvPayment.setText("Thanh toán: " + order.getPaymentMethod());

            // Set status chip với màu sắc khác nhau
            chipStatus.setText(order.getStatus());
            setStatusChipStyle(order.getStatus());

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });

            btnViewDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        void setStatusChipStyle(String status) {
            int backgroundColor;
            int textColor = context.getColor(android.R.color.white);

            switch (status) {
                case "Chờ xác nhận":
                    backgroundColor = context.getColor(android.R.color.holo_orange_dark);
                    break;
                case "Đang giao hàng":
                    backgroundColor = context.getColor(android.R.color.holo_blue_dark);
                    break;
                case "Hoàn thành":
                    backgroundColor = context.getColor(android.R.color.holo_green_dark);
                    break;
                case "Đã hủy":
                    backgroundColor = context.getColor(android.R.color.holo_red_dark);
                    break;
                default:
                    backgroundColor = context.getColor(com.example.ecommerceapp.R.color.colorPrimary);
                    break;
            }

            chipStatus.setChipBackgroundColorResource(android.R.color.transparent);
            chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(backgroundColor));
            chipStatus.setTextColor(textColor);
        }

        String formatPrice(double price) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(price);
        }
    }
}