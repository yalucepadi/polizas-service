
# Reto IDM: Microservicios de Poliza y Siniestro

Introducción

-El proyecto implementa dos microservicios:

Microservicio de Poliza: Gestiona la creación de poliza y busqueda por DNI y Id.

-Microservicio de Siniestro:

Gestiona la creacion del Siniestro, usando los Id de las poliza(Webclient conexión), busqueda por Id, por polizaId.



## 🚀 Tecnologías Usadas


- Spring Boot 3.2.0
- Spring WebFlux (programación reactiva, funcional)
- Spring WebClient
- Spring test reactor



## Uso

- Crear poliza

```javascript
curl --location 'http://localhost:8080/api/polizas' \
--header 'Content-Type: application/json' \
--data '{
    "id": "POL12346",
    "tipoSeguro": "CASA",
    "fechaInicio": "2025-08-01",
    "fechaFin": "2026-08-01",
    "dniCliente": "12345679"
}'
```
- busqueda por DNI

```javascript
curl --location 'http://localhost:8080/api/polizas?dni=12345679' \
--header 'Content-Type: application/json' \
--data ''
```



## Appendice

Siniestro microservicio:

https://github.com/yalucepadi/siniestros-service

