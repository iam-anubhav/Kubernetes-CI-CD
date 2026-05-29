package com.example.bankapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String transactionType,
        BigDecimal amount,
        String sourceAccount,
        String targetAccount,
        LocalDateTime createdAt
) {}
