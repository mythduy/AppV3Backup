package com.example.ecommerceapp.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ecommerceapp.models.Product;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDao - Handles all Product-related database operations
 */
public class ProductDao extends BaseDao {
    private static final String TABLE_PRODUCTS = "products";

    public ProductDao(SQLiteDatabase database) {
        super(database);
    }

    /**
     * Get all products
     */
    public List<Product> getAll() {
        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null);
        return extractProductsFromCursor(cursor);
    }

    /**
     * Get product by ID
     */
    public Product getById(int productId) {
        Cursor cursor = db.query(TABLE_PRODUCTS, null, "id=?",
                new String[]{String.valueOf(productId)}, null, null, null);

        List<Product> products = extractProductsFromCursor(cursor);
        return products.isEmpty() ? null : products.get(0);
    }

    /**
     * Search products by query
     */
    public List<Product> search(String query) {
        Cursor cursor = db.query(TABLE_PRODUCTS, null,
                "name LIKE ? OR description LIKE ? OR category LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"},
                null, null, null);
        return extractProductsFromCursor(cursor);
    }

    /**
     * Get products by category
     */
    public List<Product> getByCategory(String category) {
        Cursor cursor = db.query(TABLE_PRODUCTS, null, "category=?",
                new String[]{category}, null, null, null);
        return extractProductsFromCursor(cursor);
    }

    /**
     * Get all categories
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT DISTINCT category FROM " + TABLE_PRODUCTS, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        closeCursor(cursor);
        return categories;
    }

    /**
     * Get latest products
     */
    public List<Product> getLatest(int limit) {
        Cursor cursor = db.query(TABLE_PRODUCTS, null, "stock > ?",
                new String[]{"0"}, null, null, "id DESC", String.valueOf(limit));
        return extractProductsFromCursor(cursor);
    }

    /**
     * Get featured products
     */
    public List<Product> getFeatured(int limit) {
        Cursor cursor = db.query(TABLE_PRODUCTS, null, "is_featured = ? AND stock > ?",
                new String[]{"1", "0"}, null, null, "price DESC", String.valueOf(limit));

        List<Product> products = extractProductsFromCursor(cursor);
        
        // If no featured products, fallback to high price products
        if (products.isEmpty()) {
            cursor = db.query(TABLE_PRODUCTS, null, "stock > ?",
                    new String[]{"10"}, null, null, "price DESC", String.valueOf(limit));
            products = extractProductsFromCursor(cursor);
        }
        
        return products;
    }

    /**
     * Get bestseller products (random for now)
     */
    public List<Product> getBestsellers(int limit) {
        Cursor cursor = db.query(TABLE_PRODUCTS, null, "stock > ?",
                new String[]{"0"}, null, null, "RANDOM()", String.valueOf(limit));
        return extractProductsFromCursor(cursor);
    }

    /**
     * Get product count by category
     */
    public int getCountByCategory(String category) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS + " WHERE category=?",
                new String[]{category});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        closeCursor(cursor);
        return count;
    }

    /**
     * Advanced filter products
     */
    public List<Product> getFiltered(String category, String priceRange, 
                                     double minRating, boolean hasDiscount,
                                     boolean isHot, boolean isNew, 
                                     boolean inStockOnly, String sortBy) {
        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_PRODUCTS + " WHERE 1=1");
        List<String> selectionArgs = new ArrayList<>();
        
        // Category filter
        if (category != null && !category.equals("all")) {
            query.append(" AND category = ?");
            selectionArgs.add(category);
        }
        
        // Price range filter
        if (priceRange != null && !priceRange.equals("all")) {
            switch (priceRange) {
                case "under_50k":
                    query.append(" AND price < 50000");
                    break;
                case "50k_100k":
                    query.append(" AND price BETWEEN 50000 AND 100000");
                    break;
                case "100k_500k":
                    query.append(" AND price BETWEEN 100000 AND 500000");
                    break;
                case "500k_1m":
                    query.append(" AND price BETWEEN 500000 AND 1000000");
                    break;
                case "over_1m":
                    query.append(" AND price > 1000000");
                    break;
            }
        }
        
        // Rating filter
        if (minRating > 0) {
            query.append(" AND rating >= ?");
            selectionArgs.add(String.valueOf(minRating));
        }
        
        // Discount filter
        if (hasDiscount) {
            query.append(" AND discount > 0");
        }
        
        // Hot filter
        if (isHot) {
            query.append(" AND is_hot = 1");
        }
        
        // New filter
        if (isNew) {
            query.append(" AND is_new = 1");
        }
        
        // Stock filter
        if (inStockOnly) {
            query.append(" AND stock > 0");
        }
        
        // Sort
        String orderBy = "";
        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc":
                    orderBy = " ORDER BY price ASC";
                    break;
                case "price_desc":
                    orderBy = " ORDER BY price DESC";
                    break;
                case "name_asc":
                    orderBy = " ORDER BY name ASC";
                    break;
                case "rating_desc":
                    orderBy = " ORDER BY rating DESC";
                    break;
                case "newest":
                    orderBy = " ORDER BY id DESC";
                    break;
                default:
                    orderBy = " ORDER BY id DESC";
            }
        } else {
            orderBy = " ORDER BY id DESC";
        }
        query.append(orderBy);
        
        Cursor cursor = db.rawQuery(query.toString(), 
                                    selectionArgs.toArray(new String[0]));
        return extractProductsFromCursor(cursor);
    }

    /**
     * Add new product
     */
    public long add(Product product) {
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("description", product.getDescription());
        values.put("price", product.getPrice());
        values.put("category", product.getCategory());
        values.put("stock", product.getStock());
        values.put("image_url", product.getImageUrl());
        values.put("rating", product.getRating());
        values.put("sku", product.getSku());
        values.put("warranty", product.getWarranty());
        values.put("discount", product.getDiscount());
        values.put("is_new", product.isNew() ? 1 : 0);
        values.put("is_hot", product.isHot() ? 1 : 0);
        values.put("is_featured", product.isFeatured() ? 1 : 0);
        
        long id = db.insert(TABLE_PRODUCTS, null, values);
        
        // Auto-generate SKU if not provided
        if (id != -1 && (product.getSku() == null || product.getSku().isEmpty())) {
            ContentValues skuValues = new ContentValues();
            skuValues.put("sku", "PRD-" + String.format("%04d", id));
            db.update(TABLE_PRODUCTS, skuValues, "id = ?", new String[]{String.valueOf(id)});
        }
        
        return id;
    }

    /**
     * Update product
     */
    public boolean update(Product product) {
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("description", product.getDescription());
        values.put("price", product.getPrice());
        values.put("category", product.getCategory());
        values.put("stock", product.getStock());
        values.put("image_url", product.getImageUrl());
        values.put("rating", product.getRating());
        values.put("sku", product.getSku());
        values.put("warranty", product.getWarranty());
        values.put("discount", product.getDiscount());
        values.put("is_new", product.isNew() ? 1 : 0);
        values.put("is_hot", product.isHot() ? 1 : 0);
        values.put("is_featured", product.isFeatured() ? 1 : 0);
        
        int rows = db.update(TABLE_PRODUCTS, values, "id = ?", 
                new String[]{String.valueOf(product.getId())});
        return rows > 0;
    }

    /**
     * Delete product
     */
    public boolean delete(int productId) {
        // Delete from cart first (foreign key constraint)
        db.delete("cart", "product_id = ?", new String[]{String.valueOf(productId)});
        
        // Delete from order_items
        db.delete("order_items", "product_id = ?", new String[]{String.valueOf(productId)});
        
        // Delete from wishlist
        db.delete("wishlist", "product_id = ?", new String[]{String.valueOf(productId)});
        
        // Delete from reviews
        db.delete("reviews", "product_id = ?", new String[]{String.valueOf(productId)});
        
        // Delete product
        int rows = db.delete(TABLE_PRODUCTS, "id = ?", new String[]{String.valueOf(productId)});
        return rows > 0;
    }

    /**
     * Update product stock
     */
    public boolean updateStock(int productId, int newStock) {
        ContentValues values = new ContentValues();
        values.put("stock", newStock);
        int rows = db.update(TABLE_PRODUCTS, values, "id = ?", 
                new String[]{String.valueOf(productId)});
        return rows > 0;
    }

    /**
     * Update product rating
     */
    public boolean updateRating(int productId, double rating) {
        ContentValues values = new ContentValues();
        values.put("rating", rating);
        int rows = db.update(TABLE_PRODUCTS, values, "id = ?", 
                new String[]{String.valueOf(productId)});
        return rows > 0;
    }

    /**
     * Get product sold count
     */
    public int getSoldCount(int productId) {
        String query = "SELECT SUM(oi.quantity) " +
                      "FROM order_items oi " +
                      "INNER JOIN orders o ON oi.order_id = o.id " +
                      "WHERE oi.product_id = ? AND o.status = 'completed'";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(productId)});
        
        int soldCount = 0;
        if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
            soldCount = cursor.getInt(0);
        }
        closeCursor(cursor);
        return soldCount;
    }

    /**
     * Extract products from cursor
     */
    private List<Product> extractProductsFromCursor(Cursor cursor) {
        List<Product> products = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                product.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow("rating")));
                product.setSku(cursor.getString(cursor.getColumnIndexOrThrow("sku")));
                product.setWarranty(cursor.getString(cursor.getColumnIndexOrThrow("warranty")));
                product.setDiscount(cursor.getDouble(cursor.getColumnIndexOrThrow("discount")));
                product.setNew(cursor.getInt(cursor.getColumnIndexOrThrow("is_new")) == 1);
                product.setHot(cursor.getInt(cursor.getColumnIndexOrThrow("is_hot")) == 1);
                product.setFeatured(cursor.getInt(cursor.getColumnIndexOrThrow("is_featured")) == 1);
                products.add(product);
            } while (cursor.moveToNext());
        }
        closeCursor(cursor);
        return products;
    }
}
