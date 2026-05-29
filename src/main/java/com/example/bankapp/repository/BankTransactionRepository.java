package com.example.bankapp.repository;

import com.example.bankapp.model.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findBySourceAccountOrTargetAccountOrderByCreatedAtDesc(String sourceAccount, String targetAccount);
}
