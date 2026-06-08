package com.fintech.api.controller;

import com.fintech.api.dto.AccountRequest;
import com.fintech.api.dto.AccountResponse;
import com.fintech.api.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "API para gestionar cuentas")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cuenta por ID", description = "Obtiene los detalles de una cuenta específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    public ResponseEntity<AccountResponse> getAccountById(
            @Parameter(description = "ID de la cuenta") @PathVariable Long id) {
        var response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    @Operation(summary = "Obtener todas las cuentas", description = "Obtiene la lista de todas las cuentas registradas")
    @ApiResponse(responseCode = "200", description = "Lista de cuentas")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        var response = accountService.getAllAccounts();
        return ResponseEntity.ok(response);
    }

    @PostMapping()
    @Operation(summary = "Crear nueva cuenta", description = "Crea una nueva cuenta con los datos proporcionados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountRequest accountRequest) {
        var response = accountService.addAccount(accountRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar cuenta", description = "Edita una cuenta existente con los datos proporcionados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta editada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    public ResponseEntity<AccountResponse> updateAccount(@Parameter(description = "ID de la cuenta a editar") @PathVariable Long id,
                                                         @RequestBody AccountRequest accountRequest) {
        var response = accountService.updateAccount(id, accountRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cuenta", description = "Elimina una cuenta por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cuenta eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    public ResponseEntity<Void> deleteAccountById(
            @Parameter(description = "ID de la cuenta a eliminar") @PathVariable Long id){
        accountService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
