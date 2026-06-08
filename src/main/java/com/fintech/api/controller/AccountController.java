package com.fintech.api.controller;

import com.fintech.api.dto.AccountRequest;
import com.fintech.api.dto.AccountResponse;
import com.fintech.api.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        var response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        var response = accountService.getAllAccounts();
        return ResponseEntity.ok(response);
    }

    @PostMapping()
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountRequest accountRequest) {
        var response = accountService.addAccount(accountRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccountById(@PathVariable Long id){
        accountService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
