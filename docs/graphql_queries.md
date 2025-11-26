# GraphQL Queries - VulnTrack API

Colección de queries y mutations de ejemplo para probar la API.

**FASE 1**: Todas las queries estaban SIN autenticación (vulnerable).  
**FASE 2**: Todas las queries requieren JWT token (excepto login).

---

## Autenticación

### Login (Obtener JWT Token)
```graphql
mutation {
  login(username: "admin", password: "admin123") {
    token
    userId
    username
    email
    role
  }
}
```

**Respuesta esperada:**
```json
{
  "data": {
    "login": {
      "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
      "userId": 1,
      "username": "admin",
      "email": "admin@vulntrack.io",
      "role": "ADMIN"
    }
  }
}
```

**Usuarios disponibles:**
- `admin` / `admin123` (ADMIN)
- `researcher` / `research123` (RESEARCHER)
- `analyst` / `analyst123` (ANALYST)

---

## Configurar Headers para Queries Autenticadas

Después de obtener el token, todas las queries deben incluir el header:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**En GraphQL UI (http://localhost:8080/q/graphql-ui/):**
1. Ir a pestaña "Headers"
2. Añadir: `Authorization`
3. Valor: `Bearer <tu_token_aqui>`

**En curl:**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Authorization: Bearer " \
  -H "Content-Type: application/json" \
  -d '{"query": "query { cves { cveId title } }"}'
```

**En Postman:**
1. Tab "Authorization"
2. Type: "Bearer Token"
3. Token: `<pegar_token>`

---

## QUERIES (Lectura)

### Usuarios

#### Obtener todos los usuarios
**RBAC: Solo ADMIN**
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
  }
}
```

#### Obtener usuario por ID
**RBAC: Solo ADMIN**
```graphql
query {
  user(id: 1) {
    id
    username
    email
    role
  }
}
```

#### Buscar usuario por username
**RBAC: Solo ADMIN**
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
**RBAC: Solo ADMIN**
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

#### Buscar usuarios (SQL Injection MITIGADO)
**RBAC: Solo ADMIN**
```graphql
query {
  searchUsers(query: "admin") {
    username
    email
  }
}
```

**Fase 1 vulnerable:** `query: "' OR '1'='1"` retornaba todos los usuarios.  
**Fase 2 seguro:** Búsqueda literal, no hay inyección SQL.

---

### Vendors (Fabricantes)

#### Obtener todos los vendors
**RBAC: ANALYST, RESEARCHER, ADMIN**
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
**RBAC: ANALYST, RESEARCHER, ADMIN**
```graphql
query {
  vendor(id: 1) {
    id
    name
    description
    website
    products {
      id
      name
      version
    }
  }
}
```

#### Buscar vendor por nombre
**RBAC: ANALYST, RESEARCHER, ADMIN**
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

#### Obtener solo vendors activos
**RBAC: ANALYST, RESEARCHER, ADMIN**
```graphql
query {
  activeVendors {
    id
    name
    website
  }
}
```

---

### Products (Productos)

#### Obtener todos los productos
**RBAC: ANALYST, RESEARCHER, ADMIN**
```graphql
query {
  products {
    id
    name
    version
    description
    active
    vendor {
      id
      name
    }
  }
}
```

#### Obtener producto por ID
**RBAC: ANALYST, RESEARCHER, ADMIN**
```graphql
query {
  product(id: 1) {
    id
    name
    version
    vendor {
      name
      website
    }
  }
}
```

#### Obtener productos de un vendor específico
**RBAC: ANALYST, RESEARCHER, ADMIN**
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

#### Obtener solo productos activos
**RBAC: ANALYST, RESEARCHER, ADMIN**
```graphql
query {
  activeProducts {
    id
    name
    version
    active
  }
}
```

---

### CVEs (Vulnerabilidades)

#### Obtener todos los CVEs
**RBAC: ANALYST, RESEARCHER, ADMIN**
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
**RBAC: ANALYST, RESEARCHER, ADMIN**
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
**RBAC: ANALYST, RESEARCHER, ADMIN**
```graphql
query {
  cveByCveId(cveId: "CVE-2023-12345") {
    id
    cveId
    title
    description
    severity
    cvssScore
    publishedDate
  }
}
```

#### Obtener CVEs por severidad
**RBAC: ANALYST, RESEARCHER, ADMIN**
```graphql
query {
  cvesBySeverity(severity: CRITICAL) {
    id
    cveId
    title
    cvssScore
    publishedDate
  }
}
```

#### Obtener solo CVEs críticos
**RBAC: ANALYST, RESEARCHER, ADMIN**
```graphql
query {
  criticalCVEs {
    id
    cveId
    title
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

#### Obtener CVEs recientes (últimos N días)
**RBAC: ANALYST, RESEARCHER, ADMIN**
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
**RBAC: ANALYST, RESEARCHER, ADMIN**
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

#### Buscar CVEs (SQL Injection MITIGADO)
**RBAC: ANALYST, RESEARCHER, ADMIN**
```graphql
query {
  searchCVEs(query: "Windows") {
    cveId
    title
    severity
  }
}
```

**Fase 1 vulnerable:** `query: "Windows' OR '1'='1"` retornaba todos los CVEs.  
**Fase 2 seguro:** Búsqueda parametrizada con Panache.

---

## MUTATIONS (Escritura)

### Usuarios

#### Crear nuevo usuario
**RBAC: Solo ADMIN**
```graphql
mutation {
  createUser(
    username: "newuser"
    email: "newuser@example.com"
    fullName: "New User"
    password: "securepass123"
    role: ANALYST
  ) {
    id
    username
    email
    role
  }
}
```

**Fase 2:** Password se hashea automáticamente con BCrypt antes de guardar.

#### Actualizar rol de usuario
**RBAC: Solo ADMIN**
```graphql
mutation {
  updateUserRole(userId: 3, newRole: RESEARCHER) {
    id
    username
    role
  }
}
```

**Fase 1 vulnerable:** Cualquiera podía hacerse ADMIN.  
**Fase 2 seguro:** Solo ADMIN puede cambiar roles.

#### Desactivar usuario
**RBAC: Solo ADMIN**
```graphql
mutation {
  deactivateUser(userId: 4) {
    id
    username
    active
  }
}
```

#### Activar usuario
**RBAC: Solo ADMIN**
```graphql
mutation {
  activateUser(userId: 4) {
    id
    username
    active
  }
}
```

---

### Vendors

#### Crear nuevo vendor
**RBAC: RESEARCHER, ADMIN**
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
**RBAC: RESEARCHER, ADMIN**
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

#### Eliminar vendor
**RBAC: Solo ADMIN**
```graphql
mutation {
  deleteVendor(vendorId: 5)
}
```

---

### Products

#### Crear nuevo producto
**RBAC: RESEARCHER, ADMIN**
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
**RBAC: RESEARCHER, ADMIN**
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

#### Eliminar producto
**RBAC: Solo ADMIN**
```graphql
mutation {
  deleteProduct(productId: 8)
}
```

---

### CVEs

#### Crear nuevo CVE
**RBAC: RESEARCHER, ADMIN**
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

**Domain Primitive:** Si `cveId` no sigue formato CVE-YYYY-NNNNN, lanza error.

#### Actualizar descripción de CVE
**RBAC: RESEARCHER, ADMIN**
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
**RBAC: RESEARCHER, ADMIN**
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
**RBAC: RESEARCHER, ADMIN**
```graphql
mutation {
  addAffectedProduct(cveId: 1, productId: 3) {
    id
    cveId
    affectedProducts {
      name
      vendor {
        name
      }
    }
  }
}
```

#### Remover producto afectado de un CVE
**RBAC: RESEARCHER, ADMIN**
```graphql
mutation {
  removeAffectedProduct(cveId: 1, productId: 3) {
    id
    cveId
    affectedProducts {
      name
    }
  }
}
```

---

## Casos de Uso por Rol

### Como ANALYST (Solo Lectura)
```graphql
# 1. Login
mutation {
  login(username: "analyst", password: "analyst123") {
    token
    role
  }
}

# 2. Consultar CVEs críticos (PERMITIDO)
query {
  criticalCVEs {
    cveId
    title
    cvssScore
  }
}

# 3. Intentar crear CVE (PROHIBIDO - Error 403)
mutation {
  createCVE(
    cveId: "CVE-2024-88888"
    title: "Test"
    severity: HIGH
    cvssScore: 7.0
    publishedDate: "2024-01-01"
  ) {
    id
  }
}
# Respuesta: Error 403 Forbidden
```

---

### Como RESEARCHER (Lectura + Escritura CVEs/Products/Vendors)
```graphql
# 1. Login
mutation {
  login(username: "researcher", password: "research123") {
    token
    role
  }
}

# 2. Consultar CVEs (PERMITIDO)
query {
  cves {
    cveId
    title
  }
}

# 3. Crear nuevo CVE (PERMITIDO)
mutation {
  createCVE(
    cveId: "CVE-2024-77777"
    title: "New Vulnerability"
    severity: MEDIUM
    cvssScore: 5.5
    publishedDate: "2024-11-20"
  ) {
    id
    cveId
  }
}

# 4. Intentar crear usuario (PROHIBIDO - Error 403)
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
# Respuesta: Error 403 Forbidden
```

---

### Como ADMIN (Control Total)
```graphql
# 1. Login
mutation {
  login(username: "admin", password: "admin123") {
    token
    role
  }
}

# 2. Ver todos los usuarios (PERMITIDO)
query {
  users {
    username
    role
  }
}

# 3. Crear nuevo usuario (PERMITIDO)
mutation {
  createUser(
    username: "newadmin"
    email: "newadmin@vulntrack.io"
    fullName: "New Admin"
    password: "admin456"
    role: ADMIN
  ) {
    id
    username
  }
}

# 4. Cambiar rol de usuario (PERMITIDO)
mutation {
  updateUserRole(userId: 3, newRole: RESEARCHER) {
    username
    role
  }
}

# 5. Eliminar vendor (PERMITIDO)
mutation {
  deleteVendor(vendorId: 5)
}
```

---

## Errores Comunes

### Error 401 Unauthorized
```json
{
  "errors": [
    {
      "message": "Unauthorized"
    }
  ]
}
```

**Causa:** Token JWT no proporcionado o inválido.  
**Solución:** Ejecutar mutation `login` y añadir header `Authorization: Bearer <token>`.

---

### Error 403 Forbidden
```json
{
  "errors": [
    {
      "message": "Forbidden"
    }
  ]
}
```

**Causa:** Usuario autenticado pero sin permisos para esta operación.  
**Solución:** Verificar matriz de permisos. Por ejemplo, ANALYST no puede crear CVEs.

---

### Error de Validación (Domain Primitive)
```json
{
  "errors": [
    {
      "message": "CVE ID debe seguir el formato CVE-YYYY-NNNNN (ej: CVE-2023-12345)",
      "path": ["createCVE"]
    }
  ]
}
```

**Causa:** Valor inválido detectado por Domain Primitive.  
**Solución:** Corregir el formato del input según las reglas de validación.

---

### Credenciales Inválidas
```json
{
  "errors": [
    {
      "message": "Credenciales inválidas",
      "path": ["login"]
    }
  ]
}
```

**Causa:** Username o password incorrectos.  
**Solución:** Verificar credenciales. Los passwords correctos son: admin123, research123, analyst123.

---

## Comparativa Fase 1 vs Fase 2

### Fase 1 (Baseline Vulnerable)

Todas las queries funcionaban sin autenticación:
```graphql
# Sin headers, sin token
query {
  users {
    username
    passwordHash  # Passwords en texto plano visibles
  }
}
```

Resultado: Retornaba todos los usuarios con passwords.

---

### Fase 2 (Security Hardening)

La misma query sin autenticación:
```graphql
query {
  users {
    username
  }
}
```

Resultado: Error 401 Unauthorized.

Con token JWT pero rol ANALYST:
```graphql
# Authorization: Bearer 
query {
  users {
    username
  }
}
```

Resultado: Error 403 Forbidden (solo ADMIN puede ver usuarios).

---

Autor: Bladimir Gonzales Miranda  
TFM: Seguridad en APIs GraphQL con Quarkus  
Universidad: UNIR - Máster en Ciberseguridad