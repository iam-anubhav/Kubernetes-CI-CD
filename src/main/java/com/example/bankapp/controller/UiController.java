package com.example.bankapp.controller;

import com.example.bankapp.dto.AmountRequest;
import com.example.bankapp.dto.RegisterRequest;
import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.exception.BankAppException;
import com.example.bankapp.service.AccountService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class UiController {

    private final AccountService accountService;

    public UiController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterForm form, RedirectAttributes redirectAttributes) {
        try {
            accountService.register(new RegisterRequest(form.getUsername(), form.getPassword(), form.getInitialBalance()));
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful. Please log in.");
            return "redirect:/login";
        } catch (BankAppException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("account", accountService.getMe(authentication.getName()));
        model.addAttribute("depositForm", new AmountForm());
        model.addAttribute("withdrawForm", new AmountForm());
        model.addAttribute("transferForm", new TransferForm());
        return "dashboard";
    }

    @PostMapping("/dashboard/deposit")
    public String deposit(Authentication authentication,
                          @ModelAttribute AmountForm depositForm,
                          RedirectAttributes redirectAttributes) {
        try {
            accountService.deposit(authentication.getName(), new AmountRequest(depositForm.getAmount()));
            redirectAttributes.addFlashAttribute("successMessage", "Deposit completed successfully.");
        } catch (BankAppException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/withdraw")
    public String withdraw(Authentication authentication,
                           @ModelAttribute AmountForm withdrawForm,
                           RedirectAttributes redirectAttributes) {
        try {
            accountService.withdraw(authentication.getName(), new AmountRequest(withdrawForm.getAmount()));
            redirectAttributes.addFlashAttribute("successMessage", "Withdraw completed successfully.");
        } catch (BankAppException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/transfer")
    public String transfer(Authentication authentication,
                           @ModelAttribute TransferForm transferForm,
                           RedirectAttributes redirectAttributes) {
        try {
            accountService.transfer(authentication.getName(),
                    new TransferRequest(transferForm.getToUsername(), transferForm.getAmount()));
            redirectAttributes.addFlashAttribute("successMessage", "Transfer completed successfully.");
        } catch (BankAppException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/transactions")
    public String transactions(Authentication authentication, Model model) {
        model.addAttribute("account", accountService.getMe(authentication.getName()));
        model.addAttribute("transactions", accountService.getTransactions(authentication.getName()));
        return "transactions";
    }

    public static class RegisterForm {
        @NotBlank
        private String username;

        @NotBlank
        private String password;

        @NotNull
        @DecimalMin(value = "0.00", inclusive = true)
        private BigDecimal initialBalance = BigDecimal.ZERO;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public BigDecimal getInitialBalance() {
            return initialBalance;
        }

        public void setInitialBalance(BigDecimal initialBalance) {
            this.initialBalance = initialBalance;
        }
    }

    public static class AmountForm {
        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        private BigDecimal amount = BigDecimal.ZERO;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    public static class TransferForm {
        @NotBlank
        private String toUsername;

        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        private BigDecimal amount = BigDecimal.ZERO;

        public String getToUsername() {
            return toUsername;
        }

        public void setToUsername(String toUsername) {
            this.toUsername = toUsername;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
