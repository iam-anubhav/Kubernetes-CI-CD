package com.example.bankapp.service;

import com.example.bankapp.dto.AmountRequest;
import com.example.bankapp.dto.RegisterRequest;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.BankTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BankTransactionRepository transactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(any())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));
    }

    @Test
    void registerShouldEncodePasswordAndReturnBalance() {
        when(accountRepository.existsByUsername("alice")).thenReturn(false);

        var response = accountService.register(new RegisterRequest("alice", "password123", new BigDecimal("100.00")));

        assertEquals("alice", response.username());
        assertEquals(new BigDecimal("100.00"), response.balance());
        verify(accountRepository).save(any());
    }

    @Test
    void depositShouldIncreaseBalance() {
        when(accountRepository.findByUsername("alice")).thenReturn(java.util.Optional.of(new com.example.bankapp.model.Account("alice", "encoded", new BigDecimal("100.00"))));

        var response = accountService.deposit("alice", new AmountRequest(new BigDecimal("25.00")));

        assertEquals(new BigDecimal("125.00"), response.balance());
        verify(accountRepository, times(1)).save(any());
        verify(transactionRepository, times(1)).save(any());
    }
}
