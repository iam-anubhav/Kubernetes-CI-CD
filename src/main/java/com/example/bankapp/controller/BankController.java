package com.example.bankapp.controller;

import com.example.bankapp.dto.AccountResponse;
import com.example.bankapp.dto.AmountRequest;
import com.example.bankapp.dto.RegisterRequest;
import com.example.bankapp.dto.TransactionResponse;
import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class BankController {

    private final AccountService accountService;

    public BankController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/")
    public ResponseEntity<Void> home() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/login"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<AccountResponse> register(@Valid @RequestBody RegisterRequest request) {
        AccountResponse response = accountService.register(request);
        return ResponseEntity.created(URI.create("/api/accounts/me")).body(response);
    }

    @GetMapping("/api/accounts/me")
    public AccountResponse me(Authentication authentication) {
        return accountService.getMe(authentication.getName());
    }

    @PostMapping("/api/accounts/me/deposit")
    public AccountResponse deposit(Authentication authentication, @Valid @RequestBody AmountRequest request) {
        return accountService.deposit(authentication.getName(), request);
    }

    @PostMapping("/api/accounts/me/withdraw")
    public AccountResponse withdraw(Authentication authentication, @Valid @RequestBody AmountRequest request) {
        return accountService.withdraw(authentication.getName(), request);
    }

    @PostMapping("/api/transfers")
    public AccountResponse transfer(Authentication authentication, @Valid @RequestBody TransferRequest request) {
        return accountService.transfer(authentication.getName(), request);
    }

    @GetMapping("/api/accounts/me/transactions")
    public List<TransactionResponse> transactions(Authentication authentication) {
        return accountService.getTransactions(authentication.getName());
    }
}
