package com.mikael.paymybuddy.web;

import com.mikael.paymybuddy.Model.Transaction;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.TransactionRepository;
import com.mikael.paymybuddy.Repository.UserRepository;
import com.mikael.paymybuddy.Service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransferWebControllerTest {

    @Autowired MockMvc mockMvc;

    @Autowired UserRepository userRepository;
    @Autowired TransactionRepository transactionRepository;

    @MockBean TransactionService transactionService;

    @Test
    void transfer_not_logged() throws Exception {
        mockMvc.perform(get("/transfer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void transfer_check_connections() throws Exception {

        User sender = new User();
        sender.setUsername("snd");
        sender.setEmail("snd@example.com");
        sender.setPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5T8n6G9JYxG8bN3qjv8n0b4oHn5lC");
        sender.setActive(true);
        sender.setBalance(new BigDecimal("100.00"));

        User friend = new User();
        friend.setUsername("friend");
        friend.setEmail("friend@example.com");
        friend.setPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5T8n6G9JYxG8bN3qjv8n0b4oHn5lC");
        friend.setActive(true);

        User friendInactive = new User();
        friendInactive.setUsername("friend2");
        friendInactive.setEmail("friend2@example.com");
        friendInactive.setPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5T8n6G9JYxG8bN3qjv8n0b4oHn5lC");
        friendInactive.setActive(false);

        userRepository.save(friend);
        userRepository.save(friendInactive);
        userRepository.save(sender);

        sender.getConnections().add(friend);
        sender.getConnections().add(friendInactive);
        userRepository.save(sender);

        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(friend);
        transaction.setAmount(new BigDecimal("12.50"));
        transaction.setDescription("Test");
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        mockMvc.perform(get("/transfer")
                        .sessionAttr("userId", sender.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("transfer"))
                .andExpect(model().attributeExists("connections"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attribute("connections", hasSize(1)))
                .andExpect(model().attribute("connections",
                        hasItem(allOf(
                                hasProperty("email", is(friend.getEmail())),
                                hasProperty("username", is(friend.getUsername()))
                        ))
                ))
                .andExpect(model().attribute("transactions", hasSize(1)));
    }

    @Test
    void transfer_transaction_not_logged() throws Exception {
        mockMvc.perform(post("/transfer")
                        .param("connectionId", "2")
                        .param("amount", "10.00")
                        .param("description", "Courses"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void transfer_transaction() throws Exception {
        Long userId = 1L;

        Mockito.doNothing()
                .when(transactionService).processTransaction(eq(userId), any());

        mockMvc.perform(post("/transfer")
                        .sessionAttr("userId", userId)
                        .param("connectionId", "2")
                        .param("amount", "10.00")
                        .param("description", "Courses"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transfer"))
                .andExpect(flash().attribute("success", "Transaction effectuée avec succès."));

        Mockito.verify(transactionService).processTransaction(eq(userId), any());
    }

    @Test
    void transfer_not_enough_money() throws Exception {
        Long userId = 1L;

        Mockito.doThrow(new IllegalArgumentException("Solde insuffisant"))
                .when(transactionService).processTransaction(eq(userId), any());

        mockMvc.perform(post("/transfer")
                        .sessionAttr("userId", userId)
                        .param("connectionId", "2")
                        .param("amount", "9999.00")
                        .param("description", "Erreur"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transfer"))
                .andExpect(flash().attribute("error", "Solde insuffisant"));

        Mockito.verify(transactionService).processTransaction(eq(userId), any());
    }
}