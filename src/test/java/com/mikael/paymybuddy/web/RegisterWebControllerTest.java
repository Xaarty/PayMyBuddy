package com.mikael.paymybuddy.web;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegisterWebControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Test
    void register_view() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void register() throws Exception {
        String username = "userrr";
        String email = "userrr@example.com";

        mockMvc.perform(post("/register")
                        .param("username", username)
                        .param("email", email)
                        .param("password", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(request().sessionAttribute("userId",
                        org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void register_duplication_fails() throws Exception {

        User mika = userRepository.findByEmail("mika@exemple.com").orElseThrow();

        String username = "new";

        mockMvc.perform(post("/register")
                        .param("username", username)
                        .param("email", mika.getEmail())
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));
    }
}