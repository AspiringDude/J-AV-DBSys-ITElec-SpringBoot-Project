package com.example.pos.controller;

import com.example.pos.model.Transaction;
import com.example.pos.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/transactions")
    public String viewAllTransactions(Model model) {
        // Fetch transactions with their items eagerly loaded
        List<Transaction> transactions = transactionService.getAllTransactionsWithItems();
        model.addAttribute("transactions", transactions);
        return "transactions";  // Your Thymeleaf template name
    }
}
