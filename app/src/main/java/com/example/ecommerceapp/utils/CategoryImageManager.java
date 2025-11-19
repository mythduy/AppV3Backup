package com.example.ecommerceapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CategoryImageManager {
    private static final String PREFS_NAME = "category_images";
    private static final String CATEGORY_IMAGES_DIR = "category_images";
    
    private Context context;
    private SharedPreferences prefs;
    
    // Map category name to asset filename
    private static final Map<String, String> ASSET_MAPPING = new HashMap<String, String>() {{
        put("Cảm biến", "category_sensor.jpg");
        put("Động cơ", "category_motor.jpg");
        put("LED", "category_led.jpg");
        put("Màn hình", "category_display.jpg");
        put("Vi điều khiển", "category_microcontroller.jpg");
        put("Module", "category_module.jpg");
        put("Linh kiện", "category_component.jpg");
        put("Nguồn", "category_power.jpg");
        put("Embedded", "category_embedded.jpg");
    }};
    
    public CategoryImageManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Get image path for a category
     */
    public String getCategoryImagePath(String categoryName) {
        // Check if custom image exists
        String customPath = prefs.getString(categoryName, null);
        if (customPath != null && new File(customPath).exists()) {
            return customPath;
        }
        
        // Check if migrated image exists
        File categoryDir = new File(context.getFilesDir(), CATEGORY_IMAGES_DIR);
        String filename = getCategoryFilename(categoryName);
        File imageFile = new File(categoryDir, filename);
        
        if (imageFile.exists()) {
            return imageFile.getAbsolutePath();
        }
        
        return null;
    }
    
    /**
     * Set custom image for a category
     */
    public void setCategoryImage(String categoryName, String imagePath) {
        prefs.edit().putString(categoryName, imagePath).apply();
    }
    
    /**
     * Save bitmap as category image
     */
    public String saveCategoryImage(String categoryName, Bitmap bitmap) {
        try {
            File directory = new File(context.getFilesDir(), CATEGORY_IMAGES_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            String filename = getCategoryFilename(categoryName);
            File file = new File(directory, filename);
            
            // Delete old file if exists
            if (file.exists()) {
                file.delete();
                android.util.Log.d("CategoryImageManager", "Deleted old image: " + file.getAbsolutePath());
            }
            
            // Save new image
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            
            String path = file.getAbsolutePath();
            setCategoryImage(categoryName, path);
            
            android.util.Log.d("CategoryImageManager", "Saved new image: " + path);
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("CategoryImageManager", "Error saving image: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Migrate all category images from assets to internal storage
     */
    public void migrateCategoryImagesFromAssets() {
        try {
            File directory = new File(context.getFilesDir(), CATEGORY_IMAGES_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            int migratedCount = 0;
            
            for (Map.Entry<String, String> entry : ASSET_MAPPING.entrySet()) {
                String categoryName = entry.getKey();
                String assetFilename = entry.getValue();
                
                // Check if already migrated
                String existingPath = getCategoryImagePath(categoryName);
                if (existingPath != null && new File(existingPath).exists()) {
                    continue;
                }
                
                // Copy from assets
                try {
                    InputStream is = context.getAssets().open("images/categories/" + assetFilename);
                    
                    String filename = getCategoryFilename(categoryName);
                    File outFile = new File(directory, filename);
                    FileOutputStream fos = new FileOutputStream(outFile);
                    
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    
                    fos.close();
                    is.close();
                    
                    // Save path
                    setCategoryImage(categoryName, outFile.getAbsolutePath());
                    migratedCount++;
                } catch (Exception e) {
                    // Asset file might not exist, skip
                }
            }
            
            android.util.Log.d("CategoryImageManager", "Migrated " + migratedCount + " category images");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Generate filename for category
     */
    private String getCategoryFilename(String categoryName) {
        String sanitized = categoryName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        return "category_" + sanitized + ".jpg";
    }
    
    /**
     * Delete category image
     */
    public void deleteCategoryImage(String categoryName) {
        String path = getCategoryImagePath(categoryName);
        if (path != null) {
            new File(path).delete();
            prefs.edit().remove(categoryName).apply();
        }
    }
}
