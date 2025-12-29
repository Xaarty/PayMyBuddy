package com.mikael.paymybuddy.web;

import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.UserRepository;

import com.mikael.paymybuddy.Service.UserService;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ConnectionsWebController {

    private final UserRepository userRepository;
    private final UserService userService;

    public ConnectionsWebController(UserRepository userRepository,
                                 UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/connections")
    public String addConnectionPage() {
        return "connections";
    }

    @PostMapping("/connections")
    public String addConnection(@RequestParam String email,
                                HttpSession session,
                                RedirectAttributes ra) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User connection = userRepository.findByEmail(email)
                .orElse(null);

        if (connection == null) {
            ra.addFlashAttribute("error", "Utilisateur introuvable : " + email);
            return "redirect:/connections";
        }

        try {
            userService.addConnection(userId, connection.getId());
            ra.addFlashAttribute("success", "Relation ajoutée : " + connection.getUsername());
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/connections";
    }
}