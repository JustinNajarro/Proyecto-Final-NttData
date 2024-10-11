package com.nttdata.AccountMs.controllers;

import com.nttdata.AccountMs.CuentaDelegateImp;
import com.nttdata.AccountMs.business.CuentaService;
import com.nttdata.AccountMs.model.CuentaRequest;
import com.nttdata.AccountMs.model.CuentaResponse;
import com.nttdata.AccountMs.model.InlineObject;
import com.nttdata.AccountMs.model.SaldoTipoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CuentaDelegateImpTest {

    @Mock
    private CuentaService cuentaService;

    @InjectMocks
    private CuentaDelegateImp cuentaDelegateImp;

    private CuentaRequest cuentaRequest;
    private CuentaResponse cuentaResponse1;
    private CuentaResponse cuentaResponse2;
    private InlineObject inlineObject;
    private SaldoTipoResponse saldoTipoResponse;

    @BeforeEach
    public void setUp() {

        cuentaRequest = new CuentaRequest();
        cuentaRequest.setSaldo(1000.0);
        cuentaRequest.setTipoCuenta(CuentaRequest.TipoCuentaEnum.AHORROS);
        cuentaRequest.setClienteId(1);

        cuentaResponse1 = new CuentaResponse();
        cuentaResponse1.setNumeroCuenta("1234567890");
        cuentaResponse1.setSaldo(1000.0);
        cuentaResponse1.setTipoCuenta(CuentaResponse.TipoCuentaEnum.AHORROS);

        cuentaResponse2 = new CuentaResponse();
        cuentaResponse2.setNumeroCuenta("0987654321");
        cuentaResponse2.setSaldo(2000.0);
        cuentaResponse2.setTipoCuenta(CuentaResponse.TipoCuentaEnum.CORRIENTE);

        inlineObject = new InlineObject();
        inlineObject.setNuevoSaldo(1500.0);

        saldoTipoResponse = new SaldoTipoResponse();
        saldoTipoResponse.setSaldo(1000.0);
        saldoTipoResponse.setTipoCuenta(SaldoTipoResponse.TipoCuentaEnum.AHORROS);
    }

    @Test
    public void testListAllAccounts_Success() {
        List<CuentaResponse> cuentas = Arrays.asList(cuentaResponse1, cuentaResponse2);

        when(cuentaService.listAllAccounts()).thenReturn(cuentas);

        ResponseEntity<List<CuentaResponse>> response = cuentaDelegateImp.listAllAccounts();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertEquals("1234567890", response.getBody().get(0).getNumeroCuenta());

        verify(cuentaService, times(1)).listAllAccounts();
    }

    @Test
    public void testCreateAccount_Success() {
        when(cuentaService.createAccount(cuentaRequest)).thenReturn(cuentaResponse1);

        ResponseEntity<CuentaResponse> response = cuentaDelegateImp.createAccount(cuentaRequest);

        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("1234567890", response.getBody().getNumeroCuenta());

        verify(cuentaService, times(1)).createAccount(cuentaRequest);
    }

    @Test
    public void testGetAccountById_Success() {
        Integer id = 1;

        when(cuentaService.getAccountById(id)).thenReturn(cuentaResponse1);

        ResponseEntity<CuentaResponse> response = cuentaDelegateImp.getAccountById(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("1234567890", response.getBody().getNumeroCuenta());

        verify(cuentaService, times(1)).getAccountById(id);
    }

    @Test
    public void testDeleteAccountById_Success() {
        Integer id = 1;

        doNothing().when(cuentaService).deleteAccountById(id);

        ResponseEntity<Void> response = cuentaDelegateImp.deleteAccountById(id);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());

        verify(cuentaService, times(1)).deleteAccountById(id);
    }

    @Test
    public void testUpdateAccountBalance_Success() {
        String numeroCuenta = "1234567890";

        doNothing().when(cuentaService).updateAccountBalance(numeroCuenta, inlineObject);

        ResponseEntity<Void> response = cuentaDelegateImp.updateAccountBalance(numeroCuenta, inlineObject);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());

        verify(cuentaService, times(1)).updateAccountBalance(numeroCuenta, inlineObject);
    }

    @Test
    public void testGetAccountBalanceAndType_Success() {
        String numeroCuenta = "1234567890";

        when(cuentaService.getAccountBalanceAndType(numeroCuenta)).thenReturn(saldoTipoResponse);

        ResponseEntity<SaldoTipoResponse> response = cuentaDelegateImp.getAccountBalanceAndType(numeroCuenta);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1000.0, response.getBody().getSaldo());
        assertEquals(SaldoTipoResponse.TipoCuentaEnum.AHORROS, response.getBody().getTipoCuenta());

        verify(cuentaService, times(1)).getAccountBalanceAndType(numeroCuenta);
    }
}

