package com.mikael.paymybuddy.web;

import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class AccountWebControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void deactivation_to_login() throws Exception {
        User mika = userRepository.findByEmail("mika@exemple.com").orElseThrow();
        Long id = mika.getId();

        mika.setActive(true);
        userRepository.save(mika);

        mockMvc.perform(post("/account/deactivate")
                        .sessionAttr("userId", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        User reloaded = userRepository.findById(id).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.isActive()).isFalse();
    }
}