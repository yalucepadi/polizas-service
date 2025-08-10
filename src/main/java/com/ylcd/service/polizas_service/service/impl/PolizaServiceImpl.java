package com.ylcd.service.polizas_service.service.impl;

import com.ylcd.service.polizas_service.model.request.PolizasRequest;
import com.ylcd.service.polizas_service.service.PolizaService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PolizaServiceImpl implements PolizaService {
    // ConcurrentHashMap para acceso thread-safe
    private final Map<String, PolizasRequest> polizas = new ConcurrentHashMap<>();

    @Override
    public Mono<PolizasRequest> obtenerPorId(String id) {
        return Mono.defer(() -> {
            Optional<PolizasRequest> optional = Optional.ofNullable(polizas.get(id));
            return optional.map(Mono::just).orElseGet(Mono::empty);
        });
    }

    @Override
    public Flux<PolizasRequest> obtenerPorDni(String dni) {
        return Flux.fromStream(polizas.values().stream()
                .filter(p -> p.dniCliente().equals(dni)));
    }

    @Override
    public Mono<PolizasRequest> crearPoliza(PolizasRequest poliza) {
        polizas.put(poliza.id(), poliza);
        return Mono.just(poliza);
    }

    @Override
    public Mono<Boolean> estaVigente(String id) {
        return obtenerPorId(id)
                .map(p -> LocalDate.now().isBefore(p.fechaFin()))
                .defaultIfEmpty(false);
    }

}
