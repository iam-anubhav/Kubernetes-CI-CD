package com.example.bankapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String toUsername,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount
) {}
