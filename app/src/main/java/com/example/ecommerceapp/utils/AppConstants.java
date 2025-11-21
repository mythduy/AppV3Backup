package com.example.ecommerceapp.utils;

/**
 * Application constants
 */
public class AppConstants {
    // OTP Configuration
    public static final int OTP_MIN_VALUE = 100000;
    public static final int OTP_MAX_VALUE = 999999;
    public static final long OTP_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes
    public static final long OTP_TIMER_INTERVAL_MS = 1000; // 1 second
    public static final int OTP_RESEND_COOLDOWN_SECONDS = 60;
    
    // Password Configuration
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 32;
    // Pattern: At least 8 characters, must contain uppercase, lowercase, and number
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    
    // Email Configuration
    public static final int EMAIL_CONNECTION_TIMEOUT_MS = 10000; // 10 seconds
    public static final int EMAIL_SEND_TIMEOUT_MS = 10000; // 10 seconds
    
    // Logging
    public static final boolean DEBUG_MODE = true; // TODO: Set false for production
    
    // Log Tags
    public static final String TAG_EMAIL = "EMAIL_DEBUG";
    public static final String TAG_EMAIL_ERROR = "EMAIL_ERROR";
    public static final String TAG_LOGIN = "LOGIN_DEBUG";
    public static final String TAG_OTP = "OTP_VERIFY";
    public static final String TAG_DB_LOGIN = "DB_LOGIN";
    public static final String TAG_DB_UPDATE = "DB_UPDATE";
    
    private AppConstants() {
        // Prevent instantiation
    }
}
