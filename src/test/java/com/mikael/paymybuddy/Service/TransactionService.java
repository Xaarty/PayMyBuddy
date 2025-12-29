package com.mikael.paymybuddy.Service;

import com.mikael.paymybuddy.DTO.TransactionRequestDTO;
import com.mikael.paymybuddy.Model.Transaction;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.TransactionRepository;
import com.mikael.paymybuddy.Repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private TransactionService transactionService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        transactionService = new TransactionService(userRepository, transactionRepository);
    }

    @Test
    void testValidTransaction() {
        User sender = new User("Alice", "alice@example.com", "pass");
        sender.setId(1L);
        sender.setActive(true);
        sender.setBalance(new BigDecimal("100.00"));

        User receiver = new User("Bob", "bob@example.com", "pass");
        receiver.setId(2L);
        sender.setActive(true);
        receiver.setBalance(new BigDecimal("50.00"));

        sender.getConnections().add(receiver);

        TransactionRequestDTO dto = new TransactionRequestDTO(1L, 2L, new BigDecimal("30.00"), "Cadeau");

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(receiver));

        transactionService.processTransaction(1L, dto);

        assertEquals(new BigDecimal("70.00"), sender.getBalance());
        assertEquals(new BigDecimal("80.00"), receiver.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(userRepository, times(1)).save(sender);
        verify(userRepository, times(1)).save(receiver);
    }

    @Test
    void testTransactionFailsIfReceiverNotInConnections() {
        User sender = new User("Alice", "alice@example.com", "pass");
        sender.setId(1L);
        sender.setActive(true);
        sender.setBalance(new BigDecimal("100.00"));

        User receiver = new User("Bob", "bob@example.com", "pass");
        receiver.setId(2L);
        sender.setActive(true);

        TransactionRequestDTO dto = new TransactionRequestDTO(1L, 2L, new BigDecimal("10.00"), "Essai");

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(receiver));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.processTransaction(1L, dto));
        assertEquals("Le destinataire n’est pas dans vos connexions.", ex.getMessage());
    }

    @Test
    void testTransactionFailsIfInsufficientBalance() {
        User sender = new User("Alice", "alice@example.com", "pass");
        sender.setId(1L);
        sender.setActive(true);
        sender.setBalance(new BigDecimal("5.00"));

        User receiver = new User("Bob", "bob@example.com", "pass");
        receiver.setId(2L);
        sender.setActive(true);
        sender.getConnections().add(receiver);

        TransactionRequestDTO dto = new TransactionRequestDTO(1L, 2L, new BigDecimal("10.00"), "Essai");

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(receiver));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.processTransaction(1L, dto));
        assertEquals("Solde insuffisant", ex.getMessage());
    }

    @Test
    void testTransactionFailsIfInvalidAmount() {
        User sender = new User("Alice", "alice@example.com", "pass");
        sender.setId(1L);
        sender.setActive(true);
        sender.setBalance(new BigDecimal("100.00"));

        User receiver = new User("Bob", "bob@example.com", "pass");
        receiver.setId(2L);
        sender.setActive(true);
        sender.getConnections().add(receiver);

        TransactionRequestDTO dto = new TransactionRequestDTO(1L, 2L, BigDecimal.ZERO, "Essai");

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(receiver));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.processTransaction(1L, dto));
        assertEquals("Montant invalide", ex.getMessage());
    }

    @Test
    void testTransactionFailsIfSenderNotFound() {
        TransactionRequestDTO dto = new TransactionRequestDTO(1L, 2L, new BigDecimal("20.00"), "Test");

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.processTransaction(1L, dto));
        assertEquals("Expéditeur introuvable", ex.getMessage());
    }

    @Test
    void testTransactionFailsIfReceiverNotFound() {
        User sender = new User("Alice", "alice@example.com", "pass");
        sender.setId(1L);
        sender.setActive(true);
        sender.setBalance(new BigDecimal("100.00"));

        TransactionRequestDTO dto = new TransactionRequestDTO(1L, 2L, new BigDecimal("20.00"), "Test");

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.processTransaction(1L, dto));
        assertEquals("Destinataire introuvable ou inactif", ex.getMessage());
    }
}
