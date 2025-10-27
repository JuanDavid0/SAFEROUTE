# API Documentation - Usuario Controller

## Información General

**Base URL:** `/usuarios`

**Descripción:** Controlador para gestión de usuarios del sistema (RF003). Permite consultar y eliminar usuarios. Solo accesible por usuarios con roles administrativos (SAD/ADM).

**Versión:** 1.0

**Requisitos Funcionales:** RF003 - Gestión de usuarios

---

## Endpoints

### 1. Listar Todos los Usuarios

Obtiene una lista completa de todos los usuarios registrados en el sistema.

**Endpoint:**
```
GET /usuarios
```

**Autenticación:** Requerida (JWT)

**Roles Permitidos:** `SAD` (Super Administrador)

**Request Headers:**
```http
Authorization: Bearer <token_jwt>
```

**Request Body:** No requiere

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 15 usuario(s)",
  "datos": [
    {
      "idUsuario": 1,
      "nombres": "Juan Carlos",
      "apellidos": "Pérez González",
      "telefono": "0987654321",
      "cedula": "1234567890",
      "direccion": "Av. Principal 123, Quito",
      "roles": ["CLI"]
    },
    {
      "idUsuario": 2,
      "nombres": "María Fernanda",
      "apellidos": "López Martínez",
      "telefono": "0998765432",
      "cedula": "0987654321",
      "direccion": "Calle Secundaria 456, Quito",
      "roles": ["ADM", "CLI"]
    },
    {
      "idUsuario": 3,
      "nombres": "Pedro Antonio",
      "apellidos": "Rodríguez Silva",
      "telefono": "0976543210",
      "cedula": "1122334455",
      "direccion": "Barrio Norte 789, Quito",
      "roles": ["SAD", "ADM", "CLI"]
    }
  ],
  "timestamp": "2025-10-26T14:30:00"
}
```

**Códigos de Estado:**
- `200 OK` - Lista obtenida exitosamente
- `401 Unauthorized` - Token no válido o expirado
- `403 Forbidden` - Usuario no tiene rol SAD
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/usuarios" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---
### Respuestas de error y fallos comunes

**Response 400 Bad Request:**
```json
{
  "status": "error",
  "message": "Error de validación",
  "data": {
    "exito": false,
    "mensaje": "Parámetros de consulta inválidos",
    "errores": [
      "page: debe ser >= 0",
      "size: debe ser entre 1 y 100",
      "sort: campo de ordenamiento inválido",
      "roles: rol no reconocido"
    ],
    "datos": {
      "camposOrdenamiento": ["nombres", "apellidos", "cedula"],
      "rolesValidos": ["SAD", "ADM", "CLI"],
      "ejemploValido": {
        "page": 0,
        "size": 20,
        "sort": "apellidos,asc"
      }
    }
  },
  "timestamp": "2025-10-26T14:30:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "status": "error",
  "message": "No autenticado",
  "data": {
    "exito": false,
    "mensaje": "Token inválido o expirado",
    "datos": {
      "causa": "Token expirado",
      "tiempoExpiracion": "2025-10-26T14:00:00",
      "sugerencia": "Renueve su sesión"
    }
  },
  "timestamp": "2025-10-26T14:30:00"
}
```

**Response 403 Forbidden:**
```json
{
  "status": "error",
  "message": "Acceso denegado",
  "data": {
    "exito": false,
    "mensaje": "No tiene permisos para esta operación",
    "datos": {
      "rolesPermitidos": ["SAD"],
      "rolUsuario": "ADM",
      "operacion": "listar_usuarios",
      "sugerencia": "Contacte al super administrador"
    }
  },
  "timestamp": "2025-10-26T14:30:00"
}
```

**Response 404 Not Found:**
```json
{
  "status": "error",
  "message": "Usuario no encontrado",
  "data": {
    "exito": false,
    "mensaje": "No existe un usuario con el ID especificado",
    "datos": {
      "idUsuario": 999,
      "sugerencia": "Verifique el ID del usuario",
      "posiblesCausas": [
        "Usuario eliminado",
        "ID incorrecto",
        "Usuario desactivado"
      ]
    }
  },
  "timestamp": "2025-10-26T14:30:00"
}
```

**Response 409 Conflict:**
```json
{
  "status": "error",
  "message": "Conflicto de datos",
  "data": {
    "exito": false,
    "mensaje": "No se puede eliminar el usuario",
    "datos": {
      "idUsuario": 123,
      "causas": [
        "Usuario tiene solicitudes activas",
        "Usuario es administrador de pedidos",
        "Existen registros asociados"
      ],
      "sugerencia": "Resuelva las dependencias primero"
    }
  },
  "timestamp": "2025-10-26T14:30:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "status": "error",
  "message": "Error de validación de negocio",
  "data": {
    "exito": false,
    "mensaje": "No se puede procesar la operación",
    "datos": {
      "validaciones": [
        "No se puede eliminar el último super administrador",
        "El usuario tiene roles incompatibles",
        "La cédula no es válida para Ecuador"
      ]
    }
  },
  "timestamp": "2025-10-26T14:30:00"
}
```

**Response 429 Too Many Requests:**
```json
{
  "status": "error",
  "message": "Demasiadas solicitudes",
  "data": {
    "exito": false,
    "mensaje": "Ha excedido el límite de consultas",
    "datos": {
      "limiteMinuto": 60,
      "consultasRealizadas": 61,
      "tiempoEspera": 30,
      "sugerencia": "Use paginación y caché"
    }
  },
  "timestamp": "2025-10-26T14:30:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "status": "error",
  "message": "Error interno del servidor",
  "data": {
    "exito": false,
    "mensaje": "Error al procesar la operación",
    "datos": {
      "tipo": "DatabaseException",
      "error": "Error de conexión a la base de datos",
      "referencia": "USR-2025102614300001",
      "sugerencia": "Contacte al administrador"
    }
  },
  "timestamp": "2025-10-26T14:30:00"
}
```

### Advertencias y consideraciones

- **Límites y validaciones:**
  - Máximo 60 consultas por minuto
  - Paginación: 100 usuarios por página
  - Cédula: validación algoritmo Ecuador
  - Teléfono: formato Ecuador (+593)

- **Seguridad:**
  - No exponer datos sensibles
  - Encriptar contraseñas (BCrypt)
  - Registrar intentos de acceso
  - Bloqueo por intentos fallidos

- **Roles y permisos:**
  - SAD: acceso total
  - ADM: consulta limitada
  - CLI: sin acceso a API
  - Validar conflictos de rol

- **Auditoría:**
  - Registro de cambios
  - Historial de roles
  - Trazabilidad de operaciones
  - Retención de logs: 1 año

- **Performance:**
  - Caché de usuarios frecuentes
  - Índices en campos de búsqueda
  - Compresión de respuestas
  - Query optimization
```

