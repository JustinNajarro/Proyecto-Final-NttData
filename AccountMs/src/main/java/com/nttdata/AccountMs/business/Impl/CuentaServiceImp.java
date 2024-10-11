package com.nttdata.AccountMs.business.Impl;

import com.nttdata.AccountMs.business.CuentaMapper;
import com.nttdata.AccountMs.business.CuentaService;

import com.nttdata.AccountMs.clients.CustomerFeignClient;
import com.nttdata.AccountMs.exception.CustomExceptions;
import com.nttdata.AccountMs.model.CuentaRequest;
import com.nttdata.AccountMs.model.CuentaResponse;
import com.nttdata.AccountMs.model.InlineObject;
import com.nttdata.AccountMs.model.SaldoTipoResponse;
import com.nttdata.AccountMs.model.entity.Cuenta;
import com.nttdata.AccountMs.model.entity.TipoCuentaEnum;
import com.nttdata.AccountMs.repository.CuentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CuentaServiceImp implements CuentaService {

    @Autowired
    CuentaRepository cuentaRepository;

    @Autowired
    CuentaMapper cuentaMapper;

    @Autowired
    CustomerFeignClient customerFeignClient;

    @Override
    public List<CuentaResponse> listAllAccounts() {
        return Optional.of(cuentaRepository.findAll().stream()
                        .map(cuentaMapper::getCuentaResponseOfCuenta)
                        .collect(Collectors.toList()))
                .filter(cuentas -> !cuentas.isEmpty())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("No se encontraron cuentas"));
    }

    @Override
    public CuentaResponse createAccount(CuentaRequest cuentaRequest) {

        Boolean customerExists = customerFeignClient.checkIfCustomerExists(cuentaRequest.getClienteId());

        if (!Boolean.TRUE.equals(customerExists)) {
            throw new CustomExceptions.BadRequestException("El cliente no existe.");
        }

        if (cuentaRequest.getSaldo() <= 0) {
            throw new CustomExceptions.BadRequestException("El saldo inicial debe ser mayor a 0.");
        }

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(generarNumeroCuentaUnico());
        cuenta.setSaldo(cuentaRequest.getSaldo());
        cuenta.setTipoCuenta(cuentaMapper.mapToEntityTipoCuenta(cuentaRequest.getTipoCuenta()));
        cuenta.setClienteId(cuentaRequest.getClienteId());

        Cuenta cuentaSave = cuentaRepository.save(cuenta);

        return cuentaMapper.getCuentaResponseOfCuenta(cuentaSave);
    }

    @Override
    public CuentaResponse getAccountById(Integer id) {
        return cuentaRepository.findById(id)
                .map(cuentaMapper::getCuentaResponseOfCuenta)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Cuenta no existente con ID: " + id));
    }

    @Override
    public void deleteAccountById(Integer id) {
        cuentaRepository.findById(id)
                .ifPresentOrElse(cuentaRepository::delete,
                        () -> { throw new CustomExceptions.ResourceNotFoundException("Cuenta no existente con ID: " + id);
                });
    }

    @Override
    public void updateAccountBalance(String numeroCuenta, InlineObject inlineObject) {
        Optional.ofNullable(cuentaRepository.findByNumeroCuenta(numeroCuenta))
                .map(cuentaOpt -> {
                    if (cuentaOpt.isEmpty()) {
                        throw new CustomExceptions.ResourceNotFoundException("Cuenta no encontrada");
                    }
                    Cuenta cuenta = cuentaOpt.get();

                    double nuevoSaldo = inlineObject.getNuevoSaldo();
                    cuenta.setSaldo(nuevoSaldo);

                    return cuentaRepository.save(cuenta);
                }).orElseThrow(() -> new CustomExceptions.BadRequestException("No se pudo actualizar el saldo"));
    }

    @Override
    public SaldoTipoResponse getAccountBalanceAndType(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .map(cuentaMapper::getSaldoOfCuenta)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Cuenta no encontrada"));
    }

    private String generarNumeroCuentaUnico() {
        String numeroCuenta;
        boolean existe;

        do {
            numeroCuenta = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            existe = cuentaRepository.existsByNumeroCuenta(numeroCuenta);
        } while (existe);

        return numeroCuenta;
    }

}
