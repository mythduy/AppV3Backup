package com.example.ecommerceapp.models;

public class Review {
    private int id;
    private int productId;
    private int userId;
    private String userName;
    private String userAvatar;
    private float rating;
    private String comment;
    private String reviewDate;

    public Review() {}

    public Review(int id, int productId, int userId, String userName, String userAvatar,
                  float rating, String comment, String reviewDate) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getReviewDate() { return reviewDate; }
    public void setReviewDate(String reviewDate) { this.reviewDate = reviewDate; }

    // Helper method to get rating stars as string
    public String getRatingStars() {
        StringBuilder stars = new StringBuilder();
        int fullStars = (int) rating;
        boolean hasHalfStar = (rating - fullStars) >= 0.5;
        
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        if (hasHalfStar) {
            stars.append("⯨");
        }
        int emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
        for (int i = 0; i < emptyStars; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    // Get formatted date
    public String getFormattedDate() {
        if (reviewDate == null || reviewDate.isEmpty()) {
            return "";
        }
        // Format: dd/MM/yyyy HH:mm -> dd/MM/yyyy
        if (reviewDate.contains(" ")) {
            return reviewDate.split(" ")[0];
        }
        return reviewDate;
    }
}
