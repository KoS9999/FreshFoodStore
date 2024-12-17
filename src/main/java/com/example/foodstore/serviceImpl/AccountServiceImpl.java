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

import java.util.*;

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

    private Map<String, String> otpStorage = new HashMap<>();
    private Map<String, User> tempUserStorage = new HashMap<>();

    /**
     * Đăng ký tài khoản mới
     */
    @Override
    public boolean registerUser(String name, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            return false;
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(false);

        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole = roleRepository.save(userRole);
        }
        user.setRoles(Collections.singleton(userRole));
        tempUserStorage.put(email, user);
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
                String email = entry.getKey();
                User tempUser = tempUserStorage.get(email);
                if (tempUser != null) {
                    Collection<Role> managedRoles = new ArrayList<>();
                    for (Role role : tempUser.getRoles()) {
                        Role managedRole = roleRepository.findByName(role.getName());
                        if (managedRole == null) {
                            managedRole = roleRepository.save(role);
                        }
                        managedRoles.add(managedRole);
                    }
                    tempUser.setRoles(managedRoles);
                    tempUser.setEnabled(true);
                    userRepository.save(tempUser);
                    otpStorage.remove(email);
                    tempUserStorage.remove(email);

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
        for (Map.Entry<String, String> entry : otpStorage.entrySet()) {
            if (entry.getValue().equals(otp)) {
                Optional<User> userOptional = userRepository.findByEmail(entry.getKey());
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    otpStorage.remove(entry.getKey());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * quên mật khẩu với OTP
     */
    @Override
    public boolean sendResetPasswordEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        String otp = generateOtp();
        otpStorage.put(email, otp);
        sendOtpToEmail(email, otp);

        return true;
    }

    @Override
    public boolean validateOtp(String otp) {
        for (Map.Entry<String, String> entry : otpStorage.entrySet()) {
            if (entry.getValue().equals(otp)) {
                otpStorage.remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    @Override
    public void updatePassword(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
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
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

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
