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
class ConnectionsWebControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;

    @Test
    void get_connections() throws Exception {
        mockMvc.perform(get("/connections"))
                .andExpect(status().isOk())
                .andExpect(view().name("connections"));
    }

    @Test
    void connection_not_logged() throws Exception {
        mockMvc.perform(post("/connections")
                        .param("email", "123@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void connections_unknown_email() throws Exception {
        User user = userRepository.findByEmail("frank@example.com").orElseThrow();

        mockMvc.perform(post("/connections")
                        .sessionAttr("userId", user.getId())
                        .param("email", "nnnnn@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/connections"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void connections_success() throws Exception {

        User me = new User();
        me.setUsername("me");
        me.setEmail("me@example.com");
        me.setPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5T8n6G9JYxG8bN3qjv8n0b4oHn5lC");
        me.setActive(true);

        User friend = new User();
        friend.setUsername("friend");
        friend.setEmail("friend@example.com");
        friend.setPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5T8n6G9JYxG8bN3qjv8n0b4oHn5lC");
        friend.setActive(true);

        userRepository.save(friend);
        userRepository.save(me);

        mockMvc.perform(post("/connections")
                        .sessionAttr("userId", me.getId())
                        .param("email", friend.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/connections"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void connections_already_friend() throws Exception {

        User me = new User();
        me.setUsername("me");
        me.setEmail("me@example.com");
        me.setPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5T8n6G9JYxG8bN3qjv8n0b4oHn5lC");
        me.setActive(true);

        User friend = new User();
        friend.setUsername("friend");
        friend.setEmail("friend@example.com");
        friend.setPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5T8n6G9JYxG8bN3qjv8n0b4oHn5lC");
        friend.setActive(true);

        userRepository.save(friend);
        userRepository.save(me);

        mockMvc.perform(post("/connections")
                        .sessionAttr("userId", me.getId())
                        .param("email", friend.getEmail()))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/connections")
                        .sessionAttr("userId", me.getId())
                        .param("email", friend.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/connections"))
                .andExpect(flash().attributeExists("error"));
    }
}