package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.User;
import com.example.foodstore.repository.UserRepository;
import com.example.foodstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;


    @ModelAttribute("user")
    public User user(Model model, Principal principal) {
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", user);
            return user;
        }
        return null;
    }

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }
}

