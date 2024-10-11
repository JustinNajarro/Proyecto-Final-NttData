package com.nttdata.AccountMs.service.impl;

import com.nttdata.AccountMs.business.CuentaMapper;
import com.nttdata.AccountMs.business.Impl.CuentaServiceImp;
import com.nttdata.AccountMs.clients.CustomerFeignClient;
import com.nttdata.AccountMs.exception.CustomExceptions;
import com.nttdata.AccountMs.model.CuentaRequest;
import com.nttdata.AccountMs.model.CuentaResponse;
import com.nttdata.AccountMs.model.InlineObject;
import com.nttdata.AccountMs.model.SaldoTipoResponse;
import com.nttdata.AccountMs.model.entity.Cuenta;
import com.nttdata.AccountMs.model.entity.TipoCuentaEnum;
import com.nttdata.AccountMs.repository.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CuentaServiceImpTest {
    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private CuentaMapper cuentaMapper;

    @Mock
    private CustomerFeignClient customerFeignClient;

    @InjectMocks
    private CuentaServiceImp cuentaServiceImp;

    private Cuenta cuenta;
    private CuentaRequest cuentaRequest;
    private CuentaResponse cuentaResponse;

    @BeforeEach
    public void setUp() {

        cuenta = new Cuenta();
        cuenta.setNumeroCuenta("1234567890");
        cuenta.setSaldo(1000.0);
        cuenta.setTipoCuenta(TipoCuentaEnum.AHORROS);
        cuenta.setClienteId(1);

        cuentaRequest = new CuentaRequest();
        cuentaRequest.setSaldo(1000.0);
        cuentaRequest.setTipoCuenta(CuentaRequest.TipoCuentaEnum.AHORROS);
        cuentaRequest.setClienteId(1);

        cuentaResponse = new CuentaResponse();
        cuentaResponse.setNumeroCuenta("1234567890");
        cuentaResponse.setSaldo(1000.0);
        cuentaResponse.setTipoCuenta(CuentaResponse.TipoCuentaEnum.AHORROS);
    }

    @Test
    @DisplayName("List all Accounts success")
    public void testListAllAccounts_Success() {
        List<Cuenta> cuentas = List.of(cuenta);
        List<CuentaResponse> cuentaResponses = List.of(cuentaResponse);

        when(cuentaRepository.findAll()).thenReturn(cuentas);
        when(cuentaMapper.getCuentaResponseOfCuenta(cuenta)).thenReturn(cuentaResponse);

        List<CuentaResponse> result = cuentaServiceImp.listAllAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1234567890", result.get(0).getNumeroCuenta());

        verify(cuentaRepository, times(1)).findAll();
        verify(cuentaMapper, times(1)).getCuentaResponseOfCuenta(cuenta);
    }

    @Test
    @DisplayName("List all accounts - No accounts found")
    public void testListAllAccounts_NoAccountsFound() {
        when(cuentaRepository.findAll()).thenReturn(List.of());

        CustomExceptions.ResourceNotFoundException exception = assertThrows(
                CustomExceptions.ResourceNotFoundException.class,
                () -> cuentaServiceImp.listAllAccounts()
        );

        assertEquals("No se encontraron cuentas", exception.getMessage());

        verify(cuentaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Create account success")
    public void testCreateAccount_Success() {
        when(customerFeignClient.checkIfCustomerExists(cuentaRequest.getClienteId())).thenReturn(true);
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuenta);
        when(cuentaMapper.getCuentaResponseOfCuenta(cuenta)).thenReturn(cuentaResponse);
        when(cuentaMapper.mapToEntityTipoCuenta(cuentaRequest.getTipoCuenta())).thenReturn(TipoCuentaEnum.AHORROS);

        CuentaResponse result = cuentaServiceImp.createAccount(cuentaRequest);

        assertNotNull(result);
        assertEquals("1234567890", result.getNumeroCuenta());
        assertEquals(1000.0, result.getSaldo());

        verify(customerFeignClient, times(1)).checkIfCustomerExists(cuentaRequest.getClienteId());
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
        verify(cuentaMapper, times(1)).getCuentaResponseOfCuenta(cuenta);
    }

    @Test
    @DisplayName("Create account - Customer not exists")
    public void testCreateAccount_CustomerNotExists() {
        when(customerFeignClient.checkIfCustomerExists(cuentaRequest.getClienteId())).thenReturn(false);

        CustomExceptions.BadRequestException exception = assertThrows(
                CustomExceptions.BadRequestException.class,
                () -> cuentaServiceImp.createAccount(cuentaRequest)
        );

        assertEquals("El cliente no existe.", exception.getMessage());

        verify(customerFeignClient, times(1)).checkIfCustomerExists(cuentaRequest.getClienteId());
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @Test
    @DisplayName("Create account - Invalid initial balance")
    public void testCreateAccount_InvalidInitialBalance() {
        when(customerFeignClient.checkIfCustomerExists(cuentaRequest.getClienteId())).thenReturn(true);
        cuentaRequest.setSaldo(0.0);

        CustomExceptions.BadRequestException exception = assertThrows(
                CustomExceptions.BadRequestException.class,
                () -> cuentaServiceImp.createAccount(cuentaRequest)
        );

        assertEquals("El saldo inicial debe ser mayor a 0.", exception.getMessage());

        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @Test
    @DisplayName("Get account by id success")
    public void testGetAccountById_Success() {
        Integer id = 1;

        when(cuentaRepository.findById(id)).thenReturn(Optional.of(cuenta));
        when(cuentaMapper.getCuentaResponseOfCuenta(cuenta)).thenReturn(cuentaResponse);

        CuentaResponse result = cuentaServiceImp.getAccountById(id);

        assertNotNull(result);
        assertEquals("1234567890", result.getNumeroCuenta());

        verify(cuentaRepository, times(1)).findById(id);
        verify(cuentaMapper, times(1)).getCuentaResponseOfCuenta(cuenta);
    }

    @Test
    @DisplayName("Get account by id - Not found")
    public void testGetAccountById_NotFound() {
        Integer id = 1;

        when(cuentaRepository.findById(id)).thenReturn(Optional.empty());

        CustomExceptions.ResourceNotFoundException exception = assertThrows(
                CustomExceptions.ResourceNotFoundException.class,
                () -> cuentaServiceImp.getAccountById(id)
        );

        assertEquals("Cuenta no existente con ID: 1", exception.getMessage());

        verify(cuentaRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Delete account by id success")
    public void testDeleteAccountById_Success() {
        Integer id = 1;

        when(cuentaRepository.findById(id)).thenReturn(Optional.of(cuenta));

        cuentaServiceImp.deleteAccountById(id);

        verify(cuentaRepository, times(1)).findById(id);
        verify(cuentaRepository, times(1)).delete(cuenta);
    }

    @Test
    @DisplayName("Delete account by id - Not found")
    public void testDeleteAccountById_NotFound() {
        Integer id = 1;

        when(cuentaRepository.findById(id)).thenReturn(Optional.empty());

        CustomExceptions.ResourceNotFoundException exception = assertThrows(
                CustomExceptions.ResourceNotFoundException.class,
                () -> cuentaServiceImp.deleteAccountById(id)
        );

        assertEquals("Cuenta no existente con ID: 1", exception.getMessage());

        verify(cuentaRepository, times(1)).findById(id);
        verify(cuentaRepository, never()).delete(any(Cuenta.class));
    }

    @Test
    @DisplayName("Update account balance success")
    public void testUpdateAccountBalance_Success() {
        String numeroCuenta = "1234567890";
        InlineObject inlineObject = new InlineObject();
        inlineObject.setNuevoSaldo(1500.0);

        when(cuentaRepository.findByNumeroCuenta(numeroCuenta)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuenta);

        cuentaServiceImp.updateAccountBalance(numeroCuenta, inlineObject);

        assertEquals(1500.0, cuenta.getSaldo());

        verify(cuentaRepository, times(1)).findByNumeroCuenta(numeroCuenta);
        verify(cuentaRepository, times(1)).save(cuenta);
    }

    @Test
    @DisplayName("Update account balance - Not found")
    public void testUpdateAccountBalance_AccountNotFound() {
        String numeroCuenta = "1234567890";
        InlineObject inlineObject = new InlineObject();
        inlineObject.setNuevoSaldo(1500.0);

        when(cuentaRepository.findByNumeroCuenta(numeroCuenta)).thenReturn(Optional.empty());

        CustomExceptions.ResourceNotFoundException exception = assertThrows(
                CustomExceptions.ResourceNotFoundException.class,
                () -> cuentaServiceImp.updateAccountBalance(numeroCuenta, inlineObject)
        );

        assertEquals("Cuenta no encontrada", exception.getMessage());

        verify(cuentaRepository, times(1)).findByNumeroCuenta(numeroCuenta);
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @Test
    @DisplayName("Get account balance ant type success")
    public void testGetAccountBalanceAndType_Success() {
        String numeroCuenta = "1234567890";
        SaldoTipoResponse saldoTipoResponse = new SaldoTipoResponse();
        saldoTipoResponse.setSaldo(1000.0);
        saldoTipoResponse.setTipoCuenta(SaldoTipoResponse.TipoCuentaEnum.AHORROS);

        when(cuentaRepository.findByNumeroCuenta(numeroCuenta)).thenReturn(Optional.of(cuenta));
        when(cuentaMapper.getSaldoOfCuenta(cuenta)).thenReturn(saldoTipoResponse);

        SaldoTipoResponse result = cuentaServiceImp.getAccountBalanceAndType(numeroCuenta);
        
        assertNotNull(result);
        assertEquals(1000.0, result.getSaldo());
        assertEquals(SaldoTipoResponse.TipoCuentaEnum.AHORROS, result.getTipoCuenta());

        verify(cuentaRepository, times(1)).findByNumeroCuenta(numeroCuenta);
        verify(cuentaMapper, times(1)).getSaldoOfCuenta(cuenta);
    }

    @Test
    @DisplayName("Get account balance ant type - Not found")
    public void testGetAccountBalanceAndType_NotFound() {
        String numeroCuenta = "1234567890";

        when(cuentaRepository.findByNumeroCuenta(numeroCuenta)).thenReturn(Optional.empty());

        CustomExceptions.ResourceNotFoundException exception = assertThrows(
                CustomExceptions.ResourceNotFoundException.class,
                () -> cuentaServiceImp.getAccountBalanceAndType(numeroCuenta)
        );

        assertEquals("Cuenta no encontrada", exception.getMessage());

        verify(cuentaRepository, times(1)).findByNumeroCuenta(numeroCuenta);
        verify(cuentaMapper, never()).getSaldoOfCuenta(any(Cuenta.class));
    }

}
