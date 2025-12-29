package com.mikael.paymybuddy.web;

import com.mikael.paymybuddy.DTO.TransactionRequestDTO;
import com.mikael.paymybuddy.DTO.UserListDTO;
import com.mikael.paymybuddy.Model.Transaction;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.TransactionRepository;
import com.mikael.paymybuddy.Repository.UserRepository;
import com.mikael.paymybuddy.Service.TransactionService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class TransferWebController {

    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    public TransferWebController(UserRepository userRepository, TransactionService transactionService, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/transfer")
    public String transferPage(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable ou inactif"));

        List<UserListDTO> connections = user.getConnections().stream()
                .filter(User::isActive)
                .map(UserListDTO::fromUser)
                .toList();

        model.addAttribute("connections", connections);

        List<Transaction> transactions = transactionRepository.findBySenderId(userId);
        model.addAttribute("transactions", transactions);

        return "transfer";
    }

    @PostMapping("/transfer")
    public String doTransfer(@RequestParam Long connectionId,
                             @RequestParam BigDecimal amount,
                             @RequestParam String description,
                             HttpSession session,
                             RedirectAttributes ra) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            TransactionRequestDTO dto = new TransactionRequestDTO();
            dto.setReceiverId(connectionId);
            dto.setAmount(amount);
            dto.setDescription(description);

            transactionService.processTransaction(userId, dto);

            ra.addFlashAttribute("success", "Transaction effectuée avec succès.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/transfer";
    }
}