## Implementación de Seguridad - Fase 2

Este documento describe las medidas de seguridad implementadas en **Fase 2** para mitigar las vulnerabilidades identificadas en Fase 1.

-----

## Autenticación JWT

La autenticación se basa en **JSON Web Tokens (JWT)** con firma **RSA asimétrica**.

### Configuración técnica

* **Algoritmo:** RS256 (RSA-SHA256)
* **Tamaño de clave:** 2048 bits
* **Duración del token:** 3600 segundos (1 hora)
* **Issuer:** `https://vulntrack.io`

El token JWT incluye los siguientes claims:

* `upn`: Username del usuario
* `groups`: Array con el rol del usuario (para validación de `@RolesAllowed`)
* `userId`: ID interno del usuario en base de datos
* `email`: Email del usuario
* `fullName`: Nombre completo
* `iat`: Timestamp de emisión
* `exp`: Timestamp de expiración

### Implementación

La generación de tokens se realiza en `AuthenticationService`:

```java
public String generateToken(User user) {
    Set<String> roles = new HashSet<>();
    roles.add(user.getRole().name());
    return Jwt.issuer("https://vulntrack.io")
        .upn(user.getUsername().value())
        .groups(roles)
        .claim("userId", user.id)
        .claim("email", user.getEmail().value())
        .claim("fullName", user.getFullName())
        .expiresIn(Duration.ofHours(1))
        .sign();
}
```

La validación del token es automática por parte de **Quarkus SmallRye JWT**. Verifica:

1.  Firma RSA con la clave pública
2.  Issuer correcto
3.  Token no expirado
4.  Estructura de claims válida

-----

## Password Hashing con BCrypt

Los passwords se almacenan hasheados usando **BCrypt** con **cost factor 12**.

### Formato del hash

BCrypt genera hashes con el siguiente formato:

```
$2a$12$GYbx3LjE5CD0q85KBJG8vOEmSSlth20gMIRYR0Dk2XPKAgT9E5tm6
```

Donde:

* `$2a$`: Identificador del algoritmo BCrypt
* `12`: Cost factor ($2^{12} = 4096$ iteraciones)
* Los siguientes 22 caracteres: **Salt aleatorio**
* Los últimos 31 caracteres: **Hash resultante**

### Por qué BCrypt

BCrypt fue elegido por:

1.  **Salt automático** único por password
2.  **Cost factor ajustable** (resistencia a brute force)
3.  Algoritmo diseñado específicamente para passwords (**lento por diseño**)
4.  Resistente a ataques de timing

La implementación usa `BcryptUtil` de Quarkus Elytron:

```java
// Al crear usuario
String hashedPassword = BcryptUtil.bcryptHash(plainPassword);

// Al autenticar
boolean valid = BcryptUtil.matches(plainPassword, hashedPassword);
```

-----

## Control de Acceso Basado en Roles (RBAC)

El sistema define tres roles con permisos incrementales:

* **ANALYST**: Rol de **solo lectura**. Puede consultar CVEs, productos y vendors, pero no puede crear ni modificar nada.
* **RESEARCHER**: Puede leer y escribir CVEs, productos y vendors. **No puede gestionar usuarios ni eliminar recursos**.
* **ADMIN**: **Control total** del sistema. Puede realizar todas las operaciones incluyendo gestión de usuarios y eliminación de recursos.

### Implementación

RBAC se implementa mediante la anotación **`@RolesAllowed`** de Jakarta Security:

```java
@Query("users")
@RolesAllowed("ADMIN")
public List<User> getAllUsers() {
    return User.listAll();
}

@Mutation("createCVE")
@RolesAllowed({"RESEARCHER", "ADMIN"})
public CVE createCVE(...) {
    // implementación
}
```

Cuando un usuario sin los permisos necesarios intenta ejecutar una operación, Quarkus retorna un **error HTTP 403 Forbidden** automáticamente.

### Matriz de permisos

| Recurso | Query (read) | Create | Update | Delete / Other | Roles Permitidos |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Users** | Solo ADMIN | Solo ADMIN | Solo ADMIN | Deactivate: Solo ADMIN | ADMIN |
| **CVEs** | ANALYST, RESEARCHER, ADMIN | RESEARCHER, ADMIN | RESEARCHER, ADMIN | Add/Remove Products: RESEARCHER, ADMIN | ANALYST, RESEARCHER, ADMIN |
| **Products** | ANALYST, RESEARCHER, ADMIN | RESEARCHER, ADMIN | RESEARCHER, ADMIN | Solo ADMIN | ANALYST, RESEARCHER, ADMIN |
| **Vendors** | ANALYST, RESEARCHER, ADMIN | RESEARCHER, ADMIN | RESEARCHER, ADMIN | Solo ADMIN | ANALYST, RESEARCHER, ADMIN |

-----

## Mitigación de SQL Injection

En Fase 1, las queries `searchUsers` y `searchCVEs` eran vulnerables a **SQL injection** por usar concatenación directa de strings en SQL nativo:

