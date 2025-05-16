package com.example.pos.controller;

import com.example.pos.model.User;
import com.example.pos.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public String showRegisterPage(@RequestParam(value = "error", required = false) String error,
                                   Model model) {
        if ("exists".equals(error)) {
            model.addAttribute("message", "Username already taken");
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String username,
                                  @RequestParam String password,
                                  HttpSession session) {
        if (userRepository.existsByUsername(username)) {
            return "redirect:/register?error=exists";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // Reminder: hash in real apps
        userRepository.save(newUser);

        session.setAttribute("username", username);
        return "redirect:/user-dashboard";
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session) {
        boolean isValidUser = userRepository.existsByUsernameAndPassword(username, password);
        if (isValidUser) {
            session.setAttribute("username", username);
            return "redirect:/user-dashboard";
        }
        return "redirect:/login?error=true";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
