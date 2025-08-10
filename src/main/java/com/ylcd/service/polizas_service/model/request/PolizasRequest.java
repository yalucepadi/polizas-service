package com.ylcd.service.polizas_service.model.request;

import java.time.LocalDate;

public record PolizasRequest(
         String id,
         String tipoSeguro,
         LocalDate fechaInicio,
         LocalDate fechaFin,
         String dniCliente
) {



}
