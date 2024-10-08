package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Role;
import com.example.foodstore.entity.User;
import com.example.foodstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        // Kiểm tra nếu người dùng không tồn tại
        User user = optionalUser.orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        // Lấy các vai trò của người dùng và chuyển đổi sang GrantedAuthority
        for (Role role : user.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        }

        // Tạo đối tượng UserDetails từ thông tin của người dùng
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                grantedAuthorities
        );
    }
}
