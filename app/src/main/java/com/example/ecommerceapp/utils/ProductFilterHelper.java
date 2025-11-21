package com.example.ecommerceapp.utils;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.database.DatabaseHelper;
import com.example.ecommerceapp.models.Product;
import java.util.List;

public class ProductFilterHelper {
    private Activity activity;
    private DatabaseHelper dbHelper;
    private OnFilterAppliedListener listener;
    
    // Filter state
    private String selectedCategory = "all";
    private String selectedPriceRange = "all";
    private double selectedMinRating = 0.0;
    private boolean filterDiscount = false;
    private boolean filterHot = false;
    private boolean filterNew = false;
    private boolean filterInStockOnly = true;
    private String selectedSortBy = "newest";
    
    public interface OnFilterAppliedListener {
        void onFilterApplied(List<Product> filteredProducts);
    }
    
    public ProductFilterHelper(Activity activity, DatabaseHelper dbHelper, OnFilterAppliedListener listener) {
        this.activity = activity;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }
    
    public void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
        bottomSheetDialog.setContentView(view);
        
        // Find views
        ChipGroup chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        ChipGroup chipGroupPrice = view.findViewById(R.id.chipGroupPrice);
        ChipGroup chipGroupRating = view.findViewById(R.id.chipGroupRating);
        ChipGroup chipGroupSpecial = view.findViewById(R.id.chipGroupSpecial);
        MaterialCheckBox cbInStockOnly = view.findViewById(R.id.cbInStockOnly);
        MaterialButton btnClearFilter = view.findViewById(R.id.btnClearFilter);
        MaterialButton btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        ImageView btnClose = view.findViewById(R.id.btnClose);
        
        // Setup Category chips
        String[] categories = {"Tất cả", "Vi điều khiển", "Cảm biến", "Màn hình", "Module", 
                              "Linh kiện", "Động cơ", "LED", "Nguồn", "Máy tính nhúng", "Module WiFi"};
        String[] categoryValues = {"all", "Vi điều khiển", "Cảm biến", "Màn hình", "Module",
                                  "Linh kiện", "Động cơ", "LED", "Nguồn", "Máy tính nhúng", "Module WiFi"};
        addChips(chipGroupCategory, categories, categoryValues, selectedCategory);
        
        // Setup Price chips
        String[] priceRanges = {"Tất cả", "Dưới 50k", "50k - 100k", "100k - 500k", "500k - 1M", "Trên 1M"};
        String[] priceValues = {"all", "under_50k", "50k_100k", "100k_500k", "500k_1m", "over_1m"};
        addChips(chipGroupPrice, priceRanges, priceValues, selectedPriceRange);
        
        // Setup Rating chips
        String[] ratings = {"Tất cả", "⭐⭐⭐⭐⭐ 5 sao", "⭐⭐⭐⭐ 4+ sao", "⭐⭐⭐ 3+ sao"};
        String[] ratingValues = {"0", "5", "4", "3"};
        addChips(chipGroupRating, ratings, ratingValues, String.valueOf((int) selectedMinRating));
        
        // Setup Special chips (multi-select)
        addCheckableChip(chipGroupSpecial, "🔥 Đang giảm giá", filterDiscount);
        addCheckableChip(chipGroupSpecial, "🔥 Sản phẩm HOT", filterHot);
        addCheckableChip(chipGroupSpecial, "✨ Sản phẩm MỚI", filterNew);
        
        // Setup stock checkbox
        cbInStockOnly.setChecked(filterInStockOnly);
        
        // Close button
        btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        
        // Clear filter button
        btnClearFilter.setOnClickListener(v -> {
            clearFilters();
            bottomSheetDialog.dismiss();
            applyFilters();
        });
        
        // Apply filter button
        btnApplyFilter.setOnClickListener(v -> {
            // Get selected category
            int selectedCategoryId = chipGroupCategory.getCheckedChipId();
            if (selectedCategoryId != View.NO_ID) {
                Chip selectedChip = view.findViewById(selectedCategoryId);
                int index = chipGroupCategory.indexOfChild(selectedChip);
                selectedCategory = categoryValues[index];
            }
            
            // Get selected price range
            int selectedPriceId = chipGroupPrice.getCheckedChipId();
            if (selectedPriceId != View.NO_ID) {
                Chip selectedChip = view.findViewById(selectedPriceId);
                int index = chipGroupPrice.indexOfChild(selectedChip);
                selectedPriceRange = priceValues[index];
            }
            
            // Get selected rating
            int selectedRatingId = chipGroupRating.getCheckedChipId();
            if (selectedRatingId != View.NO_ID) {
                Chip selectedChip = view.findViewById(selectedRatingId);
                int index = chipGroupRating.indexOfChild(selectedChip);
                selectedMinRating = Double.parseDouble(ratingValues[index]);
            }
            
            // Get special filters
            filterDiscount = ((Chip) chipGroupSpecial.getChildAt(0)).isChecked();
            filterHot = ((Chip) chipGroupSpecial.getChildAt(1)).isChecked();
            filterNew = ((Chip) chipGroupSpecial.getChildAt(2)).isChecked();
            
            // Get stock filter
            filterInStockOnly = cbInStockOnly.isChecked();
            
            bottomSheetDialog.dismiss();
            applyFilters();
        });
        
        bottomSheetDialog.show();
    }
    
    public void showSortDialog() {
        String[] sortOptions = {"Mới nhất", "Giá tăng dần", "Giá giảm dần", "Tên A-Z", "Đánh giá cao nhất"};
        String[] sortValues = {"newest", "price_asc", "price_desc", "name_asc", "rating_desc"};
        
        int selectedIndex = 0;
        for (int i = 0; i < sortValues.length; i++) {
            if (sortValues[i].equals(selectedSortBy)) {
                selectedIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(activity)
            .setTitle("Sắp xếp theo")
            .setSingleChoiceItems(sortOptions, selectedIndex, (dialog, which) -> {
                selectedSortBy = sortValues[which];
                dialog.dismiss();
                applyFilters();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void addChips(ChipGroup chipGroup, String[] labels, String[] values, String selectedValue) {
        for (int i = 0; i < labels.length; i++) {
            Chip chip = new Chip(activity);
            chip.setText(labels[i]);
            chip.setCheckable(true);
            chip.setChecked(values[i].equals(selectedValue));
            chipGroup.addView(chip);
        }
    }
    
    private void addCheckableChip(ChipGroup chipGroup, String label, boolean checked) {
        Chip chip = new Chip(activity);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChecked(checked);
        chipGroup.addView(chip);
    }
    
    private void clearFilters() {
        selectedCategory = "all";
        selectedPriceRange = "all";
        selectedMinRating = 0.0;
        filterDiscount = false;
        filterHot = false;
        filterNew = false;
        filterInStockOnly = true;
        selectedSortBy = "newest";
    }
    
    private void applyFilters() {
        List<Product> filteredProducts = dbHelper.getFilteredProducts(
            selectedCategory, selectedPriceRange, selectedMinRating,
            filterDiscount, filterHot, filterNew, filterInStockOnly, selectedSortBy
        );
        
        if (listener != null) {
            listener.onFilterApplied(filteredProducts);
        }
    }
    
    // Public method to get current filters and apply
    public void refreshProducts() {
        applyFilters();
    }
}
