# AuthController API Documentation

## 📋 Información General
- **Base Path:** `/api/auth`
- **Controlador:** `AuthController.java`
- **Autenticación:** La mayoría de endpoints son públicos excepto `/crear-administrador`

---

## 🔐 Endpoints
```http
POST /api/auth/login
Content-Type: application/json
```

**Descripción:** Autentica un usuario mediante cédula y contraseña.

**Request Body:**
```json
{
  "cedula": "1234567890",
  "contrasenia": "password123"
}
```

**Validaciones:**
- `cedula`: Obligatoria, exactamente 10 dígitos numéricos
- `contrasenia`: Obligatoria

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Inicio de sesión exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "rol": "ADM"
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Roles Posibles:**
- `SAD` - SuperAdministrador
- `ADM` - Administrador  
- `CLI` - Cliente

---

### 2. Registro

```http
POST /api/auth/registro
Content-Type: application/json
```

**Descripción:** Registra un nuevo usuario en el sistema (rol CLI por defecto).

**Request Body:**
```json
{
  "nombres": "Juan Carlos",
  "apellidos": "Pérez García",
  "telefono": "3001234567",
  "cedula": "1234567890",
  "direccion": "Calle 123 #45-67 Apto 101",
  "contrasenia": "password123"
}
```

**Validaciones:**
- `nombres`: Obligatorio, máx 100 caracteres, solo letras y espacios
- `apellidos`: Obligatorio, máx 100 caracteres, solo letras y espacios
- `telefono`: Obligatorio, exactamente 10 dígitos numéricos, único
- `cedula`: Obligatoria, exactamente 10 dígitos numéricos, único
- `direccion`: Obligatoria, máx 150 caracteres
- `contrasenia`: 6-50 caracteres

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Usuario registrado exitosamente",
  "data": {
    "idUsuario": 15,
    "nombres": "Juan Carlos",
    "apellidos": "Pérez García",
    "telefono": "3001234567",
    "cedula": "1234567890",
    "direccion": "Calle 123 #45-67 Apto 101",
    "estadoUsuario": "ACTIVO"
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Errores Comunes:**
- `400`: Validación fallida (formato incorrecto)
- `409`: Cédula o teléfono ya registrado

---

### 3. Cambiar Contraseña

```http
POST /api/auth/cambiar-contrasenia
Content-Type: application/json
```

**Descripción:** Permite cambiar la contraseña de un usuario autenticado.

**Request Body:**
```json
{
  "cedula": "1234567890",
  "contraseniaActual": "oldPassword123",
  "contraseniaNueva": "newPassword456"
}
```

**Validaciones:**
- `cedula`: Obligatoria, exactamente 10 dígitos numéricos
- `contraseniaActual`: Obligatoria
- `contraseniaNueva`: Obligatoria, mínimo 8 caracteres

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Contraseña cambiada exitosamente",
  "data": null,
  "timestamp": "2025-10-26T18:30:00"
}
```

**Errores Comunes:**
- `400`: Contraseña actual incorrecta
- `404`: Usuario no encontrado

---

### 4. Crear Administrador

```http
POST /api/auth/crear-administrador
Content-Type: application/json
Authorization: Bearer {token}
```

**Descripción:** Permite al SuperAdministrador (SAD) crear nuevos administradores.

**Seguridad:** 🔒 Requiere rol `SAD`

**Request Body:**
```json
{
  "nombres": "María José",
  "apellidos": "González López",
  "telefono": "3009876543",
  "cedula": "9876543210",
  "direccion": "Carrera 50 #30-20 Oficina 201",
  "contrasenia": "admin123"
}
```

**Validaciones:** Iguales al endpoint de registro

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Administrador creado exitosamente",
  "data": {
    "idUsuario": 16,
    "nombres": "María José",
    "apellidos": "González López",
    "telefono": "3009876543",
    "cedula": "9876543210",
    "direccion": "Carrera 50 #30-20 Oficina 201",
    "estadoUsuario": "ACTIVO"
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Errores Comunes:**
- `403`: Usuario autenticado no es SAD
- `409`: Cédula o teléfono ya registrado

---

## 📦 Modelos de Datos

### LoginRequest
```typescript
{
  cedula: string        // Exactamente 10 dígitos numéricos
  contrasenia: string   // Requerida
}
```

### RegistroRequest
```typescript
{
  nombres: string       // Máx 100 caracteres, solo letras y espacios
  apellidos: string     // Máx 100 caracteres, solo letras y espacios
  telefono: string      // Exactamente 10 dígitos numéricos, único
  cedula: string        // Exactamente 10 dígitos numéricos, único
  direccion: string     // Máx 150 caracteres
  contrasenia: string   // 6-50 caracteres
}
```

### JwtResponse
```typescript
{
  token: string         // JWT token
  rol: string          // "SAD" | "ADM" | "CLI"
}
```

### CambiarContraseniaRequest
```typescript
{
  cedula: string             // Exactamente 10 dígitos numéricos
  contraseniaActual: string  // Requerida
  contraseniaNueva: string   // Mínimo 8 caracteres
}
```

### Usuario (Response)
```typescript
{
  idUsuario: number
  nombres: string
  apellidos: string
  telefono: string
  cedula: string
  direccion: string
  estadoUsuario: string     // "ACTIVO" | "INACTIVO"
  // contrasenia NO se incluye en respuestas
}
```

---

## 🔒 Seguridad

### Autenticación
- **Login:** Devuelve JWT token válido por 24 horas (configurable)
- **Token:** Se envía en header `Authorization: Bearer {token}`
- **Contraseñas:** Hasheadas con BCrypt antes de almacenar

### Roles y Permisos
- `SAD` (SuperAdministrador): Acceso total, puede crear administradores
- `ADM` (Administrador): Gestión de pedidos y productos
- `CLI` (Cliente): Crear solicitudes, ver sus pedidos

---

## ⚠️ Códigos de Error

| Código | Descripción |
|--------|-------------|
| 400 | Validación fallida, datos incorrectos |
| 401 | Credenciales inválidas |
| 403 | Sin permisos para la operación |
| 404 | Usuario no encontrado |
| 409 | Cédula o teléfono ya registrado |
| 500 | Error interno del servidor |

---

## 📝 Notas Importantes

1. **Cédula como Username:** El sistema usa la cédula en lugar de correo electrónico
2. **Formato de Teléfono:** 10 dígitos sin prefijo internacional (ej: `3001234567`)
3. **Nombres con Tildes:** Soporta caracteres especiales españoles (á, é, í, ó, ú, ñ)
4. **Estado de Usuario:** Por defecto `ACTIVO` al registrarse
5. **Unicidad:** Tanto cédula como teléfono deben ser únicos en el sistema

---

## 🧪 Ejemplos con cURL

### Login
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "cedula": "1234567890",
    "contrasenia": "password123"
  }'
```

### Registro
```bash
curl -X POST "http://localhost:8080/api/auth/registro" \
  -H "Content-Type: application/json" \
  -d '{
    "nombres": "Juan Carlos",
    "apellidos": "Pérez García",
    "telefono": "3001234567",
    "cedula": "1234567890",
    "direccion": "Calle 123 #45-67",
    "contrasenia": "password123"
  }'
```

### Cambiar Contraseña
```bash
curl -X POST "http://localhost:8080/api/auth/cambiar-contrasenia" \
  -H "Content-Type: application/json" \
  -d '{
    "cedula": "1234567890",
    "contraseniaActual": "oldPassword123",
    "contraseniaNueva": "newPassword456"
  }'
```

### Crear Administrador (requiere token SAD)
```bash
curl -X POST "http://localhost:8080/api/auth/crear-administrador" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_SAD_TOKEN" \
  -d '{
    "nombres": "María José",
    "apellidos": "González López",
    "telefono": "3009876543",
    "cedula": "9876543210",
    "direccion": "Carrera 50 #30-20",
    "contrasenia": "admin123"
  }'
```
