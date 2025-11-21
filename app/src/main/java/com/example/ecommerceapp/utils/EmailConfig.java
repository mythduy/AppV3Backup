package com.example.ecommerceapp.utils;

/**
 * Email configuration class
 * In production, these should be stored in BuildConfig or remote config
 */
public class EmailConfig {
    // TODO: Move to BuildConfig in production
    private static final String FROM_EMAIL = "mythduy@gmail.com";
    private static final String FROM_PASSWORD = "ztkjwbuuodwasvdb"; // App Password
    
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    
    public static String getFromEmail() {
        return FROM_EMAIL;
    }
    
    public static String getFromPassword() {
        return FROM_PASSWORD;
    }
    
    public static String getSmtpHost() {
        return SMTP_HOST;
    }
    
    public static String getSmtpPort() {
        return SMTP_PORT;
    }
}
