package com.mikael.paymybuddy.web;

import com.mikael.paymybuddy.DTO.UserRegistrationDTO;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterWebController {

    private final UserService userService;

    public RegisterWebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String password,
                             HttpSession session,
                             Model model) {

        try {
            UserRegistrationDTO dto = new UserRegistrationDTO();
            dto.setUsername(username);
            dto.setEmail(email);
            dto.setPassword(password);

            User user = userService.registerUser(dto);

            session.setAttribute("userId", user.getId());
            return "redirect:/profile";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}