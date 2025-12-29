package com.mikael.paymybuddy.web;

import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.UserRepository;
import com.mikael.paymybuddy.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginWebController {

    private final UserService userService;
    private final UserRepository userRepository;

    public LoginWebController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {

        System.out.println("POST /login email=" + email);
        boolean ok = userService.userAuthenticate(email, password);
        System.out.println("auth ok=" + ok);

        if (!ok) {
            model.addAttribute("error", "Email ou mot de passe invalide");
            return "login";
        }

        User user = userRepository.findByEmail(email).orElseThrow();
        System.out.println("userId=" + user.getId() + " active=" + user.isActive());
        if (!user.isActive()) {
            model.addAttribute("error", "Compte inactif");
            return "login";
        }

        session.setAttribute("userId", user.getId());
        System.out.println("session userId set -> redirect /profile");
        return "redirect:/profile";
    }
}