package com.mikael.paymybuddy.web;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfileWebControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;

    @Test
    void profile_not_logged() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void profile_view() throws Exception {
        User user = userRepository.findByEmail("mika@exemple.com").orElseThrow();

        mockMvc.perform(get("/profile")
                        .sessionAttr("userId", user.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void edit_redirect() throws Exception {
        User user = userRepository.findByEmail("mika@exemple.com").orElseThrow();

        mockMvc.perform(get("/profile/edit")
                        .sessionAttr("userId", user.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-edit"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void edit_profile() throws Exception {
        User mika = userRepository.findByEmail("mika@exemple.com").orElseThrow();
        Long id = mika.getId();

        String newUsername = "mika2";
        String newEmail = "mika2@exemple.com";

        mockMvc.perform(post("/profile/edit")
                        .sessionAttr("userId", id)
                        .param("username", newUsername)
                        .param("email", newEmail)
                        .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        User reloaded = userRepository.findById(id).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.getEmail()).isEqualTo(newEmail);
        org.assertj.core.api.Assertions.assertThat(reloaded.getUsername()).isEqualTo(newUsername);
    }
}