package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.User;
import com.example.foodstore.repository.UserRepository;
import com.example.foodstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "admin/customer-list";
    }
    @GetMapping("/edit")
    public String editUser(@RequestParam("id") Long id, Model model) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
        } else {
            throw new RuntimeException("User not found with ID: " + id);
        }
        return "admin/edit-user";
    }
    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user) {
        // Kiểm tra nếu ID không null
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("User ID must not be null.");
        }

        Optional<User> existingUser = userService.findById(user.getUserId());
        if (existingUser.isPresent()) {
            User currentUser = existingUser.get();
            currentUser.setName(user.getName());
            currentUser.setPhone(user.getPhone());
            currentUser.setAddress(user.getAddress());
            userService.save(currentUser);
        } else {
            throw new RuntimeException("User not found with ID: " + user.getUserId());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/block")
    public String blockUser(@RequestParam("id") Long id) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            User currentUser = user.get();
            currentUser.setEnabled(false);
            userService.save(currentUser);
        }
        return "redirect:/admin/users";
    }
    @GetMapping("/unblock")
    public String unblockUser(@RequestParam("id") Long id) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            User currentUser = user.get();
            currentUser.setEnabled(true);
            userService.save(currentUser);
        }
        return "redirect:/admin/users";
    }
}