```java
// VULNERABLE - Fase 1
return User.getEntityManager()
    .createNativeQuery(
        "SELECT * FROM users WHERE username LIKE '%" + query + "%'",
        User.class
    )
    .getResultList();
```

Un atacante podía enviar `query = "' OR '1'='1"` para ejecutar:

```sql
SELECT * FROM users WHERE username LIKE '%' OR '1'='1%'
```

Esto retornaba todos los usuarios del sistema.

En Fase 2, las búsquedas usan **Panache con queries parametrizadas**:

```java
// SEGURO - Fase 2
String searchPattern = "%" + query + "%";
return User.list("username LIKE ?1 OR email LIKE ?2", searchPattern, searchPattern);
```

Panache genera **prepared statements** automáticamente. El mismo ataque ahora busca literalmente el string `%' OR '1'='1%`, que no coincide con ningún username real.

-----

## Protecciones a nivel GraphQL

### Query Depth Limiting

Configurado con `quarkus.smallrye-graphql.max-depth=5` para prevenir queries profundamente anidadas que causan alto consumo de CPU y memoria.

Una query que exceda 5 niveles de profundidad es rechazada antes de ejecutarse.

### Introspection Deshabilitada

La introspection de GraphQL está deshabilitada con `quarkus.smallrye-graphql.security.deny-introspection=true`.

Esto previene que atacantes puedan mapear el schema completo de la API ejecutando:

```graphql
query {
    __schema {
        types {
            name
            fields {
                name
            }
        }
    }
}
```

En Fase 1 esto retornaba toda la estructura de la API. En Fase 2 retorna un error.

### Query Batching Deshabilitado

El batching de queries está deshabilitado con `quarkus.smallrye-graphql.enable-query-batching=false` para prevenir ataques de **DoS** mediante alias:

```graphql
query {
    u1: users { id }
    u2: users { id }
    u3: users { id }
    ... 100+ aliases
}
```

Esto forzaba al servidor a ejecutar la misma query 100 veces en una sola petición HTTP.

-----

## Comparativa: Fase 1 vs Fase 2

Las vulnerabilidades críticas identificadas en Fase 1 han sido mitigadas:

| Vulnerabilidad | Fase 1 | Fase 2 |
| :--- | :--- | :--- |
| **Sin Autenticación (CVSS 9.8)** | Cualquiera podía ejecutar cualquier query o mutation | Todas las operaciones requieren **JWT válido** |
| **Sin Autorización (CVSS 9.8)** | Un usuario ANALYST podía hacerse ADMIN | Solo usuarios con rol **ADMIN** pueden cambiar roles |
| **Passwords en Texto Plano (CVSS 10.0)** | Passwords almacenados como strings sin hash | **BCrypt** con cost factor 12 |
| **SQL Injection (CVSS 9.8)** | Concatenación directa en `searchUsers` y `searchCVEs` | Queries **parametrizadas** con Panache |
| **Introspection Habilitada (CVSS 5.3)** | Atacante podía mapear toda la API | Introspection **deshabilitada** |
| **Query Depth DoS (CVSS 7.5)** | Sin límite de profundidad | Máximo **5 niveles** |

**Score CVSS total:**

* **Fase 1:** 62.0 (5 vulnerabilidades críticas, 1 alta, 1 media)
* **Fase 2:** 3.0 (DoS residual por paginación no implementada)

-----

## Testing de Seguridad

Estos son ejemplos de queries para verificar que las protecciones funcionan correctamente.

**Login exitoso:**

```graphql
mutation {
    login(username: "admin", password: "admin123") {
        token
        role
    }
}
# Debe retornar un token JWT válido.
```

**Login con password incorrecta:**

```graphql
mutation {
    login(username: "admin", password: "wrongpass")
}
# Debe retornar error "Credenciales inválidas".
```

**Query sin autenticación:**

```graphql
query {
    users {
        username
    }
}
# Debe retornar error 401 Unauthorized.
```

**Intento de escalación de privilegios (ANALYST intentando crear usuario):**

```graphql
# Primero login como analyst
# Luego con el token:
mutation {
    createUser(
        username: "hacker"
        email: "hack@test.com"
        fullName: "Hacker"
        password: "hack123"
        role: ADMIN
    ) {
        id
    }
}
# Debe retornar error 403 Forbidden.
```

**SQL Injection bloqueado:**

```graphql
query {
    searchUsers(query: "' OR '1'='1")
}
# Debe retornar lista vacía o usuarios que literalmente coincidan con ese string (ninguno).
```

-----

## Referencias

* OWASP API Security Top 10 (2023)
* RFC 8725: JSON Web Token Best Current Practices
* Provos & Mazières (1999): A Future-Adaptable Password Scheme
* OWASP GraphQL Cheat Sheet
* Secure by Design (Manning, 2019)

-----

**Autor:** Bladimir Gonzales Miranda
**TFM:** Seguridad en APIs GraphQL con Quarkus
**Universidad:** UNIR - Máster en Ciberseguridad
**Fecha:** Noviembre 2024