package com.example.bankapp.dto;

import java.math.BigDecimal;

public record AccountResponse(
        String username,
        BigDecimal balance
) {}
