package com.example.ecommerceapp.utils;

import android.util.Log;

/**
 * Logging utility to control debug logging in production
 */
public class LogUtil {
    
    public static void d(String tag, String message) {
        if (AppConstants.DEBUG_MODE) {
            Log.d(tag, message);
        }
    }
    
    public static void e(String tag, String message) {
        Log.e(tag, message);
    }
    
    public static void e(String tag, String message, Throwable throwable) {
        Log.e(tag, message, throwable);
    }
    
    public static void w(String tag, String message) {
        Log.w(tag, message);
    }
    
    public static void i(String tag, String message) {
        if (AppConstants.DEBUG_MODE) {
            Log.i(tag, message);
        }
    }
    
    private LogUtil() {
        // Prevent instantiation
    }
}
