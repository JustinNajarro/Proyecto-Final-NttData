package com.nttdata.TransaccionMs.service.impl;

import com.nttdata.TransaccionMs.business.Impl.TransaccionServiceImp;
import com.nttdata.TransaccionMs.business.TransaccionMapper;
import com.nttdata.TransaccionMs.clients.CuentaFeignClient;
import com.nttdata.TransaccionMs.dto.SaldoResponse;
import com.nttdata.TransaccionMs.dto.SaldoUpdateRequest;
import com.nttdata.TransaccionMs.exception.CustomExceptions;
import com.nttdata.TransaccionMs.model.TransactionRequest;
import com.nttdata.TransaccionMs.model.TransactionResponse;
import com.nttdata.TransaccionMs.model.entity.Transaccion;
import com.nttdata.TransaccionMs.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransaccionServiceImpTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private CuentaFeignClient cuentaFeignClient;

    @Mock
    private TransaccionMapper transaccionMapper;

    @InjectMocks
    private TransaccionServiceImp transaccionServiceImp;

    private TransactionRequest transactionRequest;
    private Transaccion transaccion;
    private TransactionResponse transactionResponse;
    private SaldoResponse saldoResponse;

    @BeforeEach
    public void setUp() {

        transactionRequest = new TransactionRequest();
        transactionRequest.setCuentaOrigen("1234567890");
        transactionRequest.setCuentaDestino("0987654321");
        transactionRequest.setMonto(100.0);

        transaccion = new Transaccion();
        transaccion.setCuentaOrigen("1234567890");
        transaccion.setCuentaDestino("0987654321");
        transaccion.setMonto(100.0);
        transaccion.setFecha(LocalDateTime.now());

        transactionResponse = new TransactionResponse();
        transactionResponse.setCuentaOrigen("1234567890");
        transactionResponse.setCuentaDestino("0987654321");
        transactionResponse.setMonto(100.0);

        saldoResponse = new SaldoResponse();
        saldoResponse.setSaldo(1000.0);
        saldoResponse.setTipoCuenta("AHORROS");
    }

    @Test
    @DisplayName("Consultar transacciones success")
    public void testConsultarHistorialTransacciones_Success() {
        List<Transaccion> transacciones = Arrays.asList(transaccion);

        when(transaccionRepository.findAll()).thenReturn(transacciones);
        when(transaccionMapper.getTransaccionResponseOfTransaccion(transaccion)).thenReturn(transactionResponse);

        List<TransactionResponse> result = transaccionServiceImp.consultarHistorialTransacciones();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1234567890", result.get(0).getCuentaOrigen());

        verify(transaccionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Registrar deposito success")
    public void testRegistrarDeposito_Success() {
        when(cuentaFeignClient.getAccountBalanceAndType(transactionRequest.getCuentaOrigen())).thenReturn(saldoResponse);
        when(transaccionMapper.getTransaccionOfTransaccionRequest(transactionRequest)).thenReturn(transaccion);
        when(transaccionRepository.save(transaccion)).thenReturn(transaccion);
        when(transaccionMapper.getTransaccionResponseOfTransaccion(transaccion)).thenReturn(transactionResponse);

        TransactionResponse result = transaccionServiceImp.registrarDeposito(transactionRequest);

        assertNotNull(result);
        assertEquals(100.0, result.getMonto());
        assertEquals("1234567890", result.getCuentaOrigen());

        verify(cuentaFeignClient, times(1)).getAccountBalanceAndType(transactionRequest.getCuentaOrigen());
        verify(cuentaFeignClient, times(1)).updateAccountBalance(eq(transactionRequest.getCuentaOrigen()), any(SaldoUpdateRequest.class));
        verify(transaccionRepository, times(1)).save(transaccion);
    }

    @Test
    @DisplayName("Registrar deposito - Not found account")
    public void testRegistrarDeposito_ResourceNotFoundException() {
        when(cuentaFeignClient.getAccountBalanceAndType(transactionRequest.getCuentaOrigen())).thenReturn(null);

        CustomExceptions.ResourceNotFoundException exception = assertThrows(
                CustomExceptions.ResourceNotFoundException.class,
                () -> transaccionServiceImp.registrarDeposito(transactionRequest)
        );

        assertEquals("Cuenta no encontrada", exception.getMessage());

        verify(cuentaFeignClient, times(1)).getAccountBalanceAndType(transactionRequest.getCuentaOrigen());
        verify(cuentaFeignClient, never()).updateAccountBalance(anyString(), any(SaldoUpdateRequest.class));
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    @DisplayName("Registrar retiro success")
    public void testRegistrarRetiro_Success() {
        when(cuentaFeignClient.getAccountBalanceAndType(transactionRequest.getCuentaOrigen())).thenReturn(saldoResponse);
        when(transaccionMapper.getTransaccionOfTransaccionRequest(transactionRequest)).thenReturn(transaccion);
        when(transaccionRepository.save(transaccion)).thenReturn(transaccion);
        when(transaccionMapper.getTransaccionResponseOfTransaccion(transaccion)).thenReturn(transactionResponse);

        TransactionResponse result = transaccionServiceImp.registrarRetiro(transactionRequest);

        assertNotNull(result);
        assertEquals(100.0, result.getMonto());
        assertEquals("1234567890", result.getCuentaOrigen());

        verify(cuentaFeignClient, times(1)).getAccountBalanceAndType(transactionRequest.getCuentaOrigen());
        verify(cuentaFeignClient, times(1)).updateAccountBalance(eq(transactionRequest.getCuentaOrigen()), any(SaldoUpdateRequest.class));
        verify(transaccionRepository, times(1)).save(transaccion);
    }

    @Test
    @DisplayName("Registrar retiro - Bad request Ahorros")
    public void testRegistrarRetiro_BadRequestException_Ahorros() {
        saldoResponse.setSaldo(50.0);

        when(cuentaFeignClient.getAccountBalanceAndType(transactionRequest.getCuentaOrigen())).thenReturn(saldoResponse);

        CustomExceptions.BadRequestException exception = assertThrows(
                CustomExceptions.BadRequestException.class,
                () -> transaccionServiceImp.registrarRetiro(transactionRequest)
        );

        assertEquals("No se puede realizar un retiro que deje el saldo en negativo para cuentas de ahorro", exception.getMessage());

        verify(cuentaFeignClient, times(1)).getAccountBalanceAndType(transactionRequest.getCuentaOrigen());
        verify(cuentaFeignClient, never()).updateAccountBalance(anyString(), any(SaldoUpdateRequest.class));
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    @DisplayName("Registrar retiro - Bad request Corriente")
    public void testRegistrarRetiro_BadRequestException_Corriente() {
        saldoResponse.setSaldo(-600.0);
        saldoResponse.setTipoCuenta("CORRIENTE");

        when(cuentaFeignClient.getAccountBalanceAndType(transactionRequest.getCuentaOrigen())).thenReturn(saldoResponse);

        CustomExceptions.BadRequestException exception = assertThrows(
                CustomExceptions.BadRequestException.class,
                () -> transaccionServiceImp.registrarRetiro(transactionRequest)
        );

        assertEquals("No se puede realizar un sobregiro mayor a -500 en cuentas corrientes", exception.getMessage());

        verify(cuentaFeignClient, times(1)).getAccountBalanceAndType(transactionRequest.getCuentaOrigen());
        verify(cuentaFeignClient, never()).updateAccountBalance(anyString(), any(SaldoUpdateRequest.class));
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    @DisplayName("Registrar transferencia success")
    public void testRegistrarTransferencia_Success() {
        SaldoResponse saldoDestino = new SaldoResponse();
        saldoDestino.setSaldo(500.0);
        saldoDestino.setTipoCuenta("AHORROS");

        when(cuentaFeignClient.getAccountBalanceAndType(transactionRequest.getCuentaOrigen())).thenReturn(saldoResponse);
        when(cuentaFeignClient.getAccountBalanceAndType(transactionRequest.getCuentaDestino())).thenReturn(saldoDestino);
        when(transaccionMapper.getTransaccionOfTransaccionRequest(transactionRequest)).thenReturn(transaccion);
        when(transaccionRepository.save(transaccion)).thenReturn(transaccion);
        when(transaccionMapper.getTransaccionResponseOfTransaccion(transaccion)).thenReturn(transactionResponse);

        TransactionResponse result = transaccionServiceImp.registrarTransferencia(transactionRequest);

        assertNotNull(result);
        assertEquals(100.0, result.getMonto());
        assertEquals("1234567890", result.getCuentaOrigen());

        verify(cuentaFeignClient, times(1)).getAccountBalanceAndType(transactionRequest.getCuentaOrigen());
        verify(cuentaFeignClient, times(1)).getAccountBalanceAndType(transactionRequest.getCuentaDestino());
        verify(cuentaFeignClient, times(2)).updateAccountBalance(anyString(), any(SaldoUpdateRequest.class));
        verify(transaccionRepository, times(1)).save(transaccion);
    }

    @Test
    @DisplayName("Registrar transferencia - Bad request Ahorros")
    public void testRegistrarTransferencia_BadRequestException_Ahorros() {
        saldoResponse.setSaldo(50.0);

        when(cuentaFeignClient.getAccountBalanceAndType(transactionRequest.getCuentaOrigen())).thenReturn(saldoResponse);

        CustomExceptions.BadRequestException exception = assertThrows(
                CustomExceptions.BadRequestException.class,
                () -> transaccionServiceImp.registrarTransferencia(transactionRequest)
        );

        assertEquals("No se puede realizar una transferencia que deje el saldo en negativo para cuentas de ahorro", exception.getMessage());

        verify(cuentaFeignClient, times(1)).getAccountBalanceAndType(transactionRequest.getCuentaOrigen());
        verify(cuentaFeignClient, never()).getAccountBalanceAndType(transactionRequest.getCuentaDestino());
        verify(cuentaFeignClient, never()).updateAccountBalance(anyString(), any(SaldoUpdateRequest.class));
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }
}
