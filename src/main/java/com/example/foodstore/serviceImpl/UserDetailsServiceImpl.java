    package com.example.foodstore.serviceImpl;

    import com.example.foodstore.entity.Role;
    import com.example.foodstore.entity.User;
    import com.example.foodstore.repository.UserRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.authentication.DisabledException;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.stereotype.Service;

    import java.util.ArrayList;
    import java.util.Collection;
    import java.util.Optional;
    import java.util.Set;
    import java.util.stream.Collectors;

    @Service
    public class UserDetailsServiceImpl implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                throw new UsernameNotFoundException("Email chưa được đăng ký");
            }
            User user = userOptional.get();

            if (!user.isEnabled()) {
                throw new DisabledException("Tài khoản chưa được kích hoạt");
            }
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    user.isEnabled(), true, true, true,
                    getAuthorities(user.getRoles())
            );
        }

        private Collection<? extends GrantedAuthority> getAuthorities(Collection<Role> roles) {
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList());
        }

    }


