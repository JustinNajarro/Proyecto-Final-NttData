package com.nttdata.CustomerMs.controllers;

import com.nttdata.CustomerMs.ClienteDelegateImp;
import com.nttdata.CustomerMs.business.ClienteService;
import com.nttdata.CustomerMs.model.ClienteRequest;
import com.nttdata.CustomerMs.model.ClienteResponse;
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
public class ClienteDelegateImpTest {
    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteDelegateImp clienteDelegateImp;

    private ClienteResponse clienteResponse1;
    private ClienteResponse clienteResponse2;
    private ClienteRequest clienteRequest;

    @BeforeEach
    public void setUp() {
        clienteResponse1 = crearClienteResponse(1, "Justin", "Najarro", "12345678", "justin@gmail.com");
        clienteResponse2 = crearClienteResponse(2, "Melanie", "Herrera", "87654321", "melanie@gmail.com");

        clienteRequest = crearClienteRequest("Justin", "Najarro", "12345678", "justin@gmail.com");
    }

    @Test
    @DisplayName("List all customers")
    public void testListAllCustomers_Success() {
        List<ClienteResponse> clienteResponses = Arrays.asList(clienteResponse1, clienteResponse2);

        when(clienteService.listAllCustomers()).thenReturn(clienteResponses);

        ResponseEntity<List<ClienteResponse>> response = clienteDelegateImp.listAllCustomers();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertEquals("Justin", response.getBody().get(0).getNombre());

        verify(clienteService, times(1)).listAllCustomers();
    }

    @Test
    @DisplayName("Create customer")
    public void testCreateCustomer_Success() {
        when(clienteService.createCustomer(clienteRequest)).thenReturn(clienteResponse1);

        ResponseEntity<ClienteResponse> response = clienteDelegateImp.createCustomer(clienteRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Justin", response.getBody().getNombre());

        verify(clienteService, times(1)).createCustomer(clienteRequest);
    }

    @Test
    @DisplayName("Get customer by id")
    public void testGetCustomerById_Success() {
        Integer id = 1;

        when(clienteService.getCustomerById(id)).thenReturn(clienteResponse1);

        ResponseEntity<ClienteResponse> response = clienteDelegateImp.getCustomerById(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Justin", response.getBody().getNombre());

        verify(clienteService, times(1)).getCustomerById(id);
    }

    @Test
    @DisplayName("Delete customer by id")
    public void testDeleteCustomerById_Success() {
        Integer id = 1;

        ResponseEntity<Void> response = clienteDelegateImp.deleteCustomerById(id);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());

        verify(clienteService, times(1)).deleteCustomerById(id);
    }

    @Test
    @DisplayName("Update customer by id")
    public void testUpdateCustomerById_Success() {
        Integer id = 1;

        ResponseEntity<Void> response = clienteDelegateImp.updateCustomerById(id, clienteRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());

        verify(clienteService, times(1)).updateCustomerById(id, clienteRequest);
    }

    @Test
    public void testCheckIfCustomerExists_True() {
        Integer Id = 1;

        when(clienteService.checkIfCustomerExists(Id)).thenReturn(true);

        ResponseEntity<Boolean> response = clienteDelegateImp.checkIfCustomerExists(Id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());

        verify(clienteService, times(1)).checkIfCustomerExists(Id);
    }

    private ClienteResponse crearClienteResponse(Integer id, String nombre, String apellido, String dni, String email) {
        ClienteResponse clienteResponse = new ClienteResponse();
        clienteResponse.setId(id);
        clienteResponse.setNombre(nombre);
        clienteResponse.setApellido(apellido);
        clienteResponse.setDni(dni);
        clienteResponse.setEmail(email);
        return clienteResponse;
    }

    private ClienteRequest crearClienteRequest(String nombre, String apellido, String dni, String email) {
        ClienteRequest clienteRequest = new ClienteRequest();
        clienteRequest.setNombre(nombre);
        clienteRequest.setApellido(apellido);
        clienteRequest.setDni(dni);
        clienteRequest.setEmail(email);
        return clienteRequest;
    }
}