**Notas Importantes:**
- Solo usuarios con rol `SAD` pueden listar todos los usuarios
- El mensaje indica el número total de usuarios encontrados
- Los usuarios pueden tener múltiples roles simultáneamente
- Los roles posibles son: `CLI` (Cliente), `ADM` (Administrador), `SAD` (Super Administrador)

---

### 2. Obtener Usuario por ID

Obtiene la información detallada de un usuario específico mediante su identificador.

**Endpoint:**
```
GET /usuarios/{id}
```

**Autenticación:** Requerida (JWT)

**Roles Permitidos:** `SAD`, `ADM`

**Path Parameters:**
- `id` (Integer, requerido) - ID del usuario a consultar

**Request Headers:**
```http
Authorization: Bearer <token_jwt>
```

**Request Body:** No requiere

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Usuario encontrado",
  "datos": {
    "idUsuario": 1,
    "nombres": "Juan Carlos",
    "apellidos": "Pérez González",
    "telefono": "0987654321",
    "cedula": "1234567890",
    "direccion": "Av. Principal 123, Quito",
    "roles": ["CLI"]
  },
  "timestamp": "2025-10-26T14:30:00"
}
```

**Response (404 Not Found):**
```json
{
  "exito": false,
  "mensaje": "Usuario no encontrado con ID: 999",
  "datos": null,
  "timestamp": "2025-10-26T14:30:00"
}
```

**Códigos de Estado:**
- `200 OK` - Usuario encontrado exitosamente
- `401 Unauthorized` - Token no válido o expirado
- `403 Forbidden` - Usuario no tiene rol SAD o ADM
- `404 Not Found` - Usuario no existe con ese ID
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/usuarios/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Notas Importantes:**
- Tanto `SAD` como `ADM` pueden consultar información de usuarios
- No se devuelve información sensible como contraseñas
- El campo `roles` es un array porque un usuario puede tener múltiples roles
- El teléfono y cédula son únicos en el sistema

---

### 3. Eliminar Usuario

Elimina un usuario del sistema de forma permanente.

**Endpoint:**
```
DELETE /usuarios/{id}
```

**Autenticación:** Requerida (JWT)

**Roles Permitidos:** `SAD` (Super Administrador)

**Path Parameters:**
- `id` (Integer, requerido) - ID del usuario a eliminar

**Request Headers:**
```http
Authorization: Bearer <token_jwt>
```

**Request Body:** No requiere

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Usuario eliminado exitosamente",
  "datos": null,
  "timestamp": "2025-10-26T14:30:00"
}
```

