# GraphQL Queries - VulnTrack API

Colección de queries y mutations de ejemplo para probar la API.

FASE 1: Todas las queries están sin autenticación (vulnerable).

---

## QUERIES (Lectura)

### Usuarios

#### Obtener todos los usuarios
```graphql
query {
    users {
        id
        username
        email
        fullName
        role
        active
        createdAt
        updatedAt
    }
}
```

#### Obtener usuario por ID
```graphql
query {
    user(id: 1) {
        id
        username
        email
        fullName
        role
    }
}
```

#### Buscar usuario por username
```graphql
query {
    userByUsername(username: "admin") {
        id
        username
        email
        role
    }
}
```

#### Obtener usuarios por rol
```graphql
query {
    usersByRole(role: RESEARCHER) {
        id
        username
        email
        fullName
    }
}
```

#### Buscar usuarios (vulnerable a SQL Injection)
```graphql
query {
    searchUsers(query: "admin") {
        username
        email
        passwordHash
    }
}
```

---

### Vendors (Fabricantes)

#### Obtener todos los vendors
```graphql
query {
    vendors {
        id
        name
        description
        website
        active
    }
}
```

#### Obtener vendor por ID
```graphql
query {
    vendor(id: 1) {
        id
        name
        description
        website
    }
}
```

#### Buscar vendor por nombre
```graphql
query {
    vendorByName(name: "Microsoft") {
        id
        name
        description
        website
    }
}
```

---

### Products (Productos)

#### Obtener todos los productos
```graphql
query {
    products {
        id
        name
        version
        description
        vendor {
            id
            name
        }
    }
}
```

#### Obtener productos de un vendor específico
```graphql
query {
    productsByVendor(vendorId: 1) {
        id
        name
        version
        vendor {
            name
        }
    }
}
```

---

### CVEs (Vulnerabilidades)

#### Obtener todos los CVEs
```graphql
query {
    cves {
        id
        cveId
        title
        description
        severity
        cvssScore
        publishedDate
        affectedProducts {
            id
            name
            version
            vendor {
                name
            }
        }
    }
}
```

#### Obtener CVE por ID
```graphql
query {
    cve(id: 1) {
        id
        cveId
        title
        severity
        cvssScore
        affectedProducts {
            name
            vendor {
                name
            }
        }
    }
}
```

#### Buscar CVE por identificador CVE-YYYY-NNNNN
```graphql
query {
    cveByCveId(cveId: "CVE-2023-12345") {
        id
        cveId
        title
        description
        severity
        cvssScore
    }
}
```

#### Obtener CVEs por severidad
```graphql
query {
    cvesBySeverity(severity: CRITICAL) {
        id
        cveId
        title
        cvssScore
    }
}
```

#### Obtener solo CVEs críticos
```graphql
query {
    criticalCVEs {
        id
        cveId
        title
        cvssScore
        affectedProducts {
            name
        }
    }
}
```

#### Obtener CVEs recientes (últimos N días)
```graphql
query {
    recentCVEs(days: 30) {
        id
        cveId
        title
        severity
        publishedDate
    }
}
```

#### Buscar CVEs por rango de score
```graphql
query {
    cvesByScoreRange(minScore: 7.0, maxScore: 10.0) {
        id
        cveId
        title
        cvssScore
        severity
    }
}
```

#### Buscar CVEs (vulnerable a SQL Injection)
```graphql
query {
    searchCVEs(query: "Windows") {
        cveId
        title
        severity
    }
}
```

---

## MUTATIONS (Escritura)

### Usuarios

#### Crear nuevo usuario
```graphql
mutation {
    createUser(
        username: "testuser"
        email: "test@example.com"
        fullName: "Test User"
        password: "test123"
        role: ANALYST
    ) {
        id
        username
        email
        role
    }
}
```

Nota: En Fase 1 el password se guarda en texto plano (vulnerable).

#### Actualizar rol de usuario (vulnerable: escalación de privilegios)
```graphql
mutation {
    updateUserRole(userId: 3, newRole: ADMIN) {
        id
        username
        role
    }
}
```

