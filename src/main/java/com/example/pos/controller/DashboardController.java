package com.example.pos.controller;

import com.example.pos.model.CartItem;
import com.example.pos.model.Product;
import com.example.pos.model.Transaction;
import com.example.pos.repository.ProductRepository;
import com.example.pos.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class DashboardController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/user-dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        model.addAttribute("username", username);

        List<Product> activeProducts = productRepository.findAll()
            .stream()
            .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
            .toList();
        model.addAttribute("products", activeProducts);

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
        model.addAttribute("cart", cart);

        double cartTotal = cart.stream().mapToDouble(CartItem::getTotalPrice).sum();
        model.addAttribute("cartTotal", cartTotal);

        return "user-dashboard";
    }

    @PostMapping("/user-dashboard/add-to-cart")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || quantity <= 0) {
            redirectAttributes.addFlashAttribute("error", "Invalid product or quantity.");
            return "redirect:/user-dashboard";
        }

        if (product.getQuantity() < quantity) {
            redirectAttributes.addFlashAttribute("error", "Not enough stock for " + product.getName());
            return "redirect:/user-dashboard";
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        boolean found = false;
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(productId)) {
                int newQuantity = item.getQuantity() + quantity;
                if (newQuantity > product.getQuantity()) {
                    redirectAttributes.addFlashAttribute("error", "Not enough stock for " + product.getName());
                    return "redirect:/user-dashboard";
                }
                item.setQuantity(newQuantity);
                item.calculateTotalPrice();
                found = true;
                break;
            }
        }

        if (!found) cart.add(new CartItem(product, quantity));
        session.setAttribute("cart", cart);
        return "redirect:/user-dashboard";
    }

    @PostMapping("/user-dashboard/update-quantity")
    public String updateQuantity(@RequestParam Long productId,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        if (quantity < 1) quantity = 1;

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || quantity > product.getQuantity()) {
            redirectAttributes.addFlashAttribute("error", "Not enough stock for " + (product != null ? product.getName() : "product"));
            return "redirect:/user-dashboard";
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            for (CartItem item : cart) {
                if (item.getProduct().getId().equals(productId)) {
                    item.setQuantity(quantity);
                    item.calculateTotalPrice();
                    break;
                }
            }
            double total = cart.stream().mapToDouble(CartItem::getTotalPrice).sum();
            session.setAttribute("cartTotal", total);
        }
        return "redirect:/user-dashboard";
    }

    @PostMapping("/user-dashboard/remove-from-cart")
    public String removeFromCart(@RequestParam Long productId, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> item.getProduct().getId().equals(productId));
            session.setAttribute("cart", cart);
        }
        return "redirect:/user-dashboard";
    }

    @PostMapping("/user-dashboard/clear-cart")
    public String clearCart(HttpSession session) {
        session.removeAttribute("cart");
        session.removeAttribute("cartTotal");
        return "redirect:/user-dashboard";
    }

    @PostMapping("/user-dashboard/checkout")
    public String checkout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        String username = (String) session.getAttribute("username");

        if (cart == null || cart.isEmpty()) return "redirect:/user-dashboard";

        for (CartItem item : cart) {
            Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
            if (product == null || product.getQuantity() < item.getQuantity()) {
                redirectAttributes.addFlashAttribute("error", "Not enough stock for " + (product != null ? product.getName() : "product"));
                return "redirect:/user-dashboard";
            }
        }

        double total = 0;
        Map<Long, Integer> quantityMap = new HashMap<>();
        Map<Long, Product> productMap = new HashMap<>();

        for (CartItem item : cart) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            Long productId = product.getId();

            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            quantityMap.put(productId, quantity);
            productMap.put(productId, product);

            total += item.getTotalPrice();
        }

        transactionService.checkoutCart(username, quantityMap, productMap);

        model.addAttribute("username", username);
        model.addAttribute("total", total);
        model.addAttribute("timestamp", LocalDateTime.now());

        session.removeAttribute("cart");
        session.removeAttribute("cartTotal");

        redirectAttributes.addFlashAttribute("success", "Checkout successful!");
        return "redirect:/user-dashboard";
    }

    @GetMapping("/user-dashboard/reports")
    public String showReports(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        List<Transaction> transactions = transactionService.getAllTransactions();
        model.addAttribute("transactions", transactions);
        model.addAttribute("username", username);
        return "reports";
    }
}
