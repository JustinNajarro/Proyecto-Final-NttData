package com.nttdata.CustomerMs.services.impl;

import com.nttdata.CustomerMs.business.ClienteMapper;
import com.nttdata.CustomerMs.business.Imp.ClienteServiceImp;
import com.nttdata.CustomerMs.exception.CustomExceptions;
import com.nttdata.CustomerMs.model.ClienteRequest;
import com.nttdata.CustomerMs.model.ClienteResponse;
import com.nttdata.CustomerMs.model.entity.Cliente;
import com.nttdata.CustomerMs.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceImlTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImp clienteServiceImp;

    private Cliente cliente1;
    private Cliente cliente2;
    private ClienteResponse clienteResponse1;
    private ClienteResponse clienteResponse2;
    private ClienteRequest clienteRequest;

    @BeforeEach
    public void setUp() {
        cliente1 = crearCliente(1, "Justin", "Najarro", "12345678", "justin@gmail.com");
        cliente2 = crearCliente(2, "Melanie", "Herrera", "87654321", "melanie@gmail.com");

        clienteResponse1 = crearClienteResponse(1, "Justin", "Najarro", "12345678", "justin@gmail.com");
        clienteResponse2 = crearClienteResponse(2, "Melanie", "Herrera", "87654321", "melanie@gmail.com");

        clienteRequest = crearClienteRequest("Justin", "Najarro", "12345678", "justin@gmail.com");
    }

    @Test
    @DisplayName("List all customers success")
    public void testListAllCustomers_Success() {
        List<Cliente> clientes = Arrays.asList(cliente1, cliente2);

        when(clienteRepository.findAll()).thenReturn(clientes);
        when(clienteMapper.getClienteResponseOfCliente(cliente1)).thenReturn(clienteResponse1);
        when(clienteMapper.getClienteResponseOfCliente(cliente2)).thenReturn(clienteResponse2);

        List<ClienteResponse> result = clienteServiceImp.listAllCustomers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Justin", result.get(0).getNombre());
        assertEquals("Melanie", result.get(1).getNombre());

        verify(clienteRepository, times(1)).findAll();
        verify(clienteMapper, times(1)).getClienteResponseOfCliente(cliente1);
        verify(clienteMapper, times(1)).getClienteResponseOfCliente(cliente2);
    }

    @Test
    @DisplayName("List all customers - No customers found")
    public void testListAllCustomers_NoCustomersFound() {
        when(clienteRepository.findAll()).thenReturn(Arrays.asList());

        CustomExceptions.ResourceNotFoundException exception = assertThrows(
                CustomExceptions.ResourceNotFoundException.class,
                () -> clienteServiceImp.listAllCustomers()
        );

        assertEquals("No se encontraron clientes", exception.getMessage());

        verify(clienteRepository, times(1)).findAll();
        verifyNoMoreInteractions(clienteMapper);
    }

    @Test
    @DisplayName("Create customer success")
    public void testCreateCustomer_Success() {
        Cliente cliente = crearCliente(null, "Justin", "Najarro", "12345678", "justin@gmail.com");
        Cliente savedCliente = crearCliente(1, "Justin", "Najarro", "12345678", "justin@gmail.com");

        when(clienteRepository.existsByDni("12345678")).thenReturn(false);
        when(clienteMapper.getClienteOfClienteRequest(clienteRequest)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(savedCliente);
        when(clienteMapper.getClienteResponseOfCliente(savedCliente)).thenReturn(clienteResponse1);

        ClienteResponse result = clienteServiceImp.createCustomer(clienteRequest);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Justin", result.getNombre());

        verify(clienteRepository, times(1)).existsByDni("12345678");
        verify(clienteMapper, times(1)).getClienteOfClienteRequest(clienteRequest);
        verify(clienteRepository, times(1)).save(cliente);
        verify(clienteMapper, times(1)).getClienteResponseOfCliente(savedCliente);
    }

    @Test
    @DisplayName("Create customer - Dni already exists")
    public void testCreateCustomer_DniAlreadyExists() {
        when(clienteRepository.existsByDni("12345678")).thenReturn(true);

        CustomExceptions.DniAlreadyExistsException exception = assertThrows(
                CustomExceptions.DniAlreadyExistsException.class,
                () -> clienteServiceImp.createCustomer(clienteRequest)
        );

        assertEquals("El cliente con DNI 12345678 ya existe", exception.getMessage());

        verify(clienteRepository, times(1)).existsByDni("12345678");
        verifyNoMoreInteractions(clienteMapper);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create customer - Missing required fields")
    public void testCreateCustomer_MissingRequiredFields() {
        ClienteRequest clienteRequestInvalido = crearClienteRequest("", "Najarro", "12345678", "justin@gmail.com");

        CustomExceptions.BadRequestException exception = assertThrows(
                CustomExceptions.BadRequestException.class,
                () -> clienteServiceImp.createCustomer(clienteRequestInvalido)
        );

        assertEquals("El campo 'nombre' es obligatorio y no puede estar vacío.", exception.getMessage());

        verifyNoInteractions(clienteRepository);
        verifyNoInteractions(clienteMapper);
    }

    @Test
    @DisplayName("Get customer by id success")
    public void testGetCustomerById_Success() {
        Integer id = 1;

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente1));
        when(clienteMapper.getClienteResponseOfCliente(cliente1)).thenReturn(clienteResponse1);

        ClienteResponse result = clienteServiceImp.getCustomerById(id);

        assertNotNull(result);
        assertEquals("Justin", result.getNombre());

        verify(clienteRepository, times(1)).findById(id);
        verify(clienteMapper, times(1)).getClienteResponseOfCliente(cliente1);
    }

    @Test
    @DisplayName("Get customer by id - No found")
    public void testGetCustomerById_NotFound() {
        Integer id = 1;

        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        CustomExceptions.ResourceNotFoundException exception = assertThrows(
                CustomExceptions.ResourceNotFoundException.class,
                () -> clienteServiceImp.getCustomerById(id)
        );

        assertEquals("Cliente no encontrado con ID: 1", exception.getMessage());

        verify(clienteRepository, times(1)).findById(id);
        verifyNoMoreInteractions(clienteMapper);
    }

    @Test
    @DisplayName("Delete customer by id success")
    public void testDeleteCustomerById_Success() {
        Integer id = 1;

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente1));

        clienteServiceImp.deleteCustomerById(id);

        verify(clienteRepository, times(1)).findById(id);
        verify(clienteRepository, times(1)).delete(cliente1);
    }

    @Test
    @DisplayName("Delete customer by id - Not found")
    public void testDeleteCustomerById_NotFound() {
        Integer id = 1;

        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        CustomExceptions.ResourceNotFoundException exception = assertThrows(
                CustomExceptions.ResourceNotFoundException.class,
                () -> clienteServiceImp.deleteCustomerById(id)
        );

        assertEquals("Cliente no encontrado con ID: 1", exception.getMessage());

        verify(clienteRepository, times(1)).findById(id);
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Update customer by id success")
    public void testUpdateCustomerById_Success() {
        Integer id = 1;

        Cliente clienteExistente = crearCliente(1, "Justin", "Najarro", "12345678", "justin@gmail.com");
        Cliente clienteActualizado = crearCliente(1, "Justin", "Villagaray", "12345679", "justin@gmail.com");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

        clienteServiceImp.updateCustomerById(id, clienteRequest);

        verify(clienteRepository, times(1)).findById(id);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    public void testCheckIfCustomerExists_True() {
        Integer Id = 1;
        when(clienteRepository.existsById(Id)).thenReturn(true);

        Boolean exists = clienteServiceImp.checkIfCustomerExists(Id);

        assertTrue(exists);
        verify(clienteRepository, times(1)).existsById(Id);
    }


    private Cliente crearCliente(Integer id, String nombre, String apellido, String dni, String email) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setDni(dni);
        cliente.setEmail(email);
        return cliente;
    }

    private ClienteRequest crearClienteRequest(String nombre, String apellido, String dni, String email) {
        ClienteRequest clienteRequest = new ClienteRequest();
        clienteRequest.setNombre(nombre);
        clienteRequest.setApellido(apellido);
        clienteRequest.setDni(dni);
        clienteRequest.setEmail(email);
        return clienteRequest;
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



}
