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
public class ForgotPasswordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private ForgotPasswordController forgotPasswordController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(forgotPasswordController).build();
    }

    @Test
    public void testHandleForgotPasswordWithValidEmail() throws Exception {
        when(accountService.sendResetPasswordEmail("john@example.com")).thenReturn(true);

        mockMvc.perform(post("/forgotpassword").param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/confirmOTPfgpw"))
                .andExpect(model().attribute("message", "An email has been sent to reset your password."));
    }
    @Test
    public void testHandleForgotPasswordWithInvalidEmail() throws Exception {
        when(accountService.sendResetPasswordEmail("unknown@example.com")).thenReturn(false);

        mockMvc.perform(post("/forgotpassword").param("email", "unknown@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/forgotpassword"))
                .andExpect(model().attribute("error", "Email not found."));
    }

    @Test
    public void testHandleNewPasswordWithValidOtp() throws Exception {
        when(accountService.updatePasswordWithOtp("123456", "newPassword123")).thenReturn(true);

        mockMvc.perform(post("/newPassword")
                        .param("otp", "123456")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "newPassword123"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/login"))
                .andExpect(model().attribute("message", "Your password has been updated successfully."));
    }

    @Test
    public void testHandleNewPasswordWithMismatchedPasswords() throws Exception {
        mockMvc.perform(post("/newPassword")
                        .param("otp", "123456")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "wrongPassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/newpassword"))
                .andExpect(model().attribute("error", "Passwords do not match!"));
    }

}
