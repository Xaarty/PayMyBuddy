package com.mikael.paymybuddy.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikael.paymybuddy.DTO.TransactionRequestDTO;
import com.mikael.paymybuddy.Service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void transaction_success() throws Exception {
        Long senderId = 1L;
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setSenderId(senderId);
        dto.setReceiverId(2L);
        dto.setAmount(new BigDecimal("25.50"));
        dto.setDescription("Café");

        mockMvc.perform(post("/api/users/" + senderId + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction effectuée avec succès."));

        Mockito.verify(transactionService).processTransaction(eq(senderId), any(TransactionRequestDTO.class));
    }

    @Test
    void transaction_fails() throws Exception {
        Long senderId = 1L;
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setSenderId(senderId);
        dto.setReceiverId(3L);
        dto.setAmount(new BigDecimal("5000"));
        dto.setDescription("Grosse erreur");

        Mockito.doThrow(new IllegalArgumentException("Solde insuffisant"))
                .when(transactionService).processTransaction(eq(senderId), any(TransactionRequestDTO.class));

        mockMvc.perform(post("/api/users/" + senderId + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Solde insuffisant"));
    }

    @Test
    void missing_fields() throws Exception {
        Long senderId = 1L;
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setSenderId(senderId);
        System.out.println("ReceiverId: " + dto.getReceiverId() + ", Amount: " + dto.getAmount());
        mockMvc.perform(post("/api/users/" + senderId + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
