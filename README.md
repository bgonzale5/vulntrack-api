# VulnTrack API - GraphQL Security Assessment

Sistema de gestión de vulnerabilidades (CVE) desarrollado como parte del TFM en Ciberseguridad de UNIR.

**Objetivo**: Demostrar vulnerabilidades específicas de GraphQL y estrategias de mitigación siguiendo principios de **Secure by Design**.

## Stack Tecnológico

- **Framework**: Quarkus 3.29.0
- **Lenguaje**: Java 21
- **API**: SmallRye GraphQL
- **Base de datos**: PostgreSQL 16
- **ORM**: Hibernate ORM with Panache
- **Migraciones**: Flyway

## Arquitectura

El proyecto sigue Domain-Driven Design (DDD) con énfasis en **Domain Primitives**:

## Setup del Proyecto

### Prerequisitos

- Java 21 instalado
- Docker Desktop corriendo
- Maven 3.9+

### 1. Levantar la base de datos

```bash
docker-compose up -d
```

Esto levanta PostgreSQL 16 en el puerto 5432.

### 2. Compilar el proyecto

```bash
./mvnw clean compile
```

### 3. Ejecutar en modo dev

```bash
./mvnw quarkus:dev
```

La aplicación estará disponible en:
- **API GraphQL**: http://localhost:8080/graphql
- **GraphQL UI**: http://localhost:8080/q/graphql-ui

## Fases del Desarrollo

### Fase 1: Baseline Vulnerable (Actual)

**Objetivo**: Implementar API funcional SIN controles de seguridad.

- Domain Primitives (CVEId, Email, Severity)
- Modelo de dominio completo
- GraphQL API (queries + mutations)
-  NO hay autenticación
-  NO hay autorización
-  Introspection HABILITADA (vulnerable)
-  NO hay query depth limiting

**Branch**: `entrega-1-baseline`

### Fase 2: Security Hardening (Próxima)

- JWT Authentication
- RBAC (Role-Based Access Control)
- Field-level authorization
- Query depth limiting
- Query cost analysis
- Introspection disabled

**Branch**: `entrega-2-security`

### Fase 3: Testing & Validation (Final)

- Tests de seguridad automatizados
- Scripts de ataque (Python)
- Benchmarking
- Documentación completa

**Branch**: `entrega-3-testing`

## Principios de Secure by Design Aplicados

### Domain Primitives

Cada concepto del dominio tiene su propio tipo con validación encapsulada:

```java
// ANTES (vulnerable)
public void createCVE(String cveId, String email) {
    // No hay validación, cualquier string es aceptado
}

// DESPUÉS (seguro)
public void createCVE(CVEId cveId, Email email) {
    // Si el objeto existe, es válido por diseño
}
```

### Fail-Fast

Las validaciones ocurren en el constructor. Un objeto inválido no puede ser creado:

```java
CVEId.of("invalid");  // Lanza IllegalArgumentException inmediatamente
CVEId.of("CVE-2023-12345");  // Retorna CVEId válido
```

### Inmutabilidad

Todos los Domain Primitives son inmutables (final class, no setters), previniendo modificaciones no controladas.

### Arquitectura del Sistema

![Architecture Diagram](docs/architecture.md)

Para más detalles, ver [documentación de arquitectura](docs/architecture.md).

## Evidencia Visual del Desarrollo

### 1. GraphQL API Funcionando

Consulta de CVEs críticos con productos afectados y sus fabricantes:

![GraphQL Query Success](docs/screenshots/01-graphql-query-success.png)

**Demostración:** La API GraphQL retorna correctamente CVE-2023-12345 (Remote Code Execution in Windows 10) con severidad CRITICAL (9.8) y la lista completa de productos afectados.

---

### 2. Validación de Domain Primitives (Secure by Design)

Domain Primitive `CVEId` rechazando formato inválido con fail-fast:

![Domain Validation](docs/screenshots/02-domain-primitive-validation.png)

**Principio aplicado:** El Domain Primitive `CVEId` valida el formato CVE-YYYY-NNNNN en el constructor. Un objeto inválido **no puede ser creado**, previniendo datos corruptos en el sistema.