**Response (404 Not Found):**
```json
{
  "exito": false,
  "mensaje": "Usuario no encontrado con ID: 999",
  "datos": null,
  "timestamp": "2025-10-26T14:30:00"
}
```

**Códigos de Estado:**
- `200 OK` - Usuario eliminado exitosamente
- `401 Unauthorized` - Token no válido o expirado
- `403 Forbidden` - Usuario no tiene rol SAD
- `404 Not Found` - Usuario no existe con ese ID
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X DELETE "http://localhost:8080/usuarios/5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Notas Importantes:**
- **Solo usuarios con rol `SAD` pueden eliminar usuarios**
- Esta operación es **irreversible** - el usuario se elimina permanentemente de la base de datos
- No se puede eliminar el propio usuario (validación del sistema)
- Al eliminar un usuario, también se eliminan sus datos relacionados (solicitudes, pedidos asociados como cliente)
- Recomendación: Considerar implementar eliminación lógica en lugar de física para mantener histórico

**⚠️ PRECAUCIÓN:**
Esta operación elimina permanentemente todos los datos del usuario. Úsala con extremo cuidado.

---

## Modelos de Datos

### UsuarioDTO

Modelo de transferencia de datos para representar un usuario del sistema.

**TypeScript Interface:**
```typescript
interface UsuarioDTO {
  idUsuario: number;           // ID único del usuario (generado automáticamente)
  nombres: string;             // Nombres del usuario (máx 100 caracteres, solo letras y espacios)
  apellidos: string;           // Apellidos del usuario (máx 100 caracteres, solo letras y espacios)
  telefono: string;            // Teléfono (10 dígitos numéricos, único en el sistema)
  cedula: string;              // Cédula de identidad (10 dígitos numéricos, único, username del sistema)
  direccion: string;           // Dirección física del usuario
  roles: string[];             // Lista de roles asignados (CLI, ADM, SAD)
}
```

**Ejemplo Completo:**
```json
{
  "idUsuario": 15,
  "nombres": "Ana María",
  "apellidos": "Torres Vargas",
  "telefono": "0987654321",
  "cedula": "1750234567",
  "direccion": "Conjunto Los Álamos, Casa 25, Cumbayá",
  "roles": ["ADM", "CLI"]
}
```

**Validaciones:**
- `nombres`: Solo letras y espacios, máximo 100 caracteres
- `apellidos`: Solo letras y espacios, máximo 100 caracteres
- `telefono`: Exactamente 10 dígitos numéricos (formato: 09XXXXXXXX)
- `cedula`: Exactamente 10 dígitos numéricos (validación de cédula ecuatoriana)
- `direccion`: Texto libre, requerido
- `roles`: Array de strings con valores válidos: `CLI`, `ADM`, `SAD`

