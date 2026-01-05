package com.mikael.paymybuddy.web;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RechargeWebControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;

    @Test
    void recharge_view() throws Exception {

        User user = new User();
        user.setUsername("mika");
        user.setEmail("mika@example.com");
        user.setPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5T8n6G9JYxG8bN3qjv8n0b4oHn5lC");
        user.setActive(true);
        userRepository.save(user);

        mockMvc.perform(get("/recharge").sessionAttr("userId", user.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("recharge"));
    }

    @Test
    void recharge_not_logged() throws Exception {
        mockMvc.perform(post("/recharge").param("amount", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void recharge_successful() throws Exception {
        User mika = userRepository.findByEmail("mika@exemple.com").orElseThrow();
        Long id = mika.getId();

        BigDecimal balance = userRepository.findById(id).orElseThrow().getBalance();

        mockMvc.perform(post("/recharge")
                        .sessionAttr("userId", id)
                        .param("amount", "10.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recharge"))
                .andExpect(flash().attributeExists("success"));

        User reloaded = userRepository.findById(id).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.getBalance())
                .isEqualByComparingTo(balance.add(new BigDecimal("10.00")));
    }
}