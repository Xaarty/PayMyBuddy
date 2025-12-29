package com.mikael.paymybuddy.web;

import com.mikael.paymybuddy.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class RechargeWebController {

    private final UserService userService;

    public RechargeWebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/recharge")
    public String rechargePage(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        return "recharge";
    }

    @PostMapping("/recharge")
    public String doRecharge(@RequestParam BigDecimal amount,
                             HttpSession session,
                             RedirectAttributes ra) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            userService.rechargeAccount(userId, amount);
            ra.addFlashAttribute("success", "Recharge effectuée : +" + amount + "€");
            return "redirect:/recharge";
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/recharge";
        }
    }
}
