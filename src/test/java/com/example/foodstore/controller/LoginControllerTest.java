package com.example.foodstore.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class LoginControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test
    public void testLoginWithoutError() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/login"))
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    public void testLoginWithInvalidCredentialsError() throws Exception {
        mockMvc.perform(get("/login").param("error", "invalidCredentials"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/login"))
                .andExpect(model().attribute("error", "Email hoặc mật khẩu không đúng"));
    }

    @Test
    public void testLoginWithEmailNotRegisteredError() throws Exception {
        mockMvc.perform(get("/login").param("error", "emailNotRegistered"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/login"))
                .andExpect(model().attribute("error", "Email chưa được đăng ký"));
    }

    @Test
    public void testLoginWithAccountNotEnabledError() throws Exception {
        mockMvc.perform(get("/login").param("error", "accountNotEnabled"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/login"))
                .andExpect(model().attribute("error", "Tài khoản chưa được kích hoạt"));
    }
}
