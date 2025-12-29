package com.mikael.paymybuddy.Service;

import com.mikael.paymybuddy.DTO.TransactionRequestDTO;
import com.mikael.paymybuddy.Model.Transaction;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.TransactionRepository;
import com.mikael.paymybuddy.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void processTransaction(Long senderId, TransactionRequestDTO dto) {

        if (dto.getReceiverId() == null || dto.getAmount() == null) {
            throw new IllegalArgumentException("Le destinataire et le montant sont obligatoires");
        }

        User sender = userRepository.findByIdAndActiveTrue(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Expéditeur introuvable"));
        User receiver = userRepository.findByIdAndActiveTrue(dto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Destinataire introuvable ou inactif"));

        if (!sender.getConnections().contains(receiver)) {
            throw new IllegalArgumentException("Le destinataire n’est pas dans vos connexions.");
        }

        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Montant invalide");
        }

        if (sender.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new IllegalArgumentException("Solde insuffisant");
        }

        sender.setBalance(sender.getBalance().subtract(dto.getAmount()));
        receiver.setBalance(receiver.getBalance().add(dto.getAmount()));

        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(dto.getAmount());
        transaction.setDescription(dto.getDescription());
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);
        userRepository.save(sender);
        userRepository.save(receiver);
    }
}
