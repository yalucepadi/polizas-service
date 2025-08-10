package com.ylcd.service.polizas_service.config;

import com.ylcd.service.polizas_service.model.request.PolizasRequest;
import com.ylcd.service.polizas_service.service.impl.PolizaServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.time.LocalDate;

@Configuration
public class PolizasConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // en producción limita a los orígenes necesarios
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

    @Bean
    public CommandLineRunner dataLoader(PolizaServiceImpl polizaService) {
        return args -> {
            polizaService.crearPoliza(
                    new PolizasRequest("POL12345",
                            "SOAT",
                            LocalDate.now().minusDays(10),
                            LocalDate.now().plusYears(1),
                            "12345678")
            );

            polizaService.crearPoliza(
                    new PolizasRequest("POL23456",
                            "HOGAR",
                            LocalDate.now().minusMonths(3),
                            LocalDate.now().plusMonths(9),
                            "87654321")
            );
        };
    }
}