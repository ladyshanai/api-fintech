package com.fintech.api.service;

import com.fintech.api.client.DolarApiClient;
import com.fintech.api.client.DolarModel;
import com.fintech.api.dto.AccountRequest;
import com.fintech.api.dto.AccountResponse;
import com.fintech.api.entity.AccountEntity;
import com.fintech.api.entity.ClientEntity;
import com.fintech.api.enums.Currency;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests Unitarios para AccountService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private DolarApiClient dolarApiClient;

    @InjectMocks
    private AccountService accountService;

    private ClientEntity mockClient;
    private AccountEntity mockAccountARS;
    private AccountEntity mockAccountUSD;
    private DolarModel mockDolarModel;

    @BeforeEach
    void setUp() {
        // Preparar cliente mock
        mockClient = new ClientEntity();
        mockClient.setId(1L);
        mockClient.setFirstName("Juan");
        mockClient.setLastNameOrCompanyName("Pérez");
        mockClient.setDocumentNumber("12345678");
        mockClient.setEmail("juan@test.com");
        mockClient.setActive(true);

        // Preparar cuenta en ARS
        mockAccountARS = new AccountEntity();
        mockAccountARS.setAccountId(1L);
        mockAccountARS.setAccountNumber("001-ARS");
        mockAccountARS.setCurrency(Currency.ARS);
        mockAccountARS.setBalance(new BigDecimal("50000.00"));
        mockAccountARS.setActive(true);
        mockAccountARS.setClient(mockClient);
        mockAccountARS.setCreatedAt(LocalDateTime.now());
        mockAccountARS.setUpdatedAt(LocalDateTime.now());

        // Preparar cuenta en USD
        mockAccountUSD = new AccountEntity();
        mockAccountUSD.setAccountId(2L);
        mockAccountUSD.setAccountNumber("002-USD");
        mockAccountUSD.setCurrency(Currency.USD);
        mockAccountUSD.setBalance(new BigDecimal("1000.00"));
        mockAccountUSD.setActive(true);
        mockAccountUSD.setClient(mockClient);
        mockAccountUSD.setCreatedAt(LocalDateTime.now());
        mockAccountUSD.setUpdatedAt(LocalDateTime.now());

        // Preparar modelo del dólar
        mockDolarModel = new DolarModel(
                "Oficial",
                "ABC",
                "Dólar",
                new BigDecimal("46.50"),
                new BigDecimal("46.00"),
                LocalDateTime.now()
        );
    }

    // ============================================
    // TESTS PARA getAccountById
    // ============================================

    @Test
    @DisplayName("getAccountById: Debe retornar AccountResponse cuando cuenta existe (ARS)")
    void testGetAccountByIdARS() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccountARS));

        // When
        AccountResponse response = accountService.getAccountById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.accountId());
        assertEquals("001-ARS", response.accountNumber());
        assertEquals(Currency.ARS, response.currency());
        assertEquals(new BigDecimal("50000.00"), response.balance());
        assertEquals(BigDecimal.ZERO, response.balanceInPesos());
        assertTrue(response.active());

        verify(accountRepository, times(1)).findById(1L);
        verify(dolarApiClient, never()).getCotizacion();
    }

    @Test
    @DisplayName("getAccountById: Debe convertir a pesos cuando cuenta es USD")
    void testGetAccountByIdUSD() {
        // Given
        when(accountRepository.findById(2L)).thenReturn(Optional.of(mockAccountUSD));
        when(dolarApiClient.getCotizacion()).thenReturn(mockDolarModel);

        // When
        AccountResponse response = accountService.getAccountById(2L);

        // Then
        assertNotNull(response);
        assertEquals(2L, response.accountId());
        assertEquals("002-USD", response.accountNumber());
        assertEquals(Currency.USD, response.currency());
        assertEquals(new BigDecimal("1000.00"), response.balance());
        // 1000 * 46.00 = 46000
        // Usar compareTo para comparación de BigDecimal
        assertEquals(0, response.balanceInPesos().compareTo(new BigDecimal("46000")));
        assertTrue(response.active());

        verify(accountRepository, times(1)).findById(2L);
        verify(dolarApiClient, times(1)).getCotizacion();
    }

    @Test
    @DisplayName("getAccountById: Debe lanzar excepción cuando cuenta no existe")
    void testGetAccountByIdNotFound() {
        // Given
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> accountService.getAccountById(999L)
        );
        assertTrue(exception.getMessage().contains("Account not found"));
        verify(accountRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("getAccountById: Debe incluir datos del cliente en respuesta")
    void testGetAccountByIdClientData() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccountARS));

        // When
        AccountResponse response = accountService.getAccountById(1L);

        // Then
        assertEquals(1L, response.clientId());
        assertEquals("Juan", response.clientName());
        verify(accountRepository, times(1)).findById(1L);
    }

    // ============================================
    // TESTS PARA getAllAccounts
    // ============================================

    @Test
    @DisplayName("getAllAccounts: Debe retornar lista de todas las cuentas")
    void testGetAllAccounts() {
        // Given
        List<AccountEntity> accounts = Arrays.asList(mockAccountARS, mockAccountUSD);
        when(accountRepository.findAll()).thenReturn(accounts);
        when(dolarApiClient.getCotizacion()).thenReturn(mockDolarModel);

        // When
        List<AccountResponse> responses = accountService.getAllAccounts();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("001-ARS", responses.get(0).accountNumber());
        assertEquals("002-USD", responses.get(1).accountNumber());
        verify(accountRepository, times(1)).findAll();
        verify(dolarApiClient, times(1)).getCotizacion(); // Una vez para la cuenta USD
    }

    @Test
    @DisplayName("getAllAccounts: Debe retornar lista vacía cuando no hay cuentas")
    void testGetAllAccountsEmpty() {
        // Given
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<AccountResponse> responses = accountService.getAllAccounts();

        // Then
        assertNotNull(responses);
        assertEquals(0, responses.size());
        verify(accountRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllAccounts: Debe convertir correctamente todos los USD a pesos")
    void testGetAllAccountsWithMultipleUSD() {
        // Given
        List<AccountEntity> accounts = Arrays.asList(mockAccountARS, mockAccountUSD, mockAccountUSD);
        when(accountRepository.findAll()).thenReturn(accounts);
        when(dolarApiClient.getCotizacion()).thenReturn(mockDolarModel);

        // When
        List<AccountResponse> responses = accountService.getAllAccounts();

        // Then
        assertEquals(3, responses.size());
        // Primera cuenta (ARS): sin conversión
        assertEquals(BigDecimal.ZERO, responses.get(0).balanceInPesos());
        // Segunda y tercera (USD): con conversión
        // Usar compareTo para comparación de BigDecimal
        assertEquals(0, responses.get(1).balanceInPesos().compareTo(new BigDecimal("46000")));
        assertEquals(0, responses.get(2).balanceInPesos().compareTo(new BigDecimal("46000")));
    }

    // ============================================
    // TESTS PARA addAccount
    // ============================================

    @Test
    @DisplayName("addAccount: Debe crear nueva cuenta correctamente")
    void testAddAccountSuccess() {
        // Given
        AccountRequest request = new AccountRequest(
                1L,
                "003-ARS",
                "ARS",
                new BigDecimal("25000.00")
        );
        when(clientRepository.findById(1L)).thenReturn(Optional.of(mockClient));
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(mockAccountARS);

        // When
        AccountResponse response = accountService.addAccount(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.accountId());
        assertEquals("001-ARS", response.accountNumber());
        assertEquals(Currency.ARS, response.currency());
        assertTrue(response.active());
        verify(clientRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(any(AccountEntity.class));
    }

    @Test
    @DisplayName("addAccount: Debe crear cuenta USD correctamente")
    void testAddAccountUSD() {
        // Given
        AccountRequest request = new AccountRequest(
                1L,
                "004-USD",
                "USD",
                new BigDecimal("5000.00")
        );
        when(clientRepository.findById(1L)).thenReturn(Optional.of(mockClient));
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(mockAccountUSD);

        // When
        AccountResponse response = accountService.addAccount(request);

        // Then
        assertNotNull(response);
        assertEquals(Currency.USD, response.currency());
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("addAccount: Debe lanzar excepción cuando cliente no existe")
    void testAddAccountClientNotFound() {
        // Given
        AccountRequest request = new AccountRequest(
                999L,
                "005-ARS",
                "ARS",
                new BigDecimal("10000.00")
        );
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> accountService.addAccount(request)
        );
        assertTrue(exception.getMessage().contains("Client not found"));
        verify(clientRepository, times(1)).findById(999L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("addAccount: Debe establecer campos correctamente al crear")
    void testAddAccountFieldsSet() {
        // Given
        AccountRequest request = new AccountRequest(
                1L,
                "006-ARS",
                "ARS",
                new BigDecimal("15000.00")
        );
        when(clientRepository.findById(1L)).thenReturn(Optional.of(mockClient));

        AccountEntity capturedAccount = new AccountEntity();
        when(accountRepository.save(any(AccountEntity.class)))
                .thenAnswer(invocation -> {
                    AccountEntity account = invocation.getArgument(0);
                    capturedAccount.setAccountNumber(account.getAccountNumber());
                    capturedAccount.setCurrency(account.getCurrency());
                    capturedAccount.setBalance(account.getBalance());
                    capturedAccount.setActive(account.getActive());
                    capturedAccount.setClient(account.getClient());
                    capturedAccount.setAccountId(1L);
                    capturedAccount.setCreatedAt(LocalDateTime.now());
                    capturedAccount.setUpdatedAt(LocalDateTime.now());
                    return capturedAccount;
                });

        // When
        AccountResponse response = accountService.addAccount(request);

        // Then
        assertEquals("006-ARS", response.accountNumber());
        assertEquals(Currency.ARS, response.currency());
        assertEquals(new BigDecimal("15000.00"), response.balance());
        assertTrue(response.active());
    }

    // ============================================
    // TESTS PARA deleteById
    // ============================================

    @Test
    @DisplayName("deleteById: Debe eliminar cuenta por ID")
    void testDeleteById() {
        // Given
        Long accountId = 1L;

        // When
        accountService.deleteById(accountId);

        // Then
        verify(accountRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById: Debe ejecutar delete sin excepción")
    void testDeleteByIdNoException() {
        // Given
        Long accountId = 1L;
        doNothing().when(accountRepository).deleteById(accountId);

        // When & Then
        assertDoesNotThrow(() -> accountService.deleteById(accountId));
        verify(accountRepository, times(1)).deleteById(accountId);
    }

    // ============================================
    // TESTS PARA CASOS EDGE
    // ============================================

    @Test
    @DisplayName("getAccountById: Debe manejar balance negativo correctamente")
    void testGetAccountWithNegativeBalance() {
        // Given
        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);
        account.setAccountNumber("NEGATIVE");
        account.setCurrency(Currency.ARS);
        account.setBalance(new BigDecimal("-1000.00"));
        account.setActive(true);
        account.setClient(mockClient);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When
        AccountResponse response = accountService.getAccountById(1L);

        // Then
        assertEquals(new BigDecimal("-1000.00"), response.balance());
    }

    @Test
    @DisplayName("getAccountById: Debe manejar balance cero")
    void testGetAccountWithZeroBalance() {
        // Given
        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);
        account.setAccountNumber("ZERO");
        account.setCurrency(Currency.ARS);
        account.setBalance(BigDecimal.ZERO);
        account.setActive(true);
        account.setClient(mockClient);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When
        AccountResponse response = accountService.getAccountById(1L);

        // Then
        assertEquals(BigDecimal.ZERO, response.balance());
    }

    @Test
    @DisplayName("getAccountById: Debe manejar balance grande")
    void testGetAccountWithLargeBalance() {
        // Given
        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);
        account.setAccountNumber("LARGE");
        account.setCurrency(Currency.USD);
        account.setBalance(new BigDecimal("1000000.00"));
        account.setActive(true);
        account.setClient(mockClient);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(dolarApiClient.getCotizacion()).thenReturn(mockDolarModel);

        // When
        AccountResponse response = accountService.getAccountById(1L);

        // Then
        assertEquals(new BigDecimal("1000000.00"), response.balance());
        // 1000000 * 46.00 = 46000000
        // Usar compareTo para evitar problemas con precisión decimal
        assertEquals(0, response.balanceInPesos().compareTo(new BigDecimal("46000000")));
    }

    @Test
    @DisplayName("getAccountById: Debe retornar cuenta inactiva")
    void testGetInactiveAccount() {
        // Given
        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);
        account.setAccountNumber("INACTIVE");
        account.setCurrency(Currency.ARS);
        account.setBalance(new BigDecimal("1000.00"));
        account.setActive(false);
        account.setClient(mockClient);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When
        AccountResponse response = accountService.getAccountById(1L);

        // Then
        assertFalse(response.active());
    }
}

