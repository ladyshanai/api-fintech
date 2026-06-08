package com.fintech.api.service;

import com.fintech.api.client.DollarApiClient;
import com.fintech.api.client.DollarModel;
import com.fintech.api.dto.AccountRequest;
import com.fintech.api.dto.AccountResponse;
import com.fintech.api.entity.AccountEntity;
import com.fintech.api.entity.ClientEntity;
import com.fintech.api.enums.Currency;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests Parametrizados para AccountService
 * Mejora la cobertura al probar múltiples casos con menos código
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Parametrized Tests")
class AccountServiceParametrizedTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private DollarApiClient dollarApiClient;

    @InjectMocks
    private AccountService accountService;

    // ============================================
    // PARAMETRIZED TESTS - Múltiples valores
    // ============================================

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, 4L, 5L})
    @DisplayName("getAccountById: Debe retornar cuenta para IDs válidos")
    void testGetAccountByIdWithMultipleValidIds(Long accountId) {
        // Given
        AccountEntity account = createMockAccount(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // When
        AccountResponse response = accountService.getAccountById(accountId);

        // Then
        assertNotNull(response);
        assertEquals(accountId, response.accountId());
        verify(accountRepository, times(1)).findById(accountId);
    }

    @ParameterizedTest
    @ValueSource(longs = {999L, 1000L, 5000L, Long.MAX_VALUE})
    @DisplayName("getAccountById: Debe lanzar excepción para cuentas inexistentes")
    void testGetAccountByIdWithNonExistentIds(Long accountId) {
        // Given
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> accountService.getAccountById(accountId));
        verify(accountRepository, times(1)).findById(accountId);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1000.0, ARS, 0.0",
            "2, 500.0, ARS, 0.0",
            "3, 10000.0, ARS, 0.0",
            "4, 100000.0, ARS, 0.0",
    })
    @DisplayName("getAccountById: Cuentas ARS sin conversión")
    void testGetAccountByIdARSMultipleBalances(Long accountId, Double balance, String currency, Double expectedConversion) {
        // Given
        AccountEntity account = createMockAccountWithBalance(accountId, new BigDecimal(balance.toString()), Currency.ARS);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // When
        AccountResponse response = accountService.getAccountById(accountId);

        // Then
        assertEquals(new BigDecimal(balance.toString()), response.balance());
        assertEquals(BigDecimal.ZERO, response.balanceInPesos());
        assertEquals(Currency.ARS, response.currency());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 100.0, USD, 46.00",
            "2, 500.0, USD, 46.00",
            "3, 1000.0, USD, 46.00",
            "4, 5000.0, USD, 46.00",
    })
    @DisplayName("getAccountById: Cuentas USD con conversión correcta")
    void testGetAccountByIdUSDMultipleBalances(Long accountId, Double balance, String currency, String cotiza) {
        // Given
        AccountEntity account = createMockAccountWithBalance(accountId, new BigDecimal(balance.toString()), Currency.USD);
        DollarModel dollarModel = new DollarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(dollarApiClient.getCotizacion()).thenReturn(dollarModel);

        // When
        AccountResponse response = accountService.getAccountById(accountId);

        // Then
        assertEquals(new BigDecimal(balance.toString()), response.balance());
        // balance * 46.00 = expected
        BigDecimal expectedPesos = new BigDecimal(balance.toString()).multiply(new BigDecimal("46.00"));
        assertEquals(expectedPesos, response.balanceInPesos());
        assertEquals(Currency.USD, response.currency());
    }

    @ParameterizedTest
    @CsvSource({
            "ARS, 001",
            "USD, 002",
            "ARS, 003",
            "USD, 004",
    })
    @DisplayName("addAccount: Debe crear cuentas en diferentes monedas")
    void testAddAccountDifferentCurrencies(String currency, String accountNumber) {
        // Given
        ClientEntity client = createMockClient(1L);
        AccountRequest request = new AccountRequest(
                1L,
                accountNumber,
                currency,
                new BigDecimal("1000.00")
        );

        AccountEntity savedAccount = createMockAccountWithCurrency(1L, accountNumber, currency);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(savedAccount);

        // When
        AccountResponse response = accountService.addAccount(request);

        // Then
        assertNotNull(response);
        assertEquals(accountNumber, response.accountNumber());
        verify(clientRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(any());
    }

    @ParameterizedTest
    @CsvSource({
            "0.0",
            "1.0",
            "100.0",
            "1000.0",
            "10000.0",
            "100000.0",
            "999999.99",
    })
    @DisplayName("getAccountById: Debe retornar correctamente diferentes balances")
    void testGetAccountByIdDifferentBalances(Double balance) {
        // Given
        AccountEntity account = createMockAccountWithBalance(1L, new BigDecimal(balance.toString()), Currency.ARS);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When
        AccountResponse response = accountService.getAccountById(1L);

        // Then
        assertEquals(new BigDecimal(balance.toString()), response.balance());
        assertNotNull(response);
    }

    @ParameterizedTest
    @CsvSource({
            "true",
            "false",
    })
    @DisplayName("getAccountById: Debe retornar estado correcto de actividad")
    void testGetAccountByIdDifferentActiveStatus(Boolean active) {
        // Given
        AccountEntity account = createMockAccount(1L);
        account.setActive(active);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When
        AccountResponse response = accountService.getAccountById(1L);

        // Then
        assertEquals(active, response.active());
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 5L, 10L, 100L})
    @DisplayName("deleteById: Debe eliminar múltiples cuentas")
    void testDeleteByIdMultipleAccounts(Long accountId) {
        // When
        accountService.deleteById(accountId);

        // Then
        verify(accountRepository, times(1)).deleteById(accountId);
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    private AccountEntity createMockAccount(Long accountId) {
        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setAccountNumber("TEST-" + accountId);
        account.setCurrency(Currency.ARS);
        account.setBalance(new BigDecimal("1000.00"));
        account.setActive(true);
        account.setClient(createMockClient(1L));
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private AccountEntity createMockAccountWithBalance(Long accountId, BigDecimal balance, Currency currency) {
        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setAccountNumber("TEST-" + accountId);
        account.setCurrency(currency);
        account.setBalance(balance);
        account.setActive(true);
        account.setClient(createMockClient(1L));
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private AccountEntity createMockAccountWithCurrency(Long accountId, String accountNumber, String currency) {
        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);
        account.setAccountNumber(accountNumber);
        account.setCurrency(Currency.valueOf(currency));
        account.setBalance(new BigDecimal("1000.00"));
        account.setActive(true);
        account.setClient(createMockClient(1L));
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private ClientEntity createMockClient(Long clientId) {
        ClientEntity client = new ClientEntity();
        client.setId(1L);
        client.setFirstName("Test");
        client.setLastNameOrCompanyName("User");
        client.setEmail("test@test.com");
        client.setActive(true);
        return client;
    }
}

