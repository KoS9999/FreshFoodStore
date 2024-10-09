package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Role;
import com.example.foodstore.entity.User;
import com.example.foodstore.repository.RoleRepository;
import com.example.foodstore.repository.UserRepository;
import com.example.foodstore.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    // OTP storage - lưu tạm OTP để kiểm tra
    private Map<String, String> otpStorage = new HashMap<>();

    /**
     * Đăng ký tài khoản mới
     */
    @Override
    public boolean registerUser(String email, String password) {
        // Kiểm tra nếu email đã tồn tại
        if (userRepository.findByEmail(email).isPresent()) {
            return false;
        }

        // Tạo người dùng mới
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));  // Mã hóa mật khẩu
        user.setEnabled(false);  // Chưa kích hoạt

        Role userRole = roleRepository.findByName("ROLE_USER");
        user.setRoles(Collections.singleton(userRole));

        // Lưu người dùng vào cơ sở dữ liệu
        userRepository.save(user);

        // Gửi OTP qua email
        String otp = generateOtp();
        otpStorage.put(email, otp);
        sendOtpToEmail(email, otp);

        return true;
    }

    /**
     * Xác nhận OTP đăng ký
     */
    @Override
    public boolean confirmOtpRegister(String otp) {
        for (Map.Entry<String, String> entry : otpStorage.entrySet()) {
            if (entry.getValue().equals(otp)) {
                Optional<User> userOpt = userRepository.findByEmail(entry.getKey());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setEnabled(true);  // Kích hoạt tài khoản
                    userRepository.save(user);
                    otpStorage.remove(entry.getKey());  // Xóa OTP khỏi bộ nhớ
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean confirmOtpForgotPassword(String otp) {
        System.out.println("Checking OTP: " + otp);
        for (Map.Entry<String, String> entry : otpStorage.entrySet()) {
            if (entry.getValue().equals(otp)) {
                System.out.println("OTP is valid for user: " + entry.getKey());
                return true;
            }
        }
        System.out.println("Invalid OTP.");
        return false;
    }



    @Override
    public boolean updatePasswordWithOtp(String otp, String newPassword) {
        // Duyệt qua otpStorage để tìm OTP
        for (Map.Entry<String, String> entry : otpStorage.entrySet()) {
            if (entry.getValue().equals(otp)) {
                Optional<User> userOptional = userRepository.findByEmail(entry.getKey());
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    user.setPassword(passwordEncoder.encode(newPassword));  // Mã hóa mật khẩu mới
                    userRepository.save(user);  // Lưu lại thông tin người dùng
                    otpStorage.remove(entry.getKey());  // Xóa OTP sau khi sử dụng
                    return true;
                }
            }
        }
        return false;  // Nếu không tìm thấy OTP hoặc người dùng
    }




    /**
     * Gửi email quên mật khẩu với OTP
     */
    @Override
    public boolean sendResetPasswordEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        // Sinh OTP và gửi tới email
        String otp = generateOtp();
        otpStorage.put(email, otp);
        sendOtpToEmail(email, otp);

        return true;
    }

    /**
     * Xác nhận OTP quên mật khẩu
     */
    @Override
    public boolean validateOtp(String otp) {
        for (Map.Entry<String, String> entry : otpStorage.entrySet()) {
            if (entry.getValue().equals(otp)) {
                otpStorage.remove(entry.getKey());  // Xóa OTP sau khi sử dụng
                return true;
            }
        }
        return false;
    }

    /**
     * Cập nhật mật khẩu mới
     */
    @Override
    public void updatePassword(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));  // Mã hóa mật khẩu mới
            userRepository.save(user);
        }
    }

    /**
     * Xác thực người dùng
     */
    @Override
    public boolean validateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // So sánh mật khẩu nhập với mật khẩu đã mã hóa
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    /**
     * Sinh OTP gồm 6 chữ số ngẫu nhiên
     */
    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));  // Sinh OTP 6 chữ số
    }

    /**
     * Gửi OTP tới email người dùng
     */
    private void sendOtpToEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP for Registration or Password Reset");
        message.setText("Dear user,\n\nYour OTP code is: " + otp + "\n\nThank you!");
        mailSender.send(message);
    }
}
