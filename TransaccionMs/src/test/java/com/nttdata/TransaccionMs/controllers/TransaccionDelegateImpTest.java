package com.nttdata.TransaccionMs.controllers;

import com.nttdata.TransaccionMs.TransaccionDelegateImp;
import com.nttdata.TransaccionMs.business.TransaccionService;
import com.nttdata.TransaccionMs.model.TransactionRequest;
import com.nttdata.TransaccionMs.model.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
public class TransaccionDelegateImpTest {

    @Mock
    private TransaccionService transaccionService;

    @InjectMocks
    private TransaccionDelegateImp transaccionDelegateImp;

    private TransactionRequest transactionRequest;
    private TransactionResponse transactionResponse;

    @BeforeEach
    public void setUp() {

        transactionRequest = new TransactionRequest();
        transactionRequest.setCuentaOrigen("1234567890");
        transactionRequest.setCuentaDestino("0987654321");
        transactionRequest.setMonto(100.0);

        transactionResponse = new TransactionResponse();
        transactionResponse.setCuentaOrigen("1234567890");
        transactionResponse.setCuentaDestino("0987654321");
        transactionResponse.setMonto(100.0);
    }

    @Test
    @DisplayName("Consultar Historial Transacciones")
    public void testConsultarHistorialTransacciones_Success() {
        List<TransactionResponse> historial = Arrays.asList(transactionResponse);

        when(transaccionService.consultarHistorialTransacciones()).thenReturn(historial);

        ResponseEntity<List<TransactionResponse>> response = transaccionDelegateImp.consultarHistorialTransacciones();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("1234567890", response.getBody().get(0).getCuentaOrigen());

        verify(transaccionService, times(1)).consultarHistorialTransacciones();
    }

    @Test
    @DisplayName("Registrar Deposito")
    public void testRegistrarDeposito_Success() {
        when(transaccionService.registrarDeposito(transactionRequest)).thenReturn(transactionResponse);

        ResponseEntity<TransactionResponse> response = transaccionDelegateImp.registrarDeposito(transactionRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("1234567890", response.getBody().getCuentaOrigen());

        verify(transaccionService, times(1)).registrarDeposito(transactionRequest);
    }

    @Test
    @DisplayName("Registrar Retiro")
    public void testRegistrarRetiro_Success() {
        when(transaccionService.registrarRetiro(transactionRequest)).thenReturn(transactionResponse);

        ResponseEntity<TransactionResponse> response = transaccionDelegateImp.registrarRetiro(transactionRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("1234567890", response.getBody().getCuentaOrigen());

        verify(transaccionService, times(1)).registrarRetiro(transactionRequest);
    }

    @Test
    @DisplayName("Registrar Transferencia")
    public void testRegistrarTransferencia_Success() {
        when(transaccionService.registrarTransferencia(transactionRequest)).thenReturn(transactionResponse);

        ResponseEntity<TransactionResponse> response = transaccionDelegateImp.registrarTransferencia(transactionRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("1234567890", response.getBody().getCuentaOrigen());

        verify(transaccionService, times(1)).registrarTransferencia(transactionRequest);
    }

}