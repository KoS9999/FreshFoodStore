package com.example.foodstore.service;

import com.example.foodstore.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    Optional<User> findById(Long id);

    void save(User currentUser);

    User getLoggedInUser();


}

