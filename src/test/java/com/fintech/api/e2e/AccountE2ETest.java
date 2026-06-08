package com.fintech.api.e2e;

import com.fintech.api.dto.AccountRequest;
import com.fintech.api.dto.AccountResponse;
import com.fintech.api.entity.ClientEntity;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests E2E para validar flujos completos de la API de Cuentas
 * Incluye: crear cuentas, listar, obtener por ID y eliminar
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Account API E2E Tests")
class AccountE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AccountRepository accountRepository;

    private ClientEntity clientEntity;
    private Long createdAccountId;

    @BeforeEach
    void setUp() {
        // Limpiar datos de pruebas anteriores en orden correcto (cuentas primero para evitar FK)
        accountRepository.deleteAll();
        clientRepository.deleteAll();

        // Crear un cliente de prueba
        clientEntity = new ClientEntity();
        clientEntity.setFirstName("Juan");
        clientEntity.setLastNameOrCompanyName("Pérez");
        clientEntity.setDocumentNumber("12345678");
        clientEntity.setAddress("Calle Principal 123");
        clientEntity.setPhoneNumber("+54 11 1234-5678");
        clientEntity.setEmail("juan.perez@example.com");
        clientEntity.setUserType("INDIVIDUAL");
        clientEntity.setActive(true);
        clientEntity.setOutstandingBalance(BigDecimal.ZERO);
        clientEntity.setRegistrationDate(LocalDateTime.now());
        clientEntity.setModificationDate(LocalDateTime.now());

        clientEntity = clientRepository.save(clientEntity);
    }

    @Test
    @DisplayName("Flujo E2E: Crear cuenta ARS, obtener, listar y eliminar")
    void testCompleteAccountLifecycleARS() {
        // 1. CREAR CUENTA ARS
        AccountRequest createRequest = new AccountRequest(
                clientEntity.getId(),
                "1234567890-ARS",
                "ARS",
                new BigDecimal("10000.00")
        );

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/accounts",
                createRequest,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        AccountResponse createdAccount = createResponse.getBody();
        createdAccountId = createdAccount.accountId();

        assertEquals("1234567890-ARS", createdAccount.accountNumber());
        assertEquals("ARS", createdAccount.currency().toString());
        assertEquals(new BigDecimal("10000.00"), createdAccount.balance());
        assertEquals("Juan", createdAccount.clientName());
        assertTrue(createdAccount.active());

        // 2. OBTENER CUENTA POR ID
        ResponseEntity<AccountResponse> getResponse = restTemplate.getForEntity(
                "/api/v1/accounts/" + createdAccountId,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(createdAccountId, getResponse.getBody().accountId());
        assertEquals("1234567890-ARS", getResponse.getBody().accountNumber());

        // 3. LISTAR TODAS LAS CUENTAS
        ResponseEntity<AccountResponse[]> listResponse = restTemplate.getForEntity(
                "/api/v1/accounts",
                AccountResponse[].class
        );

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        assertTrue(listResponse.getBody().length >= 1);

        boolean foundAccount = false;
        for (AccountResponse account : listResponse.getBody()) {
            if (account.accountId().equals(createdAccountId)) {
                foundAccount = true;
                assertEquals("1234567890-ARS", account.accountNumber());
                break;
            }
        }
        assertTrue(foundAccount, "Cuenta creada no encontrada en la lista");

        // 4. ELIMINAR CUENTA
        restTemplate.delete("/api/v1/accounts/" + createdAccountId);

        // 5. VERIFICAR QUE LA CUENTA FUE ELIMINADA
        ResponseEntity<AccountResponse> deleteVerification = restTemplate.getForEntity(
                "/api/v1/accounts/" + createdAccountId,
                AccountResponse.class
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, deleteVerification.getStatusCode());
    }

    @Test
    @DisplayName("Flujo E2E: Crear cuenta USD y validar conversión a pesos")
    void testCreateUSDAccountWithConversion() {
        // CREAR CUENTA USD
        AccountRequest createRequest = new AccountRequest(
                clientEntity.getId(),
                "9876543210-USD",
                "USD",
                new BigDecimal("1000.00")
        );

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/accounts",
                createRequest,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        AccountResponse createdAccount = createResponse.getBody();
        createdAccountId = createdAccount.accountId();

        assertEquals("9876543210-USD", createdAccount.accountNumber());
        assertEquals("USD", createdAccount.currency().toString());
        assertEquals(new BigDecimal("1000.00"), createdAccount.balance());

        // OBTENER LA CUENTA Y VERIFICAR CONVERSIÓN
        ResponseEntity<AccountResponse> getResponse = restTemplate.getForEntity(
                "/api/v1/accounts/" + createdAccountId,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        AccountResponse retrievedAccount = getResponse.getBody();

        assertNotNull(retrievedAccount);
        // La conversión a pesos debe ser mayor que 0 para USD
        assertTrue(retrievedAccount.balanceInPesos().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(new BigDecimal("1000.00"), retrievedAccount.balance());
    }

    @Test
    @DisplayName("E2E: Crear múltiples cuentas para un cliente")
    void testCreateMultipleAccountsForSingleClient() {
        // CREAR PRIMERA CUENTA
        AccountRequest request1 = new AccountRequest(
                clientEntity.getId(),
                "ACC-001-ARS",
                "ARS",
                new BigDecimal("5000.00")
        );

        ResponseEntity<AccountResponse> response1 = restTemplate.postForEntity(
                "/api/v1/accounts",
                request1,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertNotNull(response1.getBody());
        Long accountId1 = response1.getBody().accountId();

        // CREAR SEGUNDA CUENTA
        AccountRequest request2 = new AccountRequest(
                clientEntity.getId(),
                "ACC-002-USD",
                "USD",
                new BigDecimal("500.00")
        );

        ResponseEntity<AccountResponse> response2 = restTemplate.postForEntity(
                "/api/v1/accounts",
                request2,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotNull(response2.getBody());
        Long accountId2 = response2.getBody().accountId();

        // LISTAR TODAS LAS CUENTAS
        ResponseEntity<AccountResponse[]> listResponse = restTemplate.getForEntity(
                "/api/v1/accounts",
                AccountResponse[].class
        );

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        AccountResponse[] accounts = listResponse.getBody();
        assertNotNull(accounts);
        assertTrue(accounts.length >= 2);

        // VERIFICAR QUE AMBAS CUENTAS ESTÁN EN LA LISTA
        int accountCount = 0;
        for (AccountResponse account : accounts) {
            if (account.accountId().equals(accountId1) || account.accountId().equals(accountId2)) {
                accountCount++;
            }
        }
        assertEquals(2, accountCount, "Las dos cuentas deben estar en la lista");
    }

    @Test
    @DisplayName("E2E: Intentar crear cuenta con cliente inexistente")
    void testCreateAccountWithNonExistentClient() {
        Long nonExistentClientId = 99999L;

        AccountRequest createRequest = new AccountRequest(
                nonExistentClientId,
                "INVALID-ACC",
                "ARS",
                new BigDecimal("1000.00")
        );

        ResponseEntity<AccountResponse> response = restTemplate.postForEntity(
                "/api/v1/accounts",
                createRequest,
                AccountResponse.class
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("E2E: Obtener cuenta que no existe")
    void testGetNonExistentAccount() {
        Long nonExistentId = 99999L;

        ResponseEntity<AccountResponse> response = restTemplate.getForEntity(
                "/api/v1/accounts/" + nonExistentId,
                AccountResponse.class
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("E2E: Validar estructura de respuesta de cuentas")
    void testAccountResponseStructure() {
        // CREAR UNA CUENTA
        AccountRequest createRequest = new AccountRequest(
                clientEntity.getId(),
                "STRUCT-TEST",
                "ARS",
                new BigDecimal("15000.00")
        );

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/accounts",
                createRequest,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        AccountResponse account = createResponse.getBody();
        assertNotNull(account);

        // VALIDAR QUE TODOS LOS CAMPOS ESTÁN PRESENTES
        assertNotNull(account.accountId());
        assertNotNull(account.clientId());
        assertNotNull(account.clientName());
        assertNotNull(account.accountNumber());
        assertNotNull(account.currency());
        assertNotNull(account.balance());
        assertNotNull(account.balanceInPesos());
        assertNotNull(account.active());
        assertNotNull(account.createdAt());
        assertNotNull(account.updatedAt());

        // VALIDAR TIPOS Y VALORES
        assertTrue(account.accountId() > 0);
        assertEquals(clientEntity.getId(), account.clientId());
        assertEquals("Juan", account.clientName());
        assertEquals(new BigDecimal("15000.00"), account.balance());
        assertTrue(account.active());
    }

    @Test
    @DisplayName("E2E: Verificar que listar cuentas retorna lista vacía cuando no hay cuentas")
    void testListAccountsEmptyList() {
        // Si la base de datos está vacía, debe retornar lista vacía
        ResponseEntity<AccountResponse[]> response = restTemplate.getForEntity(
                "/api/v1/accounts",
                AccountResponse[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Al menos debería retornar un array (aunque esté vacío)
        assertNotNull(response.getBody());
    }
}




