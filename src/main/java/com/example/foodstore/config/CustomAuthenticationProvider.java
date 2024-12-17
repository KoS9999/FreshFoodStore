package com.example.foodstore.config;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationProviderCustom extends DaoAuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        UserDetails userDetails;
        try {
            userDetails = getUserDetailsService().loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            throw new UsernameNotFoundException("Email chưa được đăng ký");
        }
        try {
            return super.authenticate(authentication);
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Email hoặc mật khẩu không đúng");
        } catch (DisabledException ex) {
            throw new DisabledException("Tài khoản chưa được kích hoạt");
        }
    }


}


