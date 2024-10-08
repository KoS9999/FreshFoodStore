package com.example.foodstore.service;

import com.example.foodstore.entity.User;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUserById(Long id);

    User saveUser(User user);

    void deleteUser(Long id);

    User findByEmail(String email);

    List<User> findAll();
}

