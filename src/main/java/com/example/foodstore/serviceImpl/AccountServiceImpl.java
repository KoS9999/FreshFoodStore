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
    private JavaMailSender mailSender;  // Thêm JavaMailSender để gửi email

    private Map<String, String> otpStorage = new HashMap<>();  // Lưu trữ OTP theo email

    @Override
    public boolean registerUser(String email, String password) {
        if (userRepository.findByEmail(email) != null) {
            return false;  // Email đã tồn tại
        }

        // Tạo OTP và gửi qua email
        String otp = generateOtp();
        otpStorage.put(email, otp);
        sendOtpToEmail(email, otp);

        // Tạo người dùng mới và gán quyền ROLE_USER
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));  // Mã hóa mật khẩu trước khi lưu
        user.setEnabled(false);  // Chưa kích hoạt

        // Gán quyền ROLE_USER cho người dùng
        Role userRole = roleRepository.findByName("ROLE_USER");
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);
        return true;
    }

    @Override
    public boolean confirmOtpRegister(String otp) {
        for (Map.Entry<String, String> entry : otpStorage.entrySet()) {
            if (entry.getValue().equals(otp)) {
                // Kích hoạt tài khoản người dùng
                User user = userRepository.findByEmail(entry.getKey());
                if (user != null) {
                    user.setEnabled(true);  // Kích hoạt tài khoản
                    userRepository.save(user);
                    otpStorage.remove(entry.getKey());  // Xóa OTP sau khi sử dụng
                    return true;
                }
            }
        }
        return false;  // OTP không hợp lệ
    }

    @Override
    public boolean sendResetPasswordEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return false;  // Không tìm thấy người dùng với email này
        }

        // Tạo một OTP mới và gửi qua email để đặt lại mật khẩu
        String otp = generateOtp();
        otpStorage.put(email, otp);
        sendOtpToEmail(email, otp);

        return true;  // Gửi thành công OTP để đặt lại mật khẩu
    }

    @Override
    public boolean validateOtp(String otp) {
        // Duyệt qua các OTP được lưu trong otpStorage
        for (Map.Entry<String, String> entry : otpStorage.entrySet()) {
            if (entry.getValue().equals(otp)) {
                // Nếu OTP hợp lệ, xóa OTP khỏi bộ nhớ tạm để tránh sử dụng lại
                otpStorage.remove(entry.getKey());
                return true;  // OTP hợp lệ
            }
        }
        return false;  // OTP không hợp lệ
    }


    @Override
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            // Mã hóa mật khẩu mới trước khi cập nhật vào cơ sở dữ liệu
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
    }


    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));  // Tạo OTP 6 chữ số
    }

    private void sendOtpToEmail(String email, String otp) {
        // Tạo nội dung email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP for Registration");
        message.setText("Dear user,\n\nYour OTP code is: " + otp + "\n\nThank you!");

        // Gửi email
        mailSender.send(message);
    }
}
