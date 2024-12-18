package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.User;
import com.example.foodstore.entity.Role;
import com.example.foodstore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testListUsers() throws Exception {
        List<User> users = Arrays.asList(
                new User(1L, "Ngoc Thong", "test1@gmail.com", "password123", "avatar.jpg", "123 Main St", "1234567890", null, true, true, null),
                new User(2L, "Jane Smith", "test@example.com", "password456", "avatar2.jpg", "456 Elm St", "0987654321", null, true, true, null)
        );

        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customer-list"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("users", users));

        verify(userService, times(1)).findAll();
    }

    @Test
    public void testEditUser() throws Exception {
        Long userId = 1L;
        User user = new User(userId, "Ngoc Thong", "test1@gmail.com", "password123", "avatar.jpg", "123 Main St", "1234567890", null, true, true, null);
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        mockMvc.perform(get("/admin/users/edit").param("id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/edit-user"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", user));

        verify(userService, times(1)).findById(userId);
    }

    @Test
    public void testUpdateUser() throws Exception {
        Long userId = 1L;
        User user = new User(userId, "Ngoc Thong", "test1@gmail.com", "password123", "avatar.jpg", "Updated Address", "1234567890", null, true, true, null);
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/admin/users/update")
                        .param("userId", String.valueOf(userId))
                        .param("name", "Ngoc Thong")
                        .param("email", "test1@gmail.com")
                        .param("password", "password123")
                        .param("avatar", "avatar.jpg")
                        .param("address", "Updated Address")
                        .param("phone", "1234567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    public void testBlockUser() throws Exception {
        Long userId = 1L;
        User user = new User(userId, "Ngoc Thong", "test1@gmail.com", "password123", "avatar.jpg", "123 Main St", "1234567890", null, true, true, null);

        when(userService.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/admin/users/block").param("id", String.valueOf(userId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(1)).save(any(User.class));
    }
    @Test
    public void testUnblockUser() throws Exception {
        Long userId = 1L;
        User user = new User(userId, "Ngoc Thong", "test1@gmail.com", "password123", "avatar.jpg", "123 Main St", "1234567890", null, true, false, null);

        when(userService.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/admin/users/unblock").param("id", String.valueOf(userId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
        verify(userService, times(1)).save(any(User.class));
    }
}
