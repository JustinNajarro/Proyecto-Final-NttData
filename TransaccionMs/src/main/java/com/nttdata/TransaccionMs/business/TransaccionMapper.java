package com.nttdata.TransaccionMs.business;

import com.nttdata.TransaccionMs.model.TransactionRequest;
import com.nttdata.TransaccionMs.model.TransactionResponse;
import com.nttdata.TransaccionMs.model.entity.TipoTransaccionEnum;
import com.nttdata.TransaccionMs.model.entity.Transaccion;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class TransaccionMapper {

    public Transaccion getTransaccionOfTransaccionRequest(TransactionRequest request){
        Transaccion entity = new Transaccion();
        entity.setTipo(mapToEntityTipoTransaccion(request.getTipo()));
        entity.setMonto(request.getMonto());
        entity.setCuentaOrigen(request.getCuentaOrigen());
        entity.setCuentaDestino(request.getCuentaDestino());

        return entity;
    }

    public TipoTransaccionEnum mapToEntityTipoTransaccion(TransactionRequest.TipoEnum tipoRequest) {
        return TipoTransaccionEnum.valueOf(tipoRequest.name());
    }

    public TransactionResponse getTransaccionResponseOfTransaccion(Transaccion entity) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setTipo(mapToResponseTipoTransaccion(entity.getTipo()));
        transactionResponse.setMonto(entity.getMonto());
        OffsetDateTime offsetDateTime = entity.getFecha().atOffset(ZoneOffset.UTC);
        transactionResponse.setFecha(offsetDateTime);
        transactionResponse.setCuentaOrigen(entity.getCuentaOrigen());
        transactionResponse.setCuentaDestino(entity.getCuentaDestino());

        return transactionResponse;
    }

    public TransactionResponse.TipoEnum mapToResponseTipoTransaccion(TipoTransaccionEnum tipoTransaccionEntity) {
        return TransactionResponse.TipoEnum.valueOf(tipoTransaccionEntity.name());
    }
}
