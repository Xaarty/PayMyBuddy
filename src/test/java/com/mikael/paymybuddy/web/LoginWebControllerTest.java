package com.mikael.paymybuddy.web;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LoginWebControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;

    @Test
    void login_view() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void login_incorrect() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "nnnnn@example.com")
                        .param("password", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void login_successful() throws Exception {

        String email = "mika@exemple.com";
        String password = "123123";

        mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(request().sessionAttribute("userId", notNullValue()));
    }

    @Test
    void login_inactive() throws Exception {

        User u = new User();
        u.setUsername("inactive");
        u.setEmail("inactive@example.com");
        u.setPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5T8n6G9JYxG8bN3qjv8n0b4oHn5lC");
        u.setActive(false);
        userRepository.save(u);

        mockMvc.perform(post("/login")
                        .param("email", u.getEmail())
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"));
    }
}
