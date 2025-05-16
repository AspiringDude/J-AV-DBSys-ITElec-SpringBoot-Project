package com.example.pos.repository;

import com.example.pos.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT DISTINCT t FROM Transaction t LEFT JOIN FETCH t.items")
    List<Transaction> findAllWithItems();

}
