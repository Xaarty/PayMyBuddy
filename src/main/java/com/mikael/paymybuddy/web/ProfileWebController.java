package com.mikael.paymybuddy.web;

import com.mikael.paymybuddy.DTO.ProfileUpdateDTO;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.UserRepository;
import com.mikael.paymybuddy.Service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileWebController {

    private final UserRepository userRepository;
    private final UserService userService;

    public ProfileWebController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable ou inactif"));

        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfilePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable ou inactif"));

        model.addAttribute("user", user);
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String doEditProfile(@RequestParam String username,
                                @RequestParam String email,
                                @RequestParam(required = false) String password,
                                HttpSession session,
                                RedirectAttributes ra) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            ProfileUpdateDTO dto = new ProfileUpdateDTO();
            dto.setUsername(username);
            dto.setEmail(email);
            dto.setPassword(password); // si vide, ton service ignore

            userService.updateProfile(userId, dto);

            ra.addFlashAttribute("success", "Profil mis à jour.");
            return "redirect:/profile";

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