**Roles del Sistema:**
- `CLI` (Cliente) - Usuario estándar que puede crear solicitudes
- `ADM` (Administrador) - Usuario que gestiona pedidos y productos
- `SAD` (Super Administrador) - Máximos privilegios, gestión completa del sistema

---

## Seguridad y Autenticación

### JWT Token
Todos los endpoints requieren autenticación mediante JWT Bearer Token.

**Formato del Header:**
```http
Authorization: Bearer <token_jwt>
```

### Roles y Permisos

| Endpoint | Método | Roles Permitidos | Descripción |
|----------|--------|------------------|-------------|
| `/usuarios` | GET | SAD | Solo Super Administradores pueden listar todos los usuarios |
| `/usuarios/{id}` | GET | SAD, ADM | Super Administradores y Administradores pueden consultar usuarios |
| `/usuarios/{id}` | DELETE | SAD | Solo Super Administradores pueden eliminar usuarios |

**Jerarquía de Roles:**
```
SAD (Super Administrador)
 ├─ Puede listar todos los usuarios
 ├─ Puede consultar cualquier usuario
 ├─ Puede eliminar usuarios
 └─ Tiene todos los permisos del sistema

ADM (Administrador)
 ├─ Puede consultar usuarios
 └─ Gestiona pedidos y productos

CLI (Cliente)
 └─ Acceso limitado a sus propias solicitudes
```

---

## Manejo de Errores

### Estructura de Error Estándar

```json
{
  "exito": false,
  "mensaje": "Descripción del error",
  "datos": null,
  "timestamp": "2025-10-26T14:30:00"
}
```

### Errores Comunes

#### 401 Unauthorized
```json
{
  "exito": false,
  "mensaje": "Token no válido o expirado",
  "datos": null,
  "timestamp": "2025-10-26T14:30:00"
}
```

**Causas:**
- Token JWT expirado
- Token malformado o inválido
- No se proporcionó el header Authorization

#### 403 Forbidden
```json
{
  "exito": false,
  "mensaje": "Acceso denegado: se requiere rol SAD",
  "datos": null,
  "timestamp": "2025-10-26T14:30:00"
}
```

**Causas:**
- Usuario autenticado pero sin el rol requerido
- Intentar acceder a recursos de nivel superior al permitido

#### 404 Not Found
```json
{
  "exito": false,
  "mensaje": "Usuario no encontrado con ID: 999",
  "datos": null,
  "timestamp": "2025-10-26T14:30:00"
}
```

**Causas:**
- ID de usuario no existe en la base de datos
- Usuario fue eliminado previamente

#### 500 Internal Server Error
```json
{
  "exito": false,
  "mensaje": "Error interno del servidor: [detalle técnico]",
  "datos": null,
  "timestamp": "2025-10-26T14:30:00"
}
```

**Causas:**
- Error de conexión a base de datos
- Excepción no manejada en el servidor

---

## Casos de Uso

### Caso 1: Super Administrador consulta todos los usuarios

**Escenario:** Un Super Administrador necesita ver la lista completa de usuarios del sistema para auditoría.

**Flujo:**
1. SAD hace login → obtiene JWT token
2. Realiza petición GET `/usuarios` con token
3. Sistema devuelve lista completa de usuarios con sus roles
4. SAD puede identificar usuarios con múltiples roles

**Request:**
```bash
curl -X GET "http://localhost:8080/usuarios" \
  -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGc..."
```

**Response:**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 15 usuario(s)",
  "datos": [...]
}
```

---

### Caso 2: Administrador consulta información de un cliente específico

**Escenario:** Un ADM necesita verificar los datos de un cliente que reportó un problema con su pedido.

**Flujo:**
1. ADM obtiene el ID del usuario desde el pedido
2. Consulta información completa del usuario
3. Verifica teléfono y dirección para contacto

**Request:**
```bash
curl -X GET "http://localhost:8080/usuarios/25" \
  -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGc..."
