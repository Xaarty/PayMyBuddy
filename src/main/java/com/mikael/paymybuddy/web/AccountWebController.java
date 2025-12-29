package com.mikael.paymybuddy.web;

import com.mikael.paymybuddy.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountWebController {

    private final UserService userService;

    public AccountWebController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/account/deactivate")
    public String deactivateAccount(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        userService.deactivateUser(userId);

        session.invalidate();
        return "redirect:/login";
    }
}