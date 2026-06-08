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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests E2E adicionales para validar casos edge y errores
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Account API E2E Validation Tests")
class AccountE2EValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AccountRepository accountRepository;

    private ClientEntity client1;
    private ClientEntity client2;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        clientRepository.deleteAll();

        client1 = new ClientEntity();
        client1.setFirstName("Carlos");
        client1.setLastNameOrCompanyName("López");
        client1.setDocumentNumber("11111111");
        client1.setActive(true);
        client1.setUserType("INDIVIDUAL");
        client1.setRegistrationDate(LocalDateTime.now());
        client1.setModificationDate(LocalDateTime.now());
        client1 = clientRepository.save(client1);

        client2 = new ClientEntity();
        client2.setFirstName("María");
        client2.setLastNameOrCompanyName("García");
        client2.setDocumentNumber("22222222");
        client2.setActive(true);
        client2.setUserType("INDIVIDUAL");
        client2.setRegistrationDate(LocalDateTime.now());
        client2.setModificationDate(LocalDateTime.now());
        client2 = clientRepository.save(client2);
    }

    @Test
    @DisplayName("E2E: Validar saldos en diferentes monedas")
    void testAccountBalanceInDifferentCurrencies() {
        AccountRequest arsRequest = new AccountRequest(
                client1.getId(),
                "ACC-BALANCE-ARS",
                "ARS",
                new BigDecimal("5000.00")
        );

        ResponseEntity<AccountResponse> arsResponse = restTemplate.postForEntity(
                "/api/v1/accounts",
                arsRequest,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, arsResponse.getStatusCode());
        AccountResponse arsAccount = arsResponse.getBody();
        assertNotNull(arsAccount);
        assertEquals(new BigDecimal("5000.00"), arsAccount.balance());

        AccountRequest usdRequest = new AccountRequest(
                client1.getId(),
                "ACC-BALANCE-USD",
                "USD",
                new BigDecimal("500.00")
        );

        ResponseEntity<AccountResponse> usdResponse = restTemplate.postForEntity(
                "/api/v1/accounts",
                usdRequest,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, usdResponse.getStatusCode());
        AccountResponse usdAccount = usdResponse.getBody();
        assertNotNull(usdAccount);
        assertEquals(new BigDecimal("500.00"), usdAccount.balance());

        ResponseEntity<AccountResponse> getUsdResponse = restTemplate.getForEntity(
                "/api/v1/accounts/" + usdAccount.accountId(),
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, getUsdResponse.getStatusCode());
        AccountResponse retrievedUsdAccount = getUsdResponse.getBody();
        assertNotNull(retrievedUsdAccount);
        assertTrue(retrievedUsdAccount.balanceInPesos().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("E2E: Verificar que cada cuenta está asociada al cliente correcto")
    void testAccountBelongsToCorrectClient() {
        AccountRequest client1Request = new AccountRequest(
                client1.getId(),
                "CLIENT1-ACC",
                "ARS",
                new BigDecimal("1000.00")
        );

        ResponseEntity<AccountResponse> client1Response = restTemplate.postForEntity(
                "/api/v1/accounts",
                client1Request,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, client1Response.getStatusCode());
        assertNotNull(client1Response.getBody());
        Long account1Id = client1Response.getBody().accountId();

        AccountRequest client2Request = new AccountRequest(
                client2.getId(),
                "CLIENT2-ACC",
                "ARS",
                new BigDecimal("2000.00")
        );

        ResponseEntity<AccountResponse> client2Response = restTemplate.postForEntity(
                "/api/v1/accounts",
                client2Request,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, client2Response.getStatusCode());
        assertNotNull(client2Response.getBody());
        Long account2Id = client2Response.getBody().accountId();

        ResponseEntity<AccountResponse> getAccount1 = restTemplate.getForEntity(
                "/api/v1/accounts/" + account1Id,
                AccountResponse.class
        );

        ResponseEntity<AccountResponse> getAccount2 = restTemplate.getForEntity(
                "/api/v1/accounts/" + account2Id,
                AccountResponse.class
        );

        assertNotNull(getAccount1.getBody());
        assertEquals(client1.getId(), getAccount1.getBody().clientId());
        assertEquals("Carlos", getAccount1.getBody().clientName());

        assertNotNull(getAccount2.getBody());
        assertEquals(client2.getId(), getAccount2.getBody().clientId());
        assertEquals("María", getAccount2.getBody().clientName());
    }

    @Test
    @DisplayName("E2E: Número de cuenta debe ser único")
    void testAccountNumberUniqueness() {
        String accountNumber = "UNIQUE-ACC-123";

        AccountRequest request1 = new AccountRequest(
                client1.getId(),
                accountNumber,
                "ARS",
                new BigDecimal("1000.00")
        );

        ResponseEntity<AccountResponse> response1 = restTemplate.postForEntity(
                "/api/v1/accounts",
                request1,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, response1.getStatusCode());

        AccountRequest request2 = new AccountRequest(
                client2.getId(),
                accountNumber,
                "ARS",
                new BigDecimal("2000.00")
        );

        ResponseEntity<AccountResponse> response2 = restTemplate.postForEntity(
                "/api/v1/accounts",
                request2,
                AccountResponse.class
        );

        assertNotEquals(HttpStatus.OK, response2.getStatusCode());
    }

    @Test
    @DisplayName("E2E: Crear y listar múltiples cuentas para múltiples clientes")
    void testMultipleAccountsMultipleClients() {
        AccountRequest c1Acc1 = new AccountRequest(client1.getId(), "C1-ACC1", "ARS", new BigDecimal("1000.00"));
        AccountRequest c1Acc2 = new AccountRequest(client1.getId(), "C1-ACC2", "USD", new BigDecimal("100.00"));

        Long c1a1Id = restTemplate.postForEntity("/api/v1/accounts", c1Acc1, AccountResponse.class)
                .getBody().accountId();
        Long c1a2Id = restTemplate.postForEntity("/api/v1/accounts", c1Acc2, AccountResponse.class)
                .getBody().accountId();

        AccountRequest c2Acc1 = new AccountRequest(client2.getId(), "C2-ACC1", "ARS", new BigDecimal("5000.00"));
        AccountRequest c2Acc2 = new AccountRequest(client2.getId(), "C2-ACC2", "USD", new BigDecimal("500.00"));

        Long c2a1Id = Objects.requireNonNull(restTemplate.postForEntity("/api/v1/accounts", c2Acc1, AccountResponse.class)
                .getBody()).accountId();
        Long c2a2Id = Objects.requireNonNull(restTemplate.postForEntity("/api/v1/accounts", c2Acc2, AccountResponse.class)
                .getBody()).accountId();

        ResponseEntity<AccountResponse[]> allAccounts = restTemplate.getForEntity(
                "/api/v1/accounts",
                AccountResponse[].class
        );

        assertEquals(HttpStatus.OK, allAccounts.getStatusCode());
        AccountResponse[] accounts = allAccounts.getBody();
        assertNotNull(accounts);
        assertEquals(4, accounts.length);

        boolean[] found = new boolean[4];
        for (AccountResponse account : accounts) {
            if (account.accountId().equals(c1a1Id)) found[0] = true;
            if (account.accountId().equals(c1a2Id)) found[1] = true;
            if (account.accountId().equals(c2a1Id)) found[2] = true;
            if (account.accountId().equals(c2a2Id)) found[3] = true;
        }

        for (boolean f : found) {
            assertTrue(f, "Todas las cuentas deben estar en la lista");
        }
    }

    @Test
    @DisplayName("E2E: Eliminar cuenta y verificar que no existe más")
    void testDeleteAccountAndVerifyDeletion() {
        AccountRequest request = new AccountRequest(
                client1.getId(),
                "TO-DELETE",
                "ARS",
                new BigDecimal("1000.00")
        );

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/accounts",
                request,
                AccountResponse.class
        );

        assertNotNull(createResponse.getBody());
        Long accountId = createResponse.getBody().accountId();

        ResponseEntity<AccountResponse> existsResponse = restTemplate.getForEntity(
                "/api/v1/accounts/" + accountId,
                AccountResponse.class
        );
        assertEquals(HttpStatus.OK, existsResponse.getStatusCode());

        restTemplate.delete("/api/v1/accounts/" + accountId);

        ResponseEntity<AccountResponse> deletedResponse = restTemplate.getForEntity(
                "/api/v1/accounts/" + accountId,
                AccountResponse.class
        );
        assertEquals(HttpStatus.NOT_FOUND, deletedResponse.getStatusCode());
    }

    @Test
    @DisplayName("E2E: Crear cuentas con diferentes saldos")
    void testCreateAccountsWithVariousBalances() {
        BigDecimal[] balances = {
                BigDecimal.ONE,
                new BigDecimal("100.50"),
                new BigDecimal("999999.99"),
                new BigDecimal("0.01")
        };

        for (int i = 0; i < balances.length; i++) {
            AccountRequest request = new AccountRequest(
                    client1.getId(),
                    "BALANCE-TEST-" + i,
                    "ARS",
                    balances[i]
            );

            ResponseEntity<AccountResponse> response = restTemplate.postForEntity(
                    "/api/v1/accounts",
                    request,
                    AccountResponse.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(balances[i], response.getBody().balance());
        }
    }

    @Test
    @DisplayName("E2E: Validar que nuevas cuentas están activas por defecto")
    void testNewAccountsAreActiveByDefault() {
        AccountRequest request = new AccountRequest(
                client1.getId(),
                "ACTIVE-TEST",
                "ARS",
                new BigDecimal("1000.00")
        );

        ResponseEntity<AccountResponse> response = restTemplate.postForEntity(
                "/api/v1/accounts",
                request,
                AccountResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().active());
    }

    @Test
    @DisplayName("E2E: Validar estructura de timestamps en nuevas cuentas")
    void testAccountTimestampsOnCreation() {
        LocalDateTime beforeCreation = LocalDateTime.now();

        AccountRequest request = new AccountRequest(
                client1.getId(),
                "TIMESTAMP-TEST",
                "ARS",
                new BigDecimal("1000.00")
        );

        ResponseEntity<AccountResponse> response = restTemplate.postForEntity(
                "/api/v1/accounts",
                request,
                AccountResponse.class
        );

        LocalDateTime afterCreation = LocalDateTime.now();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse account = response.getBody();

        assertNotNull(account);
        assertNotNull(account.createdAt());
        assertNotNull(account.updatedAt());

        assertTrue(account.createdAt().isAfter(beforeCreation) || account.createdAt().isEqual(beforeCreation));
        assertTrue(account.createdAt().isBefore(afterCreation) || account.createdAt().isEqual(afterCreation));
    }
}

