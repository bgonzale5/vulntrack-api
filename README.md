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

### GraphQL API Funcionando

Consulta de CVEs críticos con productos afectados:

![GraphQL Query Success](docs/screenshots/01-graphql-query-success.png)

### Validación de Domain Primitives

Domain Primitive `CVEId` rechazando formato inválido (Secure by Design):

![Domain Validation](docs/screenshots/02-domain-primitive-validation.png)

### Demostración de Vulnerabilidad - SQL Injection

SQL Injection en `searchUsers` permitiendo bypass completo:

![SQL Injection](docs/screenshots/03-sql-injection-vulnerability.png)

⚠️ **ADVERTENCIA**: Esta vulnerabilidad es intencional para fines demostrativos. Se mitigará en Fase 2.

### Esquema de Base de Datos

Estructura normalizada con constraints y relaciones:

![Database Schema](docs/screenshots/04-database-schema.png)

### Aplicación en Ejecución

Quarkus 3.29.0 con banner personalizado del TFM:

![Quarkus Startup](docs/screenshots/05-quarkus-startup.png)

### Historial de Commits

Desarrollo iterativo evidenciado en GitHub:

![GitHub Commits](docs/screenshots/06-github-commits.png)

---


## Referencias

- **Black Hat GraphQL** (Dolev Farhi & Nick Aleks, 2023)
- **Secure by Design** (Manning, 2019)
- **OWASP API Security Top 10**
- **OWASP GraphQL Cheat Sheet**

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

## Notas del Desarrollo

Este proyecto es parte del TFM "Seguridad en APIs GraphQL con Quarkus: Evaluación de Vulnerabilidades y Estrategias de Mitigación desde un Enfoque Secure by Design".

**Desarrollo iterativo**: Cada fase se mantiene en su propia rama Git como evidencia académica del progreso.

---

**Autor**: Bladimir Gonzales Miranda  
**Director**: Rubén Pérez Chacón  
**Universidad**: UNIR - Máster en Ciberseguridad