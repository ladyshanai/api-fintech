package com.fintech.api.service;

import com.fintech.api.client.DollarApiClient;
import com.fintech.api.client.DollarModel;
import com.fintech.api.dto.AccountRequest;
import com.fintech.api.dto.AccountResponse;
import com.fintech.api.entity.AccountEntity;
import com.fintech.api.enums.Currency;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final DollarApiClient dollarApiClient;

    public AccountService(AccountRepository accountRepository, ClientRepository clientRepository, DollarApiClient dollarApiClient) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.dollarApiClient = dollarApiClient;
    }


    public AccountResponse getAccountById(Long id) {
        var accountEntity = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found" + id));
        var balance = BigDecimal.ZERO;
        if (accountEntity.getCurrency().equals(Currency.USD)) {
            DollarModel dollarModel = dollarApiClient.getCotizacion();
            balance = accountEntity.getBalance().multiply(dollarModel.compra());
        }

        return (new AccountResponse(accountEntity.getAccountId(),
                accountEntity.getClient().getId(),
                accountEntity.getClient().getFirstName(),
                accountEntity.getAccountNumber(),
                accountEntity.getCurrency(),
                accountEntity.getBalance(),
                balance,
                accountEntity.getActive(),
                accountEntity.getCreatedAt(),
                accountEntity.getUpdatedAt()));
    }


    public List<AccountResponse> getAllAccounts() {
       return accountRepository.findAll()
               .stream().map(accountEntity -> {
                   var balance = BigDecimal.ZERO;
                   if (accountEntity.getCurrency().equals(Currency.USD)) {
                       DollarModel dollarModel = dollarApiClient.getCotizacion();
                       balance = accountEntity.getBalance().multiply(dollarModel.compra());
                   }
                   return new AccountResponse(accountEntity.getAccountId(),
                           accountEntity.getClient().getId(),
                           accountEntity.getClient().getFirstName(),
                           accountEntity.getAccountNumber(),
                           accountEntity.getCurrency(),
                           accountEntity.getBalance(),
                           balance,
                           accountEntity.getActive(),
                           accountEntity.getCreatedAt(),
                           accountEntity.getUpdatedAt());
               }).toList();

    }

    public AccountResponse addAccount(AccountRequest accountRequest) {
        var clientEntity = clientRepository.findById(accountRequest.clientId())
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + accountRequest.clientId()));

        var accountEntity = new AccountEntity();
        accountEntity.setAccountNumber(accountRequest.accountNumber());
        accountEntity.setCurrency(accountRequest.currency().equals("USD") ? Currency.USD : Currency.ARS);
        accountEntity.setBalance(accountRequest.balance());
        accountEntity.setActive(true);
        accountEntity.setClient(clientEntity);

        var savedAccount = accountRepository.save(accountEntity);
        return new AccountResponse(savedAccount.getAccountId(),
                savedAccount.getClient().getId(),
                savedAccount.getClient().getFirstName(),
                savedAccount.getAccountNumber(),
                savedAccount.getCurrency(),
                savedAccount.getBalance(),
                BigDecimal.ZERO,
                savedAccount.getActive(),
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

}