package com.example.foodstore.controller;

import com.example.foodstore.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class RegisterControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private RegisterController registerController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(registerController).build();
    }

    @Test
    public void testRegisterUserWithValidData() throws Exception {
        when(accountService.registerUser("John Doe", "john@example.com", "password123")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("name", "John Doe")
                        .param("email", "john@example.com")
                        .param("password", "password123")
                        .param("confirmpassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/confirmOTPregister"))
                .andExpect(model().attribute("message", "OTP has been sent to your email."));
    }

    @Test
    public void testRegisterUserWithMismatchedPasswords() throws Exception {
        mockMvc.perform(post("/register")
                        .param("name", "John Doe")
                        .param("email", "john@example.com")
                        .param("password", "password123")
                        .param("confirmpassword", "password321"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/register"))
                .andExpect(model().attribute("error", "Passwords do not match!"));
    }
    @Test
    public void testRegisterUserWithEmailExists() throws Exception {
        when(accountService.registerUser("John Doe", "existing@example.com", "password123")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "John Doe")
                        .param("email", "existing@example.com")
                        .param("password", "password123")
                        .param("confirmpassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/register"))
                .andExpect(model().attribute("error", "Email already exists."));
    }

    @Test
    public void testRegisterUserWithShortPassword() throws Exception {
        mockMvc.perform(post("/register")
                        .param("name", "John Doe")
                        .param("email", "john@example.com")
                        .param("password", "pass")
                        .param("confirmpassword", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/register"))
                .andExpect(model().attribute("error", "Password must be at least 8 characters long."));
    }

    @Test
    public void testConfirmOtpRegister() throws Exception {
        when(accountService.confirmOtpRegister("123456")).thenReturn(true);

        mockMvc.perform(post("/confirmOTPregister").param("otp", "123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/login"))
                .andExpect(model().attribute("message", "Registration successful!"));
    }
}