```

**Response:**
```json
{
  "exito": true,
  "mensaje": "Usuario encontrado",
  "datos": {
    "idUsuario": 25,
    "nombres": "Laura Patricia",
    "apellidos": "Méndez Castro",
    "telefono": "0991234567",
    "cedula": "1723456789",
    "direccion": "Urbanización El Bosque, Manzana F, Casa 12",
    "roles": ["CLI"]
  }
}
```

---

### Caso 3: Super Administrador elimina usuario duplicado

**Escenario:** Se detectó un usuario duplicado creado por error y debe ser eliminado.

**Flujo:**
1. SAD lista todos los usuarios
2. Identifica el ID del usuario duplicado
3. Verifica que no sea el usuario activo
4. Elimina el usuario duplicado

**⚠️ IMPORTANTE:** Verificar antes de eliminar que:
- No hay pedidos activos asociados al usuario
- No hay solicitudes pendientes
- No es el propio usuario conectado

**Request:**
```bash
curl -X DELETE "http://localhost:8080/usuarios/47" \
  -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGc..."
```

**Response:**
```json
{
  "exito": true,
  "mensaje": "Usuario eliminado exitosamente",
  "datos": null
}
```

---

## Integración Frontend

### Ejemplo TypeScript/React - Listar Usuarios

```typescript
import axios from 'axios';

interface Usuario {
  idUsuario: number;
  nombres: string;
  apellidos: string;
  telefono: string;
  cedula: string;
  direccion: string;
  roles: string[];
}

interface ApiResponse<T> {
  exito: boolean;
  mensaje: string;
  datos: T;
  timestamp: string;
}

// Listar todos los usuarios (solo SAD)
async function listarUsuarios(token: string): Promise<Usuario[]> {
  try {
    const response = await axios.get<ApiResponse<Usuario[]>>(
      'http://localhost:8080/usuarios',
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );

    if (response.data.exito) {
      console.log(response.data.mensaje); // "Se encontraron X usuario(s)"
      return response.data.datos;
    } else {
      throw new Error(response.data.mensaje);
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 403) {
        throw new Error('Acceso denegado: se requiere rol SAD');
      } else if (error.response?.status === 401) {
        throw new Error('Token no válido o expirado');
      }
    }
    throw error;
  }
}

// Obtener usuario por ID (SAD o ADM)
async function obtenerUsuario(id: number, token: string): Promise<Usuario> {
  try {
    const response = await axios.get<ApiResponse<Usuario>>(
      `http://localhost:8080/usuarios/${id}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );

    if (response.data.exito) {
      return response.data.datos;
    } else {
      throw new Error(response.data.mensaje);
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 404) {
        throw new Error(`Usuario no encontrado con ID: ${id}`);
      } else if (error.response?.status === 403) {
        throw new Error('Acceso denegado: se requiere rol SAD o ADM');
      }
    }
    throw error;
  }
}

// Eliminar usuario (solo SAD)
async function eliminarUsuario(id: number, token: string): Promise<void> {
  // Confirmación obligatoria
  const confirmar = window.confirm(
    '⚠️ ¿Estás seguro de eliminar este usuario? Esta acción es irreversible.'
  );
  
  if (!confirmar) {
    throw new Error('Operación cancelada por el usuario');
  }

  try {
    const response = await axios.delete<ApiResponse<null>>(
      `http://localhost:8080/usuarios/${id}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );

    if (response.data.exito) {
      console.log(response.data.mensaje); // "Usuario eliminado exitosamente"
    } else {
      throw new Error(response.data.mensaje);
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 404) {
        throw new Error(`Usuario no encontrado con ID: ${id}`);
      } else if (error.response?.status === 403) {
        throw new Error('Acceso denegado: se requiere rol SAD');
      }
    }
    throw error;
  }
}

