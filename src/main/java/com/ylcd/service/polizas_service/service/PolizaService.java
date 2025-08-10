package com.ylcd.service.polizas_service.service;

import com.ylcd.service.polizas_service.model.request.PolizasRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolizaService {

    Mono<PolizasRequest> obtenerPorId(String id);
    Flux<PolizasRequest> obtenerPorDni(String dni);
    Mono<PolizasRequest> crearPoliza(PolizasRequest poliza);
    Mono<Boolean> estaVigente(String id);

}
