# Arquitectura del Sistema

## Diagrama de Componentes
```mermaid
graph TB
    A[GraphQL UI<br/>localhost:8080/q/graphql-ui] --> B[SmallRye GraphQL]
    B --> C[Resolvers<br/>UserResource, CVEResource]
    C --> D[Services<br/>Lógica de negocio]
    D --> E[Entities + Domain Primitives<br/>User, CVE, CVEId, Email]
    E --> F[Panache ORM]
    F --> G[(PostgreSQL 16<br/>Docker)]
    
    H[Flyway Migrations] --> G
    
    style A fill:#e1f5ff
    style B fill:#fff4e1
    style C fill:#ffe1e1
    style D fill:#e1ffe1
    style E fill:#f0e1ff
    style F fill:#ffe1f0
    style G fill:#e1e1e1
    style H fill:#fff4e1
```

## Capas de la Aplicación

### 1. Capa de Presentación
- **GraphQL UI**: Interfaz interactiva para explorar la API
- **SmallRye GraphQL**: Motor GraphQL integrado en Quarkus

### 2. Capa de Aplicación
- **Resolvers**: Exponen queries y mutations
- **DTOs**: Inputs para validación

### 3. Capa de Dominio
- **Entities**: User, Vendor, Product, CVE
- **Domain Primitives**: CVEId, Email, Username, Severity, Role
- **Lógica de negocio**: Métodos como `isCriticalOrHigh()`, `affectsVendor()`

### 4. Capa de Infraestructura
- **Panache**: Simplificación de Hibernate
- **PostgreSQL**: Base de datos relacional
- **Flyway**: Control de versiones del schema

## Patrones Aplicados

- **Domain-Driven Design (DDD)**: Modelo de dominio rico
- **Repository Pattern**: Abstracción del acceso a datos
- **Domain Primitives**: Validación encapsulada (Secure by Design)