// Ejemplo de uso en componente React
function UsuariosPanel() {
  const [usuarios, setUsuarios] = React.useState<Usuario[]>([]);
  const token = localStorage.getItem('jwt_token') || '';

  React.useEffect(() => {
    listarUsuarios(token)
      .then(setUsuarios)
      .catch(error => alert(error.message));
  }, [token]);

  const handleEliminar = async (id: number) => {
    try {
      await eliminarUsuario(id, token);
      // Recargar lista después de eliminar
      const nuevaLista = await listarUsuarios(token);
      setUsuarios(nuevaLista);
      alert('Usuario eliminado correctamente');
    } catch (error: any) {
      alert(error.message);
    }
  };

  return (
    <div>
      <h2>Usuarios del Sistema</h2>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Nombres</th>
            <th>Apellidos</th>
            <th>Cédula</th>
            <th>Teléfono</th>
            <th>Roles</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {usuarios.map(usuario => (
            <tr key={usuario.idUsuario}>
              <td>{usuario.idUsuario}</td>
              <td>{usuario.nombres}</td>
              <td>{usuario.apellidos}</td>
              <td>{usuario.cedula}</td>
              <td>{usuario.telefono}</td>
              <td>{usuario.roles.join(', ')}</td>
              <td>
                <button onClick={() => handleEliminar(usuario.idUsuario)}>
                  Eliminar
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

---

## Notas Técnicas Adicionales

### 1. Sistema de Roles Múltiples
- Un usuario puede tener múltiples roles simultáneamente
- El campo `roles` es un array: `["CLI", "ADM"]`
- Por defecto, usuarios nuevos obtienen rol `CLI`
- Solo `SAD` puede asignar roles `ADM` y `SAD` (vía endpoint de AuthController)

### 2. Identificadores Únicos
- **Cédula**: Campo único que actúa como username del sistema
- **Teléfono**: Campo único para contacto y validación OTP
- **ID Usuario**: Autoincremental, clave primaria en base de datos

### 3. Eliminación de Datos
- La eliminación actual es **física** (borra registro de BD)
- Recomendación de mejora: Implementar eliminación lógica con campo `estadoUsuario`
- Alternativa: Cambiar estado a "INACTIVO" en lugar de eliminar

### 4. Diferencias con AuthController
- **AuthController**: Creación de usuarios (registro, crear admin)
- **UsuarioController**: Consulta y eliminación de usuarios existentes
- **No hay endpoint de actualización** en UsuarioController (posible mejora futura)

### 5. Rendimiento
- El endpoint `/usuarios` devuelve todos los usuarios sin paginación
- Para sistemas con miles de usuarios, considerar implementar:
  - Paginación (`page`, `size`, `sort`)
  - Filtros (`rol`, `nombre`, `cedula`)
  - Búsqueda por criterios

---

## Mejoras Sugeridas

### 1. Paginación
```http
GET /usuarios?page=0&size=20&sort=nombres,asc
```

### 2. Filtros
```http
GET /usuarios?rol=ADM
GET /usuarios?cedula=1234567890
GET /usuarios?nombres=Juan
```

### 3. Actualización de Usuarios
```http
PUT /usuarios/{id}
Content-Type: application/json

{
  "nombres": "Juan Carlos",
  "apellidos": "Pérez González",
  "telefono": "0987654321",
  "direccion": "Nueva dirección actualizada"
}
```

### 4. Eliminación Lógica
```http
PATCH /usuarios/{id}/desactivar
```

### 5. Búsqueda Avanzada
```http
POST /usuarios/buscar
Content-Type: application/json

{
  "criterios": {
    "nombreCompleto": "Juan",
    "roles": ["ADM"],
    "estadoUsuario": "ACTIVO"
  },
  "pagina": 0,
  "tamanio": 20
}
```

---

## Changelog

**Versión 1.0** (2025-10-26)
- Versión inicial de UsuarioController
- 3 endpoints implementados (GET lista, GET por ID, DELETE)
- Control de acceso basado en roles (SAD, ADM)
- Modelo UsuarioDTO con 7 campos
- Sistema de roles múltiples por usuario

---

## Contacto y Soporte

Para preguntas técnicas o reporte de bugs relacionados con la gestión de usuarios, contactar al equipo de desarrollo backend.

**Documentación relacionada:**
- [API_AuthController.md](./API_AuthController.md) - Autenticación y registro de usuarios
- [API_OtpController.md](./API_OtpController.md) - Autenticación temporal vía SMS

---

**Última actualización:** 26 de octubre de 2025
