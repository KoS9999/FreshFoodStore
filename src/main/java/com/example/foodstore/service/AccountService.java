package com.example.foodstore.service;

public interface AccountService {
    boolean sendResetPasswordEmail(String email);
    boolean validateOtp(String otp);
    void updatePassword(String email, String newPassword);
    boolean registerUser(String name, String email, String password);
    boolean confirmOtpRegister(String otp);

    boolean confirmOtpForgotPassword(String otp);
    boolean updatePasswordWithOtp(String otp, String newPassword);

    boolean validateUser(String email, String password);
}
