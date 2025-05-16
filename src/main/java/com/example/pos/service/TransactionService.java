package com.example.pos.service;

import com.example.pos.model.Transaction;
import com.example.pos.model.TransactionItem;
import com.example.pos.model.Product;
import com.example.pos.repository.TransactionRepository;
import com.example.pos.repository.TransactionItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionItemRepository transactionItemRepository;

    public TransactionService(TransactionRepository transactionRepository, TransactionItemRepository transactionItemRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionItemRepository = transactionItemRepository;
    }

    public void checkoutCart(String username, Map<Long, Integer> cart, Map<Long, Product> productMap) {
        double total = 0;
        List<TransactionItem> items = new ArrayList<>();

        Transaction transaction = new Transaction();
        transaction.setUsername(username);
        transaction.setTimestamp(LocalDateTime.now());

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();
            Product product = productMap.get(productId);

            TransactionItem item = new TransactionItem();
            item.setTransaction(transaction);
            item.setProductName(product.getName());
            item.setQuantity(quantity);
            item.setPrice(product.getPrice());

            total += quantity * product.getPrice();
            items.add(item);
        }

        transaction.setTotalPrice(total);
        transaction.setItems(items);

        transactionRepository.save(transaction); // saves transaction and its items
    }

    // Existing method
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // New method to fetch all transactions with items eagerly loaded
    public List<Transaction> getAllTransactionsWithItems() {
        return transactionRepository.findAllWithItems();
    }
}