Cualquier usuario puede ejecutar esta mutation y hacerse ADMIN.

#### Desactivar usuario
```graphql
mutation {
    deactivateUser(userId: 4) {
        id
        username
        active
    }
}
```

---

### Vendors

#### Crear nuevo vendor
```graphql
mutation {
    createVendor(
        name: "Red Hat"
        description: "Enterprise open source solutions"
        website: "https://www.redhat.com"
    ) {
        id
        name
    }
}
```

#### Actualizar vendor
```graphql
mutation {
    updateVendor(
        vendorId: 1
        description: "Updated description"
        website: "https://updated-url.com"
    ) {
        id
        name
        description
    }
}
```

---

### Products

#### Crear nuevo producto
```graphql
mutation {
    createProduct(
        vendorId: 1
        name: "Windows Server"
        version: "2022"
        description: "Server operating system"
    ) {
        id
        name
        version
        vendor {
            name
        }
    }
}
```

#### Actualizar producto
```graphql
mutation {
    updateProduct(
        productId: 1
        version: "23H2"
        description: "Updated version"
    ) {
        id
        name
        version
    }
}
```

---

### CVEs

#### Crear nuevo CVE
```graphql
mutation {
    createCVE(
        cveId: "CVE-2024-99999"
        title: "Test Vulnerability"
        description: "This is a test CVE for demonstration"
        severity: HIGH
        cvssScore: 8.5
        publishedDate: "2024-01-15"
    ) {
        id
        cveId
        title
        severity
    }
}
```

Si el cveId no sigue el formato CVE-YYYY-NNNNN, el Domain Primitive lanza error.

#### Actualizar descripción de CVE
```graphql
mutation {
    updateCVEDescription(
        cveId: 1
        description: "Updated detailed description of the vulnerability"
    ) {
        id
        cveId
        description
    }
}
```

#### Actualizar severidad de CVE
```graphql
mutation {
    updateCVESeverity(
        cveId: 1
        severity: CRITICAL
        cvssScore: 9.5
    ) {
        id
        cveId
        severity
        cvssScore
    }
}
```

#### Añadir producto afectado a un CVE
```graphql
mutation {
    addAffectedProduct(cveId: 1, productId: 3) {
        id
        cveId
        affectedProducts {
            name
        }
    }
}
```

---

## Vulnerabilidades Demostradas (Fase 1)

### Sin Autenticación

Cualquiera puede ejecutar todas las queries.
```graphql
query {
    users {
        username
        email
        passwordHash  # Passwords en texto plano visibles
    }
}
```

### Sin Autorización

Un usuario ANALYST puede convertirse en ADMIN.
```graphql
mutation {
    updateUserRole(userId: 3, newRole: ADMIN) {
        username
        role
    }
}
```

### Introspection Habilitada

Un atacante puede mapear toda la API.
```graphql
query {
    __schema {
        types {
            name
            fields {
                name
                type {
                    name
                }
            }
        }
    }
}
```

### Sin Query Depth Limiting

Consultas profundamente anidadas pueden causar DoS.
```graphql
query {
    cves {
        affectedProducts {
            vendor {
                products {
                    vendor {
                        # Anidación infinita posible
                    }
                }
            }
        }
    }
}
```

### Sin Paginación

Se pueden obtener todos los registros de una vez.
```graphql
query {
    users {
        id
        username
        # Sin límite, retorna todo
    }
}
```

---

## Mitigaciones Planificadas (Fase 2)

En Fase 2 se implementarán:

- JWT Authentication
- RBAC con @RolesAllowed
- BCrypt password hashing
- SQL Injection mitigation (queries parametrizadas)
- Query depth limiting
- Introspection disabled
- Cursor-based pagination

---

Autor: Bladimir Gonzales Miranda  
TFM: Seguridad en APIs GraphQL con Quarkus  
Universidad: UNIR - Máster en Ciberseguridad