---

### 3. Demostración de Vulnerabilidad - SQL Injection

SQL Injection en `searchUsers` permitiendo bypass completo de autenticación:

![SQL Injection](docs/screenshots/03-sql-injection-vulnerability.png)

**ADVERTENCIA CRÍTICA**: Esta vulnerabilidad es **intencional** para fines demostrativos (Fase 1 - Baseline Vulnerable). La query `' OR '1'='1` retorna **TODOS** los usuarios con sus passwords en texto plano.

**CVSS Score:** 9.8 (Critical)  
**Mitigación:** Será implementada en Fase 2 usando Panache queries parametrizadas.

---

### 4. Esquema de Base de Datos

Estructura normalizada con foreign keys, constraints y control de versiones (Flyway):

![Database Schema](docs/screenshots/04-database-schema.png)

**Arquitectura:**
- 5 tablas principales: `users`, `vendors`, `products`, `cves`, `cve_affected_products`
- Relaciones: ManyToOne (Product → Vendor), ManyToMany (CVE ↔ Product)
- Constraints: UNIQUE (username, email, cve_id), CHECK (role, severity)
- Histórico de migraciones: `flyway_schema_history`

---

### 5. Aplicación en Ejecución

Quarkus 3.29.0 arrancando con Flyway migrations y features instaladas:

![Quarkus Startup](docs/screenshots/05-quarkus-startup.png)

**Tiempo de arranque:** 5.644s en modo dev  
**Migraciones ejecutadas:** 2 (execution time: 0.031s)  
**Features activas:** agroal, cdi, flyway, hibernate-orm, hibernate-orm-panache, hibernate-validator, jdbc-postgresql, narayana-jta, rest, smallrye-graphql

---

## Documentación Complementaria

**[Ejemplos de Queries GraphQL](docs/graphql_queries.md)**  
Colección completa de queries y mutations para probar la API. Incluye ejemplos por recurso (Users, Vendors, Products, CVEs) y casos de uso por rol.

**[Vectores de Ataque Demostrados](docs/attacks.md)**  
Documentación técnica de 7 vulnerabilidades ejecutables:
- SQL Injection (searchUsers, searchCVEs)
- Broken Access Control (sin autenticación, escalación de privilegios)
- Information Disclosure (passwords en texto plano)
- Denial of Service (queries circulares, sin paginación)

**IMPORTANTE:** Los ataques documentados son **intencionales** para fines académicos. Fase 1 = Baseline Vulnerable → Fase 2 = Mitigación.

---

## Referencias

- **Black Hat GraphQL** (Dolev Farhi & Nick Aleks, 2023) - Capítulos 4, 5, 6, 7, 8
- **Secure by Design** (Manning, 2019) - Domain Primitives, Fail-Fast
- **OWASP API Security Top 10** (2023)
- **OWASP GraphQL Cheat Sheet**
- **CWE-89**: SQL Injection
- **CWE-287**: Improper Authentication
- **CWE-862**: Missing Authorization

---

## Comandos Útiles
```bash
# Compilar
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Modo dev (hot reload)
./mvnw quarkus:dev

# Generar distribución
./mvnw package

# Detener Docker
docker-compose down

# Ver logs de la DB
docker-compose logs -f postgres
```

---

## Notas del Desarrollo

Este proyecto es parte del TFM **"Seguridad en APIs GraphQL con Quarkus: Evaluación de Vulnerabilidades y Estrategias de Mitigación desde un Enfoque Secure by Design"**.

**Desarrollo iterativo:** Cada fase se mantiene en su propia rama Git como evidencia académica del progreso.

- **Fase 1 (Actual):** Baseline vulnerable - API funcional sin controles de seguridad
- **Fase 2 (Siguiente):** Security Hardening - JWT, RBAC, ABAC, Query Limiting
- **Fase 3 (Final):** Testing & Validation - Automatización, benchmarking, documentación completa

---

**Autor:** Bladimir Gonzales Miranda  
**Director:** Rubén Pérez Chacón  
**Universidad:** UNIR - Máster en Ciberseguridad  
**Fecha:** Noviembre 2024