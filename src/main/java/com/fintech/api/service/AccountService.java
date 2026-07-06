package com.fintech.api.service;

import com.fintech.api.client.DollarApiClient;
import com.fintech.api.client.DollarModel;
import com.fintech.api.dto.AccountRequest;
import com.fintech.api.dto.AccountResponse;
import com.fintech.api.entity.AccountEntity;
import com.fintech.api.enums.Currency;
import com.fintech.api.mapper.AccountMapper;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.ClientRepository;
import com.fintech.api.exception.ResourceNotFoundException;
import com.fintech.api.exception.ExternalServiceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final DollarApiClient dollarApiClient;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository,
                          ClientRepository clientRepository,
                          DollarApiClient dollarApiClient,
                          AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.dollarApiClient = dollarApiClient;
        this.accountMapper = accountMapper;
    }

    private BigDecimal getBalanceInPesos(Currency currency, BigDecimal balance) {
        if (!currency.equals(Currency.USD)) {
            return BigDecimal.ZERO;
        }
        try {
            DollarModel dollarModel = dollarApiClient.getCotizacion();
            return balance.multiply(dollarModel.compra());
        } catch (Exception ex) {
            throw new ExternalServiceException("No se pudo obtener cotización de dólares. Servicio temporal no disponible: " + ex.getMessage(), ex);
        }
    }


    public AccountResponse getAccountById(Long id) {
        var accountEntity = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found" + id));
        var balance = getBalanceInPesos(accountEntity.getCurrency(), accountEntity.getBalance());

        return accountMapper.toResponse(accountEntity, balance, accountEntity.getCreatedAt(), accountEntity.getUpdatedAt());
    }


    public List<AccountResponse> getAllAccounts() {
      return accountRepository.findAll()
               .stream().map(accountEntity -> {
                    var balance = getBalanceInPesos(accountEntity.getCurrency(), accountEntity.getBalance());
                    return accountMapper.toResponse(accountEntity, balance, accountEntity.getCreatedAt(), accountEntity.getUpdatedAt());
                }).toList();

     }

    public AccountResponse addAccount(AccountRequest accountRequest) {
       var clientEntity = clientRepository.findById(accountRequest.clientId())
               .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + accountRequest.clientId()));

       var accountEntity = accountMapper.toEntity(accountRequest);
       accountEntity.setAccountNumber(accountRequest.accountNumber());
       accountEntity.setBalance(accountRequest.balance());
       accountEntity.setActive(true);
       accountEntity.setClient(clientEntity);

       var savedAccount = accountRepository.save(accountEntity);
       var now = LocalDateTime.now();
       return accountMapper.toResponse(savedAccount, BigDecimal.ZERO, now, now);
    }

    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    public AccountResponse updateAccount(Long id, AccountRequest accountRequest) {
        var clientEntity = clientRepository.findById(accountRequest.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + accountRequest.clientId()));

        var accountEntity = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        accountEntity.setAccountNumber(accountRequest.accountNumber());
        accountEntity.setCurrency(accountMapper.toEntity(accountRequest).getCurrency());
        accountEntity.setBalance(accountRequest.balance());
        accountEntity.setClient(clientEntity);
        accountEntity.setUpdatedAt(LocalDateTime.now());

        var updatedAccount = accountRepository.save(accountEntity);

        var balanceInPesos = getBalanceInPesos(updatedAccount.getCurrency(), updatedAccount.getBalance());

        return accountMapper.toResponse(updatedAccount, balanceInPesos, updatedAccount.getCreatedAt(), updatedAccount.getUpdatedAt());
    }
}