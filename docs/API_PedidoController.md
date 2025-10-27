# PedidoController API Documentation

## 📋 Información General
- **Base Path:** `/api/pedidos`
- **Controlador:** `PedidoController.java`
- **Seguridad:** 🔒 Todos los endpoints requieren rol `SAD` o `ADM` excepto `/pedido-disponible/{hash}`

---

### Respuestas de error y fallos comunes (aplican a múltiples endpoints)

**Response 400 Bad Request:**
```json
{
  "status": "error",
  "message": "Parámetros inválidos o datos de entrada incompletos",
  "data": {
    "exito": false,
    "errores": [
      "fechaCierre: debe ser una fecha futura",
      "fechaCierre: debe tener formato YYYY-MM-DD",
      "productos[0].cantidadMin: debe ser >= 0",
      "productos[0].cantidadMax: debe ser mayor que cantidadMin",
      "productos: debe contener al menos un producto"
    ],
    "datos": {
      "formatoFecha": "YYYY-MM-DD",
      "ejemploValido": "2025-12-31"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "status": "error",
  "message": "Token de autenticación inválido o expirado",
  "data": {
    "exito": false,
    "mensaje": "Sesión expirada o token inválido",
    "datos": {
      "causa": "Token expirado",
      "tiempoExpiracion": "2025-10-26T18:00:00",
      "sugerencia": "Inicie sesión nuevamente"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 403 Forbidden:**
```json
{
  "status": "error",
  "message": "No tiene permisos para realizar esta operación",
  "data": {
    "exito": false,
    "mensaje": "Rol insuficiente para esta operación",
    "datos": {
      "rolesPermitidos": ["SAD", "ADM"],
      "rolUsuario": "CLI",
      "operacion": "crear_pedido"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 404 Not Found:**
```json
{
  "status": "error",
  "message": "Recurso no encontrado",
  "data": {
    "exito": false,
    "mensaje": "El pedido o recurso solicitado no existe",
    "datos": {
      "idPedido": 123,
      "tipo": "Pedido",
      "sugerencia": "Verifique el ID del pedido"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 409 Conflict:**
```json
{
  "status": "error",
  "message": "Conflicto de estado",
  "data": {
    "exito": false,
    "mensaje": "No se puede modificar el pedido en su estado actual",
    "datos": {
      "idPedido": 123,
      "estadoActual": "CSL",
      "estadosPermitidos": ["AGP"],
      "operacion": "modificar",
      "sugerencia": "Solo se pueden modificar pedidos en estado AGP"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "status": "error",
  "message": "Error de validación de negocio",
  "data": {
    "exito": false,
    "mensaje": "No se puede procesar el pedido",
    "datos": {
      "idPedido": 123,
      "validaciones": [
        "La fecha de cierre debe ser al menos 7 días después de la fecha actual",
        "No se pueden incluir productos descontinuados",
        "Las cantidades máximas deben ser múltiplos de 5"
      ]
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 429 Too Many Requests:**
```json
{
  "status": "error",
  "message": "Demasiadas solicitudes",
  "data": {
    "exito": false,
    "mensaje": "Ha excedido el límite de solicitudes",
    "datos": {
      "limiteHora": 100,
      "solicitudesRealizadas": 101,
      "tiempoEspera": 360,
      "reintentar": "2025-10-26T19:30:00"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "status": "error",
  "message": "Error interno del servidor",
  "data": {
    "exito": false,
    "mensaje": "Error inesperado al procesar la solicitud",
    "datos": {
      "tipo": "DatabaseException",
      "error": "Error de conexión a la base de datos",
      "referencia": "ERR-2025102618300001",
      "sugerencia": "Contacte al administrador del sistema"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 503 Service Unavailable:**
```json
{
  "status": "error",
  "message": "Servicio no disponible temporalmente",
  "data": {
    "exito": false,
    "mensaje": "El sistema está en mantenimiento",
    "datos": {
      "estado": "Mantenimiento programado",
      "inicio": "2025-10-26T18:00:00",
      "fin": "2025-10-26T20:00:00",
      "sugerencia": "Reintente después de las 20:00"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

### Advertencias y consideraciones

- **Límites y validaciones:**
  - Máximo 100 solicitudes por hora por usuario
  - Fechas de cierre entre 7 y 30 días en el futuro
  - Cantidades máximas múltiplos de 5
  - No permitir productos descontinuados

- **Estados de pedido:**
  - AGP: Agrupamiento (modificable)
  - CSL: Consolidado (no modificable)
  - FNL: Finalizado
  - CRM: Cerrado manualmente
  - CNL: Cancelado

- **Validaciones de negocio:**
  - No permitir fechas de fin de semana
  - Validar stock disponible
  - Verificar límites de cantidades
  - Comprobar permisos por zona

- **Performance:**
  - Caché de productos frecuentes
  - Optimizar consultas grandes
  - Paginación en listados
  - Comprimir respuestas

- **Seguridad:**
  - Validar permisos por rol
  - Registrar acciones sensibles
  - Encriptar datos sensibles
  - Prevenir SQL injection

## 🔐 Endpoints

### 1. Crear Pedido

```http
POST /api/pedidos/{idAdmin}
Content-Type: application/json
Authorization: Bearer {token}
```

**Descripción:** Crea un nuevo pedido asociado a un administrador.

**Path Parameters:**
- `idAdmin` (integer): ID del administrador que crea el pedido

**Request Body:**
```json
{
  "fechaCierre": "2025-11-15",
  "productos": [
    {
      "idProducto": 45,
      "cantidadMin": 10,
      "cantidadMax": 100
    },
    {
      "idProducto": 46,
      "cantidadMin": 5,
      "cantidadMax": 50
    }
  ]
}
```

**Campos:**
- `fechaCierre` (date): Fecha límite para recibir solicitudes (formato: YYYY-MM-DD)
- `productos` (array): Lista de productos del pedido
  - `idProducto` (integer): ID del producto
  - `cantidadMin` (integer): Cantidad mínima que se puede solicitar
  - `cantidadMax` (integer): Cantidad máxima que se puede solicitar

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Pedido creado exitosamente",
  "data": {
    "idPedido": 123,
    "idAdmin": 5,
    "estadoPedido": "AGP",
    "fechaCreado": "2025-10-26",
    "fechaCierre": "2025-11-15",
    "urlHash": "ABC123XYZ456RANDOM",
    "productos": [
      {
        "idProducto": 45,
        "cantidadMin": 10,
        "cantidadMax": 100
      },
      {
        "idProducto": 46,
        "cantidadMin": 5,
        "cantidadMax": 50
      }
    ]
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Estados de Pedido:**
- `AGP` - Aguardando Solicitudes (estado inicial)
- `CSL` - Consolidado
- `CRM` - Cancelado/Removido
- `FNL` - Finalizado

---

### 2. Listar Todos los Pedidos

```http
GET /api/pedidos
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Se encontraron 15 pedido(s)",
  "data": [
    {
      "idPedido": 123,
      "idAdmin": 5,
      "estadoPedido": "AGP",
      "fechaCreado": "2025-10-26",
      "fechaCierre": "2025-11-15",
      "urlHash": "ABC123XYZ456RANDOM",
      "productos": [...]
    }
  ],
  "timestamp": "2025-10-26T18:30:00"
}
```

---

### 3. Obtener Pedido por ID

```http
GET /api/pedidos/{id}
Authorization: Bearer {token}
```

**Path Parameters:**
- `id` (integer): ID del pedido

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Pedido encontrado",
  "data": {
    "idPedido": 123,
    "idAdmin": 5,
    "estadoPedido": "AGP",
    "fechaCreado": "2025-10-26",
    "fechaCierre": "2025-11-15",
    "urlHash": "ABC123XYZ456RANDOM",
    "productos": [
      {
        "idProducto": 45,
        "cantidadMin": 10,
        "cantidadMax": 100
      }
    ]
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Errores:**
- `404`: Pedido no encontrado

---

### 4. Actualizar Pedido

```http
PUT /api/pedidos/{idPedido}
Content-Type: application/json
Authorization: Bearer {token}
```

**Path Parameters:**
- `idPedido` (integer): ID del pedido a actualizar

**Request Body:**
```json
{
  "fechaCierre": "2025-11-20",
  "productos": [
    {
      "idProducto": 45,
      "cantidadMin": 15,
      "cantidadMax": 120
    }
  ]
}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Pedido actualizado exitosamente",
  "data": {
    "idPedido": 123,
    "fechaCierre": "2025-11-20",
    ...
  },
  "timestamp": "2025-10-26T19:00:00"
}
```

---

### 5. Actualizar Estado del Pedido

```http
PUT /api/pedidos/{idPedido}/estado/{estado}
Authorization: Bearer {token}
```

**Path Parameters:**
- `idPedido` (integer): ID del pedido
- `estado` (string): Nuevo estado [`AGP`, `CSL`, `CRM`, `FNL`]

**Ejemplo:**
```
PUT /api/pedidos/123/estado/CSL
```

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Estado del pedido actualizado a CSL",
  "data": {
    "idPedido": 123,
    "estadoPedido": "CSL",
    ...
  },
  "timestamp": "2025-10-26T19:00:00"
}
```

**Estados Válidos:**
- `AGP` - Aguardando Solicitudes
- `CSL` - Consolidado
- `CRM` - Cancelado/Removido
- `FNL` - Finalizado

---

### 6. Cancelar Pedido

```http
DELETE /api/pedidos/{idPedido}
Authorization: Bearer {token}
```

**Descripción:** Cancela un pedido (cambia estado a `CRM`).

**Path Parameters:**
- `idPedido` (integer): ID del pedido a cancelar

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Pedido cancelado exitosamente (estado cambiado a CRM)",
  "data": null,
  "timestamp": "2025-10-26T19:00:00"
}
```

---

## 📦 Gestión de Productos del Pedido

### 7. Agregar Producto al Pedido

```http
POST /api/pedidos/{idPedido}/productos
Content-Type: application/json
Authorization: Bearer {token}
```

**Path Parameters:**
- `idPedido` (integer): ID del pedido

**Request Body:**
```json
{
  "idProducto": 47,
  "cantidadMin": 20,
  "cantidadMax": 80
}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Producto agregado exitosamente al pedido",
  "data": {
    "idPedido": 123,
    "productos": [
      {...existing products...},
      {
        "idProducto": 47,
        "cantidadMin": 20,
        "cantidadMax": 80
      }
    ]
  },
  "timestamp": "2025-10-26T19:00:00"
}
```

---

### 8. Eliminar Producto del Pedido

```http
DELETE /api/pedidos/{idPedido}/productos/{idProducto}
Authorization: Bearer {token}
```

**Path Parameters:**
- `idPedido` (integer): ID del pedido
- `idProducto` (integer): ID del producto a eliminar

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Producto eliminado exitosamente del pedido",
  "data": {
    "idPedido": 123,
    "productos": [...]
  },
  "timestamp": "2025-10-26T19:00:00"
}
```

---

### 9. Modificar Producto del Pedido

```http
PUT /api/pedidos/{idPedido}/productos/{idProducto}
Content-Type: application/json
Authorization: Bearer {token}
```

**Path Parameters:**
- `idPedido` (integer): ID del pedido
- `idProducto` (integer): ID del producto a modificar

**Request Body:**
```json
{
  "cantidadMin": 25,
  "cantidadMax": 150
}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Producto del pedido modificado exitosamente",
  "data": {
    "idPedido": 123,
    "productos": [
      {
        "idProducto": 45,
        "cantidadMin": 25,
        "cantidadMax": 150
      }
    ]
  },
  "timestamp": "2025-10-26T19:00:00"
}
```

---

## 🌐 Endpoint Público

### 10. Obtener Pedido por Hash

```http
GET /api/pedidos/pedido-disponible/{hash}
```

**Descripción:** Obtiene un pedido usando su hash público. **NO requiere autenticación.**

**Seguridad:** 🌐 Público (sin token)

**Path Parameters:**
- `hash` (string): Hash único del pedido (ej: `ABC123XYZ456RANDOM`)

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Pedido encontrado",
  "data": {
    "idPedido": 123,
    "estadoPedido": "AGP",
    "fechaCreado": "2025-10-26",
    "fechaCierre": "2025-11-15",
    "urlHash": "ABC123XYZ456RANDOM",
    "productos": [
      {
        "idProducto": 45,
        "cantidadMin": 10,
        "cantidadMax": 100
      }
    ]
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Uso:** Este endpoint se comparte con clientes vía WhatsApp para que vean el pedido disponible.

**Errores:**
- `404`: Hash inválido o pedido no encontrado

---

## 📦 Modelos de Datos

### PedidoDTO
```typescript
{
  idPedido?: number          // Auto-generado, read-only
  idAdmin: number            // ID del administrador
  estadoPedido: string       // "AGP" | "CSL" | "CRM" | "FNL"
  fechaCreado: string        // Formato: YYYY-MM-DD, read-only
  fechaCierre: string        // Formato: YYYY-MM-DD
  urlHash: string            // Hash único generado automáticamente
  productos: ProductoPedidoDTO[]  // Lista de productos
}
```

### ProductoPedidoDTO
```typescript
{
  idProducto: number         // ID del producto
  cantidadMin: number        // Cantidad mínima permitida
  cantidadMax: number        // Cantidad máxima permitida
}
```

---

## 🔒 Seguridad

### Autenticación
- **Endpoints privados:** Bearer Token JWT requerido
- **Endpoint público:** `/pedido-disponible/{hash}` NO requiere token

### Autorización
- **Roles permitidos:** `SAD`, `ADM`
- **Validación:** `@PreAuthorize("hasAnyRole('SAD', 'ADM')")`

---

## ⚠️ Códigos de Error

| Código | Descripción |
|--------|-------------|
| 400 | Validación fallida, datos incorrectos |
| 403 | Sin permisos (no es SAD/ADM) |
| 404 | Pedido o producto no encontrado |
| 409 | Producto duplicado en el pedido |
| 500 | Error interno del servidor |

---

## 📝 Notas Importantes

### Hash de Pedidos
- Generado automáticamente con **Hashids**
- Longitud mínima: 15 caracteres
- Único por pedido
- Se usa para URLs compartibles con clientes

### Ciclo de Vida del Pedido
```
AGP (Aguardando) → CSL (Consolidado) → FNL (Finalizado)
                ↘ CRM (Cancelado)
```

### Restricciones
- `cantidadMin` debe ser menor o igual que `cantidadMax`
- `fechaCierre` debe ser una fecha futura
- No se puede modificar un pedido en estado `FNL` o `CRM`

---

## 🧪 Ejemplos con cURL

### Crear Pedido
```bash
curl -X POST "http://localhost:8080/api/pedidos/5" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "fechaCierre": "2025-11-15",
    "productos": [
      {"idProducto": 45, "cantidadMin": 10, "cantidadMax": 100}
    ]
  }'
```

### Listar Pedidos
```bash
curl -X GET "http://localhost:8080/api/pedidos" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Actualizar Estado
```bash
curl -X PUT "http://localhost:8080/api/pedidos/123/estado/CSL" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Agregar Producto
```bash
curl -X POST "http://localhost:8080/api/pedidos/123/productos" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "idProducto": 47,
    "cantidadMin": 20,
    "cantidadMax": 80
  }'
```

### Obtener por Hash (Público)
```bash
curl -X GET "http://localhost:8080/api/pedidos/pedido-disponible/ABC123XYZ456RANDOM"
```
