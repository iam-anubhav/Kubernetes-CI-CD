package com.example.bankapp.service;

import com.example.bankapp.dto.AccountResponse;
import com.example.bankapp.dto.AmountRequest;
import com.example.bankapp.dto.RegisterRequest;
import com.example.bankapp.dto.TransactionResponse;
import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.exception.BankAppException;
import com.example.bankapp.model.Account;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.BankTransactionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository,
                          BankTransactionRepository transactionRepository,
                          PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccountResponse register(RegisterRequest request) {
        if (accountRepository.existsByUsername(request.username())) {
            throw new BankAppException("Username already exists");
        }

        BigDecimal initialBalance = request.initialBalance() == null ? BigDecimal.ZERO : request.initialBalance();
        Account account = new Account(request.username(), passwordEncoder.encode(request.password()), initialBalance);
        accountRepository.save(account);
        return new AccountResponse(account.getUsername(), account.getBalance());
    }

    @Transactional(readOnly = true)
    public AccountResponse getMe(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BankAppException("Account not found"));
        return new AccountResponse(account.getUsername(), account.getBalance());
    }

    @Transactional
    public AccountResponse deposit(String username, AmountRequest request) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BankAppException("Account not found"));

        BigDecimal amount = request.amount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankAppException("Deposit amount must be positive");
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        transactionRepository.save(new BankTransaction("DEPOSIT", amount, username, username));
        return new AccountResponse(account.getUsername(), account.getBalance());
    }

    @Transactional
    public AccountResponse withdraw(String username, AmountRequest request) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BankAppException("Account not found"));

        BigDecimal amount = request.amount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankAppException("Withdraw amount must be positive");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BankAppException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        transactionRepository.save(new BankTransaction("WITHDRAW", amount, username, username));
        return new AccountResponse(account.getUsername(), account.getBalance());
    }

    @Transactional
    public AccountResponse transfer(String sourceUsername, TransferRequest request) {
        if (sourceUsername.equalsIgnoreCase(request.toUsername())) {
            throw new BankAppException("Source and target accounts must be different");
        }

        Account source = accountRepository.findByUsername(sourceUsername)
                .orElseThrow(() -> new BankAppException("Source account not found"));
        Account target = accountRepository.findByUsername(request.toUsername())
                .orElseThrow(() -> new BankAppException("Target account not found"));

        BigDecimal amount = request.amount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankAppException("Transfer amount must be positive");
        }
        if (source.getBalance().compareTo(amount) < 0) {
            throw new BankAppException("Insufficient balance");
        }

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));

        accountRepository.save(source);
        accountRepository.save(target);

        transactionRepository.save(new BankTransaction("TRANSFER_OUT", amount, sourceUsername, target.getUsername()));
        transactionRepository.save(new BankTransaction("TRANSFER_IN", amount, sourceUsername, target.getUsername()));

        return new AccountResponse(source.getUsername(), source.getBalance());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(String username) {
        return transactionRepository
                .findBySourceAccountOrTargetAccountOrderByCreatedAtDesc(username, username)
                .stream()
                .map(tx -> new TransactionResponse(
                        tx.getTransactionType(),
                        tx.getAmount(),
                        tx.getSourceAccount(),
                        tx.getTargetAccount(),
                        tx.getCreatedAt()))
                .toList();
    }
}
