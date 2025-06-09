package com.example.foodstore.config;

import com.example.foodstore.serviceImpl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/cart/update",
                        "/cart/remove","/api/payment/callback","/wishlist/**","/shop/**",
                        "/admin/products/delete-image/**","/api/payment/check-voucher/**","/api/payment/redeem/**",
                        "/reviews/**","/admin/reviews/**","/api/chatbot","/account/update", "/account/update-phone",
                        "/send-verification-code", "/webhook", "/account/cancel-order/**",
                        "/api/payment/createPayment","/api/payment/createCODOrder","/api/payment/createVNPayPayment"))
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                "/", "/index", "/register", "/login", "/confirmOTPregister",
                                "/forgotpassword", "/confirmOtpForgotPassword", "/newPassword",
                                "/web/**", "/product/category/**", "/uploads/**", "/new-products/**","/top-selling-products/**",
                                "/cart/add", "/cart/update", "/cart/remove", "/api/payment/callback",  "/news/**",
                                "/product-details/**","/shop/**","/about","/contact","/api/payment/check-voucher/**","/api/payment/redeem/**",
                                "/reviews/**","/api/chatbot","/error"
                        ).permitAll()
                        .requestMatchers(
                                "/account/**", "/wishlist/**", "cart/checkout"
                        ).hasRole("USER")
                        .requestMatchers("/admin/products/delete-image/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll() // Thay .authenticated()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/error")
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler((request, response, exception) -> {
                            if (exception instanceof UsernameNotFoundException) {
                                response.sendRedirect("/login?error=emailNotRegistered");
                            } else if (exception instanceof DisabledException) {
                                response.sendRedirect("/login?error=accountNotEnabled");
                            } else if (exception instanceof BadCredentialsException) {
                                response.sendRedirect("/login?error=invalidCredentials");
                            } else {
                                response.sendRedirect("/login?error=unknown");
                            }
                        })
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/index")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider() {
        CustomAuthenticationProvider provider = new CustomAuthenticationProvider();
        provider.setUserDetailsService(userDetailsServiceImpl);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            var roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            if (roles.contains("ROLE_ADMIN")) {
                response.sendRedirect("/admin/dashboard");
            } else {
                response.sendRedirect("/index");
            }
        };
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
