# API Documentation - Solicitud Controller (Parte 2/2)

## Endpoints (Parte 2 - Últimos 6 endpoints)

### 7. Eliminar Producto de Solicitud

Elimina un producto específico de una solicitud existente.

**Endpoint:**
```
DELETE /solicitudes/{idSolicitud}/productos/{idProducto}
```

**Autenticación:** Requerida (Token OTP)

**Roles:** Dueño de la solicitud (validado por cédula del token)

**Path Parameters:**
- `idSolicitud` (Integer, requerido) - ID de la solicitud
- `idProducto` (Integer, requerido) - ID del producto a eliminar

**Request Headers:**
```http
Authorization: Bearer <token_otp>
```

**Request Body:** No requiere

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Producto eliminado exitosamente de la solicitud",
  "datos": {
    "idSolicitud": 48,
    "idCliente": 25,
    "nombreCliente": "María Fernanda López Martínez",
    "idPedido": 5,
    "estadoSolicitud": "PND",
    "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
    "fechaSolicitud": "2025-10-26",
    "fechaLimitePago": "2025-10-29",
    "modificacionesRestantes": 3,
    "productos": [
      {
        "idProducto": 10,
        "nombreProducto": "Arroz Premium 1kg",
        "cantidadSolicitada": 2,
        "precio": 1.85
      }
    ]
  },
  "timestamp": "2025-10-26T12:30:00"
}
```

**Response (400 Bad Request - Último producto):**
```json
{
  "exito": false,
  "mensaje": "No se puede eliminar el último producto. La solicitud debe tener al menos un producto.",
  "datos": null,
  "timestamp": "2025-10-26T12:30:00"
}
```

**Response (404 Not Found - Producto no existe):**
```json
{
  "exito": false,
  "mensaje": "El producto no existe en esta solicitud",
  "datos": null,
  "timestamp": "2025-10-26T12:30:00"
}
```

**Códigos de Estado:**
- `200 OK` - Producto eliminado exitosamente
- `400 Bad Request` - Intento de eliminar último producto
- `401 Unauthorized` - Token no válido o expirado
- `403 Forbidden` - Token OTP no pertenece al dueño de la solicitud
- `404 Not Found` - Solicitud o producto no encontrado en la solicitud
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X DELETE "http://localhost:8080/solicitudes/48/productos/18" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Notas Importantes:**
- **Requiere Token OTP**
- **Validación de propiedad**: Sistema verifica que cédula del token coincida con dueño
- **NO consume modificaciones restantes** (eliminar producto es operación separada)
- **Una solicitud SIEMPRE debe tener al menos 1 producto**
- Si intenta eliminar el último producto → Error 400
- Solo solicitudes en estado `PND` permiten eliminar productos
- El producto se elimina permanentemente de la solicitud (no se marca como inactivo)

**Casos de Uso:**
1. Cliente agregó producto por error → Lo elimina
2. Cliente decidió no comprar ese producto → Lo quita de la solicitud
3. Cliente quiere reemplazar producto → Elimina viejo, agrega nuevo

**⚠️ RESTRICCIÓN IMPORTANTE:**
```
Productos en solicitud: [A, B, C]
DELETE producto B → ✅ OK (quedan A y C)
DELETE producto C → ✅ OK (quedan A y B)
DELETE producto A → ✅ OK (quedan B y C)

Productos en solicitud: [A]
DELETE producto A → ❌ ERROR: "No se puede eliminar el último producto"
```

---

### 8. Modificar Cantidad de Producto

Modifica la cantidad solicitada de un producto específico que ya existe en la solicitud.

**Endpoint:**
```
PUT /solicitudes/{idSolicitud}/productos/{idProducto}
```

**Autenticación:** Requerida (Token OTP)

**Roles:** Dueño de la solicitud (validado por cédula del token)

**Path Parameters:**
- `idSolicitud` (Integer, requerido) - ID de la solicitud
- `idProducto` (Integer, requerido) - ID del producto a modificar

**Request Headers:**
```http
Authorization: Bearer <token_otp>
Content-Type: application/json
```

**Request Body:**
```json
{
  "cantidad": 10
}
```

**Validaciones Request:**
- `cantidad`: Requerido, debe ser mayor a 0
- Cantidad debe estar dentro del rango permitido (cantidadMin - cantidadMax del producto en el pedido)
- Producto debe existir en la solicitud

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Cantidad del producto modificada exitosamente",
  "datos": {
    "idSolicitud": 48,
    "idCliente": 25,
    "nombreCliente": "María Fernanda López Martínez",
    "idPedido": 5,
    "estadoSolicitud": "PND",
    "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
    "fechaSolicitud": "2025-10-26",
    "fechaLimitePago": "2025-10-29",
    "modificacionesRestantes": 3,
    "productos": [
      {
        "idProducto": 10,
        "nombreProducto": "Arroz Premium 1kg",
        "cantidadSolicitada": 10,
        "precio": 1.85
      },
      {
        "idProducto": 18,
        "nombreProducto": "Fideo Tallarin 500g",
        "cantidadSolicitada": 4,
        "precio": 1.45
      }
    ]
  },
  "timestamp": "2025-10-26T13:00:00"
}
```

**Response (400 Bad Request - Cantidad inválida):**
```json
{
  "exito": false,
  "mensaje": "La cantidad debe ser mayor a 0",
  "datos": null,
  "timestamp": "2025-10-26T13:00:00"
}
```

**Response (400 Bad Request - Fuera de rango):**
```json
{
  "exito": false,
  "mensaje": "La cantidad solicitada (25) está fuera del rango permitido (min: 5, max: 20)",
  "datos": null,
  "timestamp": "2025-10-26T13:00:00"
}
```

**Response (404 Not Found):**
```json
{
  "exito": false,
  "mensaje": "El producto no existe en esta solicitud",
  "datos": null,
  "timestamp": "2025-10-26T13:00:00"
}
```

**Códigos de Estado:**
- `200 OK` - Cantidad modificada exitosamente
- `400 Bad Request` - Cantidad <= 0 o fuera del rango permitido
- `401 Unauthorized` - Token no válido o expirado
- `403 Forbidden` - Token OTP no pertenece al dueño de la solicitud
- `404 Not Found` - Solicitud o producto no encontrado en la solicitud
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X PUT "http://localhost:8080/solicitudes/48/productos/10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{"cantidad": 10}'
```

**Notas Importantes:**
- **Requiere Token OTP**
- **NO consume modificaciones restantes** (modificar cantidad es operación separada)
- Cantidad se valida contra rangos del pedido (cantidadMin/cantidadMax)
- Solo solicitudes en estado `PND` permiten modificar cantidades
- Formato del body: simple objeto con clave `cantidad`
- Actualiza solo la cantidad, mantiene el mismo producto

**Diferencia con Endpoint "Modificar Solicitud":**
- **Modificar Solicitud** (`PUT /solicitudes/{id}/modificar`):
  - Reemplaza TODA la lista de productos
  - Puede cambiar dirección también
  - **Consume 1 modificación**
  
- **Modificar Cantidad** (`PUT /solicitudes/{id}/productos/{idProducto}`):
  - Modifica solo cantidad de UN producto específico
  - No afecta otros productos ni dirección
  - **NO consume modificaciones**

**Ejemplo de Uso:**
```javascript
// Cliente quiere aumentar cantidad de arroz de 2 a 10 unidades
await modificarCantidadProducto(48, 10, {cantidad: 10}, tokenOtp);
// Resultado: Solo el arroz cambia de 2 → 10
// Los demás productos quedan intactos
```

---

### 9. Cancelar Solicitud

Cancela (elimina) una solicitud existente de forma permanente.

**Endpoint:**
```
DELETE /solicitudes/{idSolicitud}
```

**Autenticación:** NO especificada (permite cancelación sin autenticación estricta)

**Path Parameters:**
- `idSolicitud` (Integer, requerido) - ID de la solicitud a cancelar

**Request Headers:**
```http
Content-Type: application/json
```

**Request Body:** No requiere

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Solicitud cancelada exitosamente",
  "datos": null,
  "timestamp": "2025-10-26T13:30:00"
}
```

**Response (404 Not Found):**
```json
{
  "exito": false,
  "mensaje": "Solicitud no encontrada con ID: 999",
  "datos": null,
  "timestamp": "2025-10-26T13:30:00"
}
```

**Códigos de Estado:**
- `200 OK` - Solicitud cancelada exitosamente
- `404 Not Found` - Solicitud no encontrada
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X DELETE "http://localhost:8080/solicitudes/48"
```

**Notas Importantes:**
- **NO requiere autenticación** (endpoint abierto)
- Elimina la solicitud **permanentemente** de la base de datos
- No valida estado de la solicitud (puede cancelar cualquier estado)
- No valida propiedad (cualquiera con el ID puede cancelar)
- ⚠️ **RIESGO DE SEGURIDAD**: Considerar agregar autenticación o cambiar a soft delete

**Recomendaciones de Mejora:**

1. **Agregar Autenticación:**
```java
@DeleteMapping("/{idSolicitud}")
public ResponseEntity<ApiResponse<Void>> cancelarSolicitud(
    @PathVariable Integer idSolicitud,
    HttpServletRequest request) {
    
    String cedula = getCedulaFromToken(request);
    authorizationService.validateSolicitudOwnership(idSolicitud, cedula);
    // ... resto del código
}
```

2. **Soft Delete (Cambiar Estado):**
```java
// En lugar de eliminar, cambiar estado a "CAN"
solicitudService.cambiarEstado(idSolicitud, "CAN");
```

3. **Restricción por Estado:**
```java
// Solo permitir cancelar solicitudes pendientes
if (!solicitud.getEstadoSolicitud().equals("PND")) {
    throw new BusinessException("Solo se pueden cancelar solicitudes pendientes");
}
```

**Uso Actual:**
```bash
# Cualquiera puede cancelar conociendo el ID
DELETE /solicitudes/48
# ✅ Solicitud 48 eliminada (sin validar dueño)
```

**Uso Recomendado:**
```bash
# Con autenticación OTP
DELETE /solicitudes/48
Authorization: Bearer <token_otp>
# ✅ Solicitud 48 cancelada (validando que es el dueño)
```

---

### 10. Listar Solicitudes de Cliente

Obtiene todas las solicitudes de un cliente específico (por ID de cliente).

**Endpoint:**
```
GET /solicitudes/{idCliente}
```

**Autenticación:** NO especificada (endpoint abierto)

**Path Parameters:**
- `idCliente` (Integer, requerido) - ID del cliente

**Request Headers:**
```http
Content-Type: application/json
```

**Request Body:** No requiere

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 7 solicitud(es) para el cliente",
  "datos": [
    {
      "idSolicitud": 48,
      "idCliente": 25,
      "nombreCliente": "María Fernanda López Martínez",
      "idPedido": 5,
      "estadoSolicitud": "PND",
      "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
      "fechaSolicitud": "2025-10-26",
      "fechaLimitePago": "2025-10-29",
      "modificacionesRestantes": 3,
      "productos": [
        {
          "idProducto": 10,
          "nombreProducto": "Arroz Premium 1kg",
          "cantidadSolicitada": 2,
          "precio": 1.85
        }
      ]
    },
    {
      "idSolicitud": 52,
      "idCliente": 25,
      "nombreCliente": "María Fernanda López Martínez",
      "idPedido": 5,
      "estadoSolicitud": "PGD",
      "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
      "fechaSolicitud": "2025-10-22",
      "fechaLimitePago": "2025-10-25",
      "modificacionesRestantes": 1,
      "productos": [...]
    },
    {
      "idSolicitud": 35,
      "idCliente": 25,
      "nombreCliente": "María Fernanda López Martínez",
      "idPedido": 3,
      "estadoSolicitud": "ENT",
      "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
      "fechaSolicitud": "2025-10-10",
      "fechaLimitePago": "2025-10-13",
      "modificacionesRestantes": 0,
      "productos": [...]
    }
  ],
  "timestamp": "2025-10-26T14:00:00"
}
```

**Response (200 OK - Sin solicitudes):**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 0 solicitud(es) para el cliente",
  "datos": [],
  "timestamp": "2025-10-26T14:00:00"
}
```

**Códigos de Estado:**
- `200 OK` - Solicitudes obtenidas exitosamente (puede ser lista vacía)
- `404 Not Found` - Cliente no encontrado
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/solicitudes/25"
```

**Notas Importantes:**
- **NO requiere autenticación** (endpoint abierto)
- Devuelve solicitudes de todos los pedidos del cliente
- Incluye solicitudes en cualquier estado (PND, PGD, ENT, CAN)
- No filtra por estado ni fecha
- ⚠️ **Consideración de privacidad**: Cualquiera con el ID puede ver solicitudes del cliente

**Diferencia con Otros Endpoints de Listado:**

| Endpoint | Autenticación | Filtro | Uso |
|----------|---------------|--------|-----|
| `GET /solicitudes/mis-solicitudes` | Token OTP | Por cédula (automático) | Cliente consulta sus solicitudes |
| `GET /solicitudes/mis-solicitudes/{hash}` | Token OTP | Por cédula + hash pedido | Cliente consulta solicitudes de un pedido |
| `GET /solicitudes/{idCliente}` | NO | Por ID cliente | Consulta abierta por cliente |
| `GET /solicitudes/pedido/{idPedido}` | JWT (SAD/ADM) | Por pedido + estado | Admin consulta solicitudes de pedido |

**Recomendación de Mejora:**
```java
// Agregar autenticación para proteger privacidad
@GetMapping("/{idCliente}")
@PreAuthorize("hasAnyRole('SAD', 'ADM')")  // O validar que sea el mismo cliente
public ResponseEntity<ApiResponse<List<SolicitudDTO>>> listarSolicitudesCliente(
    @PathVariable Integer idCliente) {
    // ...
}
```

---

### 11. Cambiar Estado de Solicitud (Manual)

Permite a administradores cambiar manualmente el estado de una solicitud (usado principalmente para confirmación de pago manual).

**Endpoint:**
```
PUT /solicitudes/{idSolicitud}/estado
```

**Autenticación:** Requerida (JWT estándar)

**Roles Permitidos:** `SAD`, `ADM`

**Path Parameters:**
- `idSolicitud` (Integer, requerido) - ID de la solicitud

**Request Headers:**
```http
Authorization: Bearer <token_jwt_estandar>
Content-Type: application/json
```

**Request Body:**
```json
{
  "nuevoEstado": "PGD"
}
```

**Valores Válidos para `nuevoEstado`:**
- `"PGD"` - Pagado (confirmación manual de pago)
- `"CAN"` - Cancelado (cancelación administrativa)

**Validaciones Request:**
- `nuevoEstado`: Requerido, debe ser "PGD" o "CAN"
- Solicitud debe existir

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Estado actualizado a PGD",
  "datos": null,
  "timestamp": "2025-10-26T14:30:00"
}
```

**Response (404 Not Found):**
```json
{
  "exito": false,
  "mensaje": "Solicitud no encontrada con ID: 999",
  "datos": null,
  "timestamp": "2025-10-26T14:30:00"
}
```

**Response (400 Bad Request):**
```json
{
  "exito": false,
  "mensaje": "Estado inválido. Valores permitidos: PGD, CAN",
  "datos": null,
  "timestamp": "2025-10-26T14:30:00"
}
```

**Códigos de Estado:**
- `200 OK` - Estado actualizado exitosamente
- `400 Bad Request` - Estado inválido
- `401 Unauthorized` - Token no válido o expirado
- `403 Forbidden` - Usuario no tiene rol SAD o ADM
- `404 Not Found` - Solicitud no encontrada
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL (Confirmar Pago):**
```bash
curl -X PUT "http://localhost:8080/solicitudes/48/estado" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{"nuevoEstado": "PGD"}'
```

**Ejemplo cURL (Cancelar Administrativamente):**
```bash
curl -X PUT "http://localhost:8080/solicitudes/48/estado" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{"nuevoEstado": "CAN"}'
```

**Notas Importantes:**
- **Requiere JWT estándar** (no Token OTP)
- **Solo SAD y ADM** pueden usar este endpoint
- Usado principalmente para **confirmación manual de pagos**
- No valida flujo de estados (puede cambiar cualquier estado a PGD o CAN)
- No registra quién hizo el cambio ni cuándo (considerar agregar auditoría)

**Casos de Uso:**

1. **Pago en Efectivo/Transferencia:**
```
Cliente realiza transferencia bancaria
→ Envía comprobante por WhatsApp
→ ADM verifica pago manualmente
→ ADM cambia estado PND → PGD
```

2. **Cancelación Administrativa:**
```
Cliente no respondió después de fecha límite
→ ADM cancela solicitud
→ ADM cambia estado PND → CAN
```

3. **Corrección de Errores:**
```
Sistema marcó solicitud como PND por error
→ ADM revisa y confirma que sí está pagada
→ ADM cambia estado a PGD
```

**Estados de Solicitud:**
- `PND` - Pendiente (esperando pago)
- `PGD` - Pagado (pago confirmado)
- `ENT` - Entregado (pedido entregado al cliente)
- `CAN` - Cancelado (solicitud cancelada)

**Flujo Normal:**
```
PND (creación) → PGD (pago confirmado) → ENT (entrega completada)
PND → CAN (cancelación)
PGD → CAN (cancelación post-pago, con reembolso)
```

**Recomendación de Mejora:**
```java
// Agregar auditoría de cambios
@PutMapping("/{idSolicitud}/estado")
@PreAuthorize("hasAnyRole('SAD', 'ADM')")
public ResponseEntity<ApiResponse<Void>> cambiarEstadoSolicitud(
        @PathVariable Integer idSolicitud,
        @RequestBody Map<String, String> body,
        Authentication authentication) {
    
    String nuevoEstado = body.get("nuevoEstado");
    String usuarioModificador = authentication.getName();
    
    solicitudService.cambiarEstadoConAuditoria(
        idSolicitud, 
        nuevoEstado,
        usuarioModificador,
        LocalDateTime.now()
    );
    
    // ...
}
```

---

### 12. Listar Solicitudes por Pedido (con Filtros)

Permite a administradores listar todas las solicitudes de un pedido específico, con opción de filtrar por estado.

**Endpoint:**
```
GET /solicitudes/pedido/{idPedido}
GET /solicitudes/pedido/{idPedido}?estado={estado}
```

**Autenticación:** Requerida (JWT estándar)

**Roles Permitidos:** `SAD`, `ADM`

**Path Parameters:**
- `idPedido` (Integer, requerido) - ID del pedido

**Query Parameters:**
- `estado` (String, opcional) - Estado de solicitud para filtrar
  - Valores válidos: `PND`, `PGD`, `ENT`, `CAN`
  - Si se omite, devuelve todas las solicitudes sin filtrar

**Request Headers:**
```http
Authorization: Bearer <token_jwt_estandar>
```

**Request Body:** No requiere

**Response (200 OK - Sin filtro de estado):**
```bash
GET /solicitudes/pedido/5
```

```json
{
  "exito": true,
  "mensaje": "Se encontraron 15 solicitud(es) para el pedido",
  "datos": [
    {
      "idSolicitud": 48,
      "idCliente": 25,
      "nombreCliente": "María Fernanda López Martínez",
      "idPedido": 5,
      "estadoSolicitud": "PND",
      "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
      "fechaSolicitud": "2025-10-26",
      "fechaLimitePago": "2025-10-29",
      "modificacionesRestantes": 3,
      "productos": [...]
    },
    {
      "idSolicitud": 49,
      "idCliente": 18,
      "nombreCliente": "Pedro Antonio Silva Vargas",
      "idPedido": 5,
      "estadoSolicitud": "PGD",
      "direccionEntrega": "Av. González Suárez N27-142",
      "fechaSolicitud": "2025-10-25",
      "fechaLimitePago": "2025-10-28",
      "modificacionesRestantes": 2,
      "productos": [...]
    },
    {
      "idSolicitud": 50,
      "idCliente": 32,
      "nombreCliente": "Ana María Torres López",
      "idPedido": 5,
      "estadoSolicitud": "CAN",
      "direccionEntrega": "Conjunto Los Pinos, Casa 45",
      "fechaSolicitud": "2025-10-24",
      "fechaLimitePago": "2025-10-27",
      "modificacionesRestantes": 0,
      "productos": [...]
    }
  ],
  "timestamp": "2025-10-26T15:00:00"
}
```

**Response (200 OK - Con filtro de estado):**
```bash
GET /solicitudes/pedido/5?estado=PGD
```

```json
{
  "exito": true,
  "mensaje": "Se encontraron 8 solicitud(es) con estado PGD",
  "datos": [
    {
      "idSolicitud": 49,
      "idCliente": 18,
      "nombreCliente": "Pedro Antonio Silva Vargas",
      "idPedido": 5,
      "estadoSolicitud": "PGD",
      "direccionEntrega": "Av. González Suárez N27-142",
      "fechaSolicitud": "2025-10-25",
      "fechaLimitePago": "2025-10-28",
      "modificacionesRestantes": 2,
      "productos": [...]
    },
    {
      "idSolicitud": 51,
      "idCliente": 22,
      "nombreCliente": "Carlos Roberto Méndez Castro",
      "idPedido": 5,
      "estadoSolicitud": "PGD",
      "direccionEntrega": "Calle Colón E5-67",
      "fechaSolicitud": "2025-10-24",
      "fechaLimitePago": "2025-10-27",
      "modificacionesRestantes": 1,
      "productos": [...]
    }
  ],
  "timestamp": "2025-10-26T15:00:00"
}
```

**Response (200 OK - Sin solicitudes):**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 0 solicitud(es) para el pedido",
  "datos": [],
  "timestamp": "2025-10-26T15:00:00"
}
```

**Códigos de Estado:**
- `200 OK` - Solicitudes obtenidas exitosamente (puede ser lista vacía)
- `401 Unauthorized` - Token no válido o expirado
- `403 Forbidden` - Usuario no tiene rol SAD o ADM
- `404 Not Found` - Pedido no encontrado
- `500 Internal Server Error` - Error del servidor

**Ejemplos cURL:**

**Todas las solicitudes del pedido:**
```bash
curl -X GET "http://localhost:8080/solicitudes/pedido/5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Solo solicitudes pendientes:**
```bash
curl -X GET "http://localhost:8080/solicitudes/pedido/5?estado=PND" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Solo solicitudes pagadas:**
```bash
curl -X GET "http://localhost:8080/solicitudes/pedido/5?estado=PGD" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Solo solicitudes canceladas:**
```bash
curl -X GET "http://localhost:8080/solicitudes/pedido/5?estado=CAN" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Notas Importantes:**
- **Requiere JWT estándar** (no Token OTP)
- **Solo SAD y ADM** pueden usar este endpoint
- El parámetro `estado` es **opcional**
- Estados válidos: `PND`, `PGD`, `ENT`, `CAN`
- El mensaje cambia según si hay filtro o no
- Útil para dashboards administrativos

**Casos de Uso:**

1. **Dashboard de Pedido:**
```javascript
// ADM ve resumen del pedido 5
const todas = await listarSolicitudesPedido(5);
// Total: 15 solicitudes

// Desglose por estado
const pendientes = await listarSolicitudesPedido(5, 'PND'); // 5 pendientes
const pagadas = await listarSolicitudesPedido(5, 'PGD');    // 8 pagadas
const canceladas = await listarSolicitudesPedido(5, 'CAN'); // 2 canceladas
```

2. **Gestión de Pagos:**
```javascript
// ADM revisa solicitudes pendientes de pago
const pendientesPago = await listarSolicitudesPedido(5, 'PND');

// Filtra por fecha límite vencida
const vencidas = pendientesPago.filter(s => 
  new Date(s.fechaLimitePago) < new Date()
);

// Marca como canceladas las vencidas
for (const solicitud of vencidas) {
  await cambiarEstadoSolicitud(solicitud.idSolicitud, 'CAN');
}
```

3. **Preparación de Entregas:**
```javascript
// ADM prepara entregas del día
const pagadas = await listarSolicitudesPedido(5, 'PGD');

// Genera lista de empaque por dirección
const porDireccion = agruparPorDireccion(pagadas);

// Genera etiquetas de entrega
for (const grupo of porDireccion) {
  await generarEtiquetaEntrega(grupo);
}
```

4. **Reportes y Estadísticas:**
```javascript
// SAD genera reporte del pedido
const solicitudes = await listarSolicitudesPedido(5);

const estadisticas = {
  total: solicitudes.length,
  pendientes: solicitudes.filter(s => s.estadoSolicitud === 'PND').length,
  pagadas: solicitudes.filter(s => s.estadoSolicitud === 'PGD').length,
  entregadas: solicitudes.filter(s => s.estadoSolicitud === 'ENT').length,
  canceladas: solicitudes.filter(s => s.estadoSolicitud === 'CAN').length,
  tasaConversion: (pagadas / total) * 100,
  ingresoTotal: calcularIngresoTotal(solicitudes.filter(s => s.estadoSolicitud === 'PGD'))
};
```

---

## Modelos de Datos Completos

### SolicitudDTO

Modelo principal para representar una solicitud en el sistema.

**TypeScript Interface:**
```typescript
interface SolicitudDTO {
  idSolicitud: number;              // ID único de la solicitud
  idCliente: number;                // ID del cliente que creó la solicitud
  nombreCliente: string;            // Nombre completo del cliente (nombres + apellidos)
  idPedido: number;                 // ID del pedido al que pertenece
  estadoSolicitud: string;          // Estado actual: "PND" | "PGD" | "ENT" | "CAN"
  direccionEntrega: string;         // Dirección de entrega (puede ser diferente a la del cliente)
  fechaSolicitud: string;           // Fecha de creación (formato: "YYYY-MM-DD")
  fechaLimitePago: string;          // Fecha límite para realizar el pago (formato: "YYYY-MM-DD")
  modificacionesRestantes: number;  // Modificaciones disponibles (0-3)
  productos: SolicitudProductoDTO[]; // Lista de productos en la solicitud
}
```

**Estados Posibles:**
- `PND` - Pendiente de pago
- `PGD` - Pagado (pago confirmado)
- `ENT` - Entregado
- `CAN` - Cancelado

**Ejemplo Completo:**
```json
{
  "idSolicitud": 48,
  "idCliente": 25,
  "nombreCliente": "María Fernanda López Martínez",
  "idPedido": 5,
  "estadoSolicitud": "PND",
  "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
  "fechaSolicitud": "2025-10-26",
  "fechaLimitePago": "2025-10-29",
  "modificacionesRestantes": 3,
  "productos": [
    {
      "idProducto": 10,
      "nombreProducto": "Arroz Premium 1kg",
      "cantidadSolicitada": 2,
      "precio": 1.85
    },
    {
      "idProducto": 12,
      "nombreProducto": "Aceite Girasol 1L",
      "cantidadSolicitada": 3,
      "precio": 3.25
    }
  ]
}
```

---

### SolicitudClienteDTO

Modelo para crear solicitud + cliente en una sola operación (endpoint público).

**TypeScript Interface:**
```typescript
interface SolicitudClienteDTO {
  // Datos personales del cliente
  nombres: string;                   // Solo letras y espacios, máx 100 caracteres
  apellidos: string;                 // Solo letras y espacios, máx 100 caracteres
  telefono: string;                  // Exactamente 10 dígitos numéricos, único
  cedula: string;                    // Exactamente 10 dígitos numéricos, único
  direccion: string;                 // Máx 150 caracteres
  
  // Datos de la solicitud
  idPedido: number;                  // ID del pedido
  productos: SolicitudProductoDTO[]; // Lista de productos a solicitar
}
```

**Validaciones:**
- `nombres`: `@NotBlank`, `@Size(max=100)`, `@Pattern(regexp="^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")`
- `apellidos`: `@NotBlank`, `@Size(max=100)`, `@Pattern(regexp="^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")`
- `telefono`: `@NotBlank`, `@Pattern(regexp="^[0-9]{10}$")`
- `cedula`: `@NotBlank`, `@Pattern(regexp="^[0-9]{10}$")`
- `direccion`: `@NotBlank`, `@Size(max=150)`

**Ejemplo:**
```json
{
  "nombres": "María Fernanda",
  "apellidos": "López Martínez",
  "telefono": "0987654321",
  "cedula": "1750234567",
  "direccion": "Calle Mariscal Foch E7-45 y Diego de Almagro",
  "idPedido": 5,
  "productos": [
    {
      "idProducto": 10,
      "cantidadSolicitada": 2
    },
    {
      "idProducto": 12,
      "cantidadSolicitada": 3
    }
  ]
}
```

---

### SolicitudModificacionDTO

Modelo para modificar una solicitud existente (dirección y/o productos).

**TypeScript Interface:**
```typescript
interface SolicitudModificacionDTO {
  direccionEntrega?: string;                  // Nueva dirección (opcional)
  nuevaDireccion?: string;                    // Alias de direccionEntrega (opcional)
  productos?: ProductoModificacionDTO[];      // Nueva lista de productos (opcional)
}

interface ProductoModificacionDTO {
  idProducto: number;        // ID del producto
  cantidadSolicitada: number; // Cantidad deseada
}
```

**Notas:**
- Ambos campos son opcionales, pero al menos uno debe enviarse
- `direccionEntrega` y `nuevaDireccion` son alias (se puede usar cualquiera)
- Si se envía `productos`, **reemplaza completamente** la lista anterior

**Ejemplo:**
```json
{
  "direccionEntrega": "Nueva dirección: Av. Amazonas N24-155",
  "productos": [
    {
      "idProducto": 10,
      "cantidadSolicitada": 8
    },
    {
      "idProducto": 15,
      "cantidadSolicitada": 2
    }
  ]
}
```

---

### SolicitudProductoDTO

Modelo para representar un producto dentro de una solicitud.

**TypeScript Interface:**
```typescript
interface SolicitudProductoDTO {
  idProducto: number;         // ID del producto
  nombreProducto?: string;    // Nombre del producto (solo en respuestas)
  cantidadSolicitada: number; // Cantidad solicitada por el cliente
  precio?: number;            // Precio unitario (solo en respuestas)
}
```

**Ejemplo en Request:**
```json
{
  "idProducto": 10,
  "cantidadSolicitada": 5
}
```

**Ejemplo en Response:**
```json
{
  "idProducto": 10,
  "nombreProducto": "Arroz Premium 1kg",
  "cantidadSolicitada": 5,
  "precio": 1.85
}
```

---

## Resumen de Endpoints Completos

### Todos los Endpoints de SolicitudController

| # | Método | Endpoint | Autenticación | Roles | Descripción |
|---|--------|----------|---------------|-------|-------------|
| 1 | POST | `/solicitudes/{idCliente}` | JWT | CLI | Crear solicitud (cliente registrado) |
| 2 | POST | `/solicitudes/public/nueva` | NO | - | Crear solicitud (cliente nuevo) |
| 3 | GET | `/solicitudes/mis-solicitudes/{hashPedido}` | Token OTP | - | Listar mis solicitudes por pedido |
| 4 | GET | `/solicitudes/mis-solicitudes` | Token OTP | - | Listar todas mis solicitudes |
| 5 | PUT | `/solicitudes/{idSolicitud}/modificar` | Token OTP | - | Modificar solicitud (máx 3 veces) |
| 6 | POST | `/solicitudes/{idSolicitud}/productos` | Token OTP | - | Agregar producto |
| 7 | DELETE | `/solicitudes/{idSolicitud}/productos/{idProducto}` | Token OTP | - | Eliminar producto |
| 8 | PUT | `/solicitudes/{idSolicitud}/productos/{idProducto}` | Token OTP | - | Modificar cantidad producto |
| 9 | DELETE | `/solicitudes/{idSolicitud}` | NO | - | Cancelar solicitud |
| 10 | GET | `/solicitudes/{idCliente}` | NO | - | Listar solicitudes de cliente |
| 11 | PUT | `/solicitudes/{idSolicitud}/estado` | JWT | SAD, ADM | Cambiar estado manual |
| 12 | GET | `/solicitudes/pedido/{idPedido}` | JWT | SAD, ADM | Listar por pedido con filtros |

**Total:** 12 endpoints

**Desglose por Tipo de Autenticación:**
- **Token OTP**: 6 endpoints (consulta/modificación cliente)
- **JWT Estándar**: 3 endpoints (creación CLI, gestión admin)
- **Sin Autenticación**: 3 endpoints (público, cancelar, listar)

---

## Seguridad y Validaciones

### Validación de Propiedad (Token OTP)

Los endpoints que requieren Token OTP implementan validación de propiedad:

```java
private String getCedulaFromToken(HttpServletRequest request) {
    String jwt = getJwtFromRequest(request);
    
    // 1. Validar que existe token
    if (jwt == null) {
        throw new BusinessException("Token no encontrado");
    }
    
    // 2. Validar que el token es válido
    if (!jwtTokenProvider.validateToken(jwt)) {
        throw new BusinessException("Token inválido o expirado");
    }
    
    // 3. Validar que es un token OTP
    if (!jwtTokenProvider.isOtpToken(jwt)) {
        throw new BusinessException("Este endpoint requiere autenticación OTP");
    }
    
    // 4. Extraer cédula del token
    return jwtTokenProvider.getCedulaFromJWT(jwt);
}

// Validar que la solicitud pertenece al dueño
authorizationService.validateSolicitudOwnership(idSolicitud, cedula);
```

**Proceso de Validación:**
1. Extrae token del header Authorization
2. Valida que el token sea válido (firma, expiración)
3. Verifica que sea un token OTP (claim `isOtpToken: true`)
4. Extrae cédula del token
5. Compara cédula con el dueño de la solicitud
6. Si no coincide → Error 403 Forbidden

---

## Manejo de Errores Específicos

### Errores de Token OTP

```json
{
  "exito": false,
  "mensaje": "Token no encontrado. Debe autenticarse vía OTP primero.",
  "datos": null,
  "timestamp": "2025-10-26T15:30:00"
}
```

```json
{
  "exito": false,
  "mensaje": "Este endpoint requiere autenticación OTP.",
  "datos": null,
  "timestamp": "2025-10-26T15:30:00"
}
```

### Errores de Validación de Propiedad

```json
{
  "exito": false,
  "mensaje": "No tienes permiso para modificar esta solicitud",
  "datos": null,
  "timestamp": "2025-10-26T15:30:00"
}
```

### Errores de Modificaciones Agotadas

```json
{
  "exito": false,
  "mensaje": "No quedan modificaciones disponibles para esta solicitud",
  "datos": null,
  "timestamp": "2025-10-26T15:30:00"
}
```

### Errores de Productos

```json
{
  "exito": false,
  "mensaje": "El producto ya existe en la solicitud. Use el endpoint de modificar cantidad.",
  "datos": null,
  "timestamp": "2025-10-26T15:30:00"
}
```

```json
{
  "exito": false,
  "mensaje": "No se puede eliminar el último producto. La solicitud debe tener al menos un producto.",
  "datos": null,
  "timestamp": "2025-10-26T15:30:00"
}
```

```json
{
  "exito": false,
  "mensaje": "La cantidad solicitada (25) está fuera del rango permitido (min: 5, max: 20)",
  "datos": null,
  "timestamp": "2025-10-26T15:30:00"
}
```

---

## Casos de Uso Completos

### Flujo 1: Cliente Nuevo Crea Solicitud

```javascript
// 1. Cliente recibe link del pedido por WhatsApp
const urlPedido = "https://app.com/pedido/abc123def456xyz789";

// 2. Cliente ingresa sus datos y productos deseados
const solicitud = await crearSolicitudClienteNuevo({
  nombres: "María Fernanda",
  apellidos: "López Martínez",
  telefono: "0987654321",
  cedula: "1750234567",
  direccion: "Calle Mariscal Foch E7-45 y Diego de Almagro",
  idPedido: 5,
  productos: [
    { idProducto: 10, cantidadSolicitada: 2 },
    { idProducto: 12, cantidadSolicitada: 3 }
  ]
});
// Sistema crea usuario + solicitud automáticamente
// solicitud.idSolicitud = 48

// 3. Cliente recibe confirmación
console.log(`Solicitud creada: ID ${solicitud.idSolicitud}`);
console.log(`Estado: ${solicitud.estadoSolicitud}`); // "PND"
console.log(`Pagar antes de: ${solicitud.fechaLimitePago}`); // "2025-10-29"
```

### Flujo 2: Cliente Modifica Su Solicitud

```javascript
// 1. Cliente necesita cambiar dirección y agregar producto
// Primero solicita OTP
await solicitarOtp({ cedula: "1750234567" });
// → Recibe SMS con código: 123456

// 2. Verifica código OTP
const { token } = await verificarOtp({ 
  cedula: "1750234567", 
  codigoOtp: "123456" 
});
// → Obtiene Token OTP temporal

// 3. Modifica la solicitud
const solicitudModificada = await modificarSolicitud(48, {
  direccionEntrega: "Nueva dirección: Av. Amazonas N24-155",
  productos: [
    { idProducto: 10, cantidadSolicitada: 8 },  // Era 2, ahora 8
    { idProducto: 12, cantidadSolicitada: 3 },  // Mantiene 3
    { idProducto: 15, cantidadSolicitada: 2 }   // NUEVO producto
  ]
}, token);

// modificacionesRestantes: 3 → 2
```

### Flujo 3: Cliente Gestiona Productos Individualmente

```javascript
// Token OTP ya obtenido previamente

// 1. Agregar nuevo producto
await agregarProducto(48, {
  idProducto: 18,
  cantidadSolicitada: 4
}, tokenOtp);
// ✅ Producto agregado (NO consume modificaciones)

// 2. Modificar cantidad de producto existente
await modificarCantidadProducto(48, 10, {
  cantidad: 15  // Cambia de 8 a 15
}, tokenOtp);
// ✅ Cantidad modificada (NO consume modificaciones)

// 3. Eliminar producto que no necesita
await eliminarProducto(48, 12, tokenOtp);
// ✅ Producto eliminado (NO consume modificaciones)

// 4. Consultar estado final
const solicitudes = await listarMisSolicitudes(tokenOtp);
// modificacionesRestantes sigue en 2 (solo el PUT /modificar consume)
```

### Flujo 4: Administrador Gestiona Pagos

```javascript
// ADM tiene JWT estándar

// 1. Ver todas las solicitudes del pedido
const todas = await listarSolicitudesPedido(5, tokenJWT);
// 15 solicitudes totales

// 2. Filtrar pendientes de pago
const pendientes = await listarSolicitudesPedido(5, tokenJWT, { estado: 'PND' });
// 5 solicitudes pendientes

// 3. Cliente envía comprobante de pago por WhatsApp
// ADM verifica y confirma pago manualmente
await cambiarEstadoSolicitud(48, { nuevoEstado: 'PGD' }, tokenJWT);
// ✅ Solicitud 48: PND → PGD

// 4. Verificar solicitudes pagadas
const pagadas = await listarSolicitudesPedido(5, tokenJWT, { estado: 'PGD' });
// 9 solicitudes pagadas (8 anteriores + 1 nueva)
```

---

## Integración Frontend - Ejemplos TypeScript

### Crear Solicitud (Cliente Nuevo)

```typescript
async function crearSolicitudPublica(datos: SolicitudClienteDTO): Promise<SolicitudDTO> {
  try {
    const response = await axios.post<ApiResponse<SolicitudDTO>>(
      'http://localhost:8080/solicitudes/public/nueva',
      datos
    );

    if (response.data.exito) {
      console.log(response.data.mensaje); // "Solicitud y cliente creados exitosamente"
      return response.data.datos;
    } else {
      throw new Error(response.data.mensaje);
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 400) {
        const mensaje = error.response.data.mensaje;
        if (mensaje.includes('cédula')) {
          throw new Error('Ya existe un usuario con esta cédula. Usa el login estándar.');
        } else if (mensaje.includes('teléfono')) {
          throw new Error('Ya existe un usuario con este teléfono.');
        }
      }
    }
    throw error;
  }
}
```

### Modificar Solicitud con Token OTP

```typescript
async function modificarSolicitudOtp(
  idSolicitud: number,
  datos: SolicitudModificacionDTO,
  tokenOtp: string
): Promise<SolicitudDTO> {
  try {
    const response = await axios.put<ApiResponse<SolicitudDTO>>(
      `http://localhost:8080/solicitudes/${idSolicitud}/modificar`,
      datos,
      {
        headers: {
          'Authorization': `Bearer ${tokenOtp}`,
          'Content-Type': 'application/json'
        }
      }
    );

    if (response.data.exito) {
      console.log(response.data.mensaje); // "Solicitud modificada exitosamente"
      console.log(`Modificaciones restantes: ${response.data.datos.modificacionesRestantes}`);
      return response.data.datos;
    } else {
      throw new Error(response.data.mensaje);
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 400) {
        throw new Error('No quedan modificaciones disponibles o la solicitud está en estado no modificable.');
      } else if (error.response?.status === 403) {
        throw new Error('No tienes permiso para modificar esta solicitud.');
      }
    }
    throw error;
  }
}
```

### Listar Solicitudes con Filtros (Admin)

```typescript
async function listarSolicitudesPedidoAdmin(
  idPedido: number,
  estado?: 'PND' | 'PGD' | 'ENT' | 'CAN',
  tokenJWT: string
): Promise<SolicitudDTO[]> {
  try {
    const url = estado
      ? `http://localhost:8080/solicitudes/pedido/${idPedido}?estado=${estado}`
      : `http://localhost:8080/solicitudes/pedido/${idPedido}`;

    const response = await axios.get<ApiResponse<SolicitudDTO[]>>(url, {
      headers: {
        'Authorization': `Bearer ${tokenJWT}`
      }
    });

    if (response.data.exito) {
      console.log(response.data.mensaje);
      return response.data.datos;
    } else {
      throw new Error(response.data.mensaje);
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 403) {
        throw new Error('Acceso denegado: se requiere rol SAD o ADM');
      }
    }
    throw error;
  }
}

// Uso
const todasSolicitudes = await listarSolicitudesPedidoAdmin(5, undefined, token);
const pendientes = await listarSolicitudesPedidoAdmin(5, 'PND', token);
const pagadas = await listarSolicitudesPedidoAdmin(5, 'PGD', token);
```

---

## Mejoras Recomendadas

### 1. Agregar Autenticación a Endpoints Abiertos

**Problema:** Endpoints sin autenticación exponen datos sensibles.

**Afectados:**
- `DELETE /solicitudes/{idSolicitud}` - Cancelar solicitud
- `GET /solicitudes/{idCliente}` - Listar solicitudes de cliente

**Solución:**
```java
@DeleteMapping("/{idSolicitud}")
public ResponseEntity<ApiResponse<Void>> cancelarSolicitud(
    @PathVariable Integer idSolicitud,
    HttpServletRequest request) {
    
    String cedula = getCedulaFromToken(request);
    authorizationService.validateSolicitudOwnership(idSolicitud, cedula);
    
    solicitudService.cancelarSolicitud(idSolicitud);
    // ...
}
```

### 2. Implementar Soft Delete en Cancelaciones

**Problema:** Cancelar elimina permanentemente datos.

**Solución:** Cambiar estado a "CAN" en lugar de eliminar:
```java
@DeleteMapping("/{idSolicitud}")
public ResponseEntity<ApiResponse<Void>> cancelarSolicitud(@PathVariable Integer idSolicitud) {
    // En lugar de: solicitudService.cancelarSolicitud(idSolicitud);
    solicitudService.cambiarEstado(idSolicitud, "CAN");
    // ...
}
```

### 3. Auditoría de Cambios de Estado

**Problema:** No se registra quién cambió el estado ni cuándo.

**Solución:** Tabla de auditoría:
```sql
CREATE TABLE solicitud_auditoria (
    id SERIAL PRIMARY KEY,
    id_solicitud INTEGER NOT NULL,
    estado_anterior VARCHAR(3),
    estado_nuevo VARCHAR(3) NOT NULL,
    usuario_modificador VARCHAR(10) NOT NULL,
    fecha_modificacion TIMESTAMP NOT NULL,
    motivo TEXT
);
```

### 4. Paginación en Listados

**Problema:** Endpoints devuelven todas las solicitudes sin límite.

**Solución:**
```java
@GetMapping("/pedido/{idPedido}")
public ResponseEntity<ApiResponse<Page<SolicitudDTO>>> listarSolicitudesPorPedidoYEstado(
    @PathVariable Integer idPedido,
    @RequestParam(required = false) String estado,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) {
    // ...
}
```

### 5. Validación de Estados Permitidos

**Problema:** Endpoint de cambio de estado acepta cualquier transición.

**Solución:** Validar flujo de estados:
```java
private static final Map<String, List<String>> TRANSICIONES_PERMITIDAS = Map.of(
    "PND", List.of("PGD", "CAN"),
    "PGD", List.of("ENT", "CAN"),
    "ENT", List.of(),  // Estado final
    "CAN", List.of()   // Estado final
);

public void validarTransicion(String estadoActual, String nuevoEstado) {
    if (!TRANSICIONES_PERMITIDAS.get(estadoActual).contains(nuevoEstado)) {
        throw new BusinessException(
            String.format("No se puede cambiar de %s a %s", estadoActual, nuevoEstado)
        );
    }
}
```

---

## Documentación Relacionada

- **[API_AuthController.md](./API_AuthController.md)** - Autenticación y registro
- **[API_OtpController.md](./API_OtpController.md)** - Flujo OTP para clientes sin login
- **[API_PedidoController.md](./API_PedidoController.md)** - Gestión de pedidos colectivos
- **[API_ProductoController.md](./API_ProductoController.md)** - Catálogo de productos

---

### Respuestas de error y fallos comunes (aplican a múltiples endpoints de solicitudes)

**Response 400 Bad Request:**
```json
{
  "exito": false,
  "mensaje": "Datos de entrada inválidos o operación no permitida (p. ej., eliminar último producto).",
  "errores": ["cantidadSolicitada debe ser mayor que 0","No se puede eliminar el último producto"],
  "timestamp": "2025-10-26T12:30:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "exito": false,
  "mensaje": "Token OTP inválido o expirado.",
  "timestamp": "2025-10-26T12:30:00"
}
```

**Response 403 Forbidden:**
```json
{
  "exito": false,
  "mensaje": "No tiene permisos para modificar esta solicitud (token no pertenece al propietario).",
  "timestamp": "2025-10-26T12:30:00"
}
```

**Response 404 Not Found:**
```json
{
  "exito": false,
  "mensaje": "Solicitud o producto no encontrado.",
  "timestamp": "2025-10-26T12:30:00"
}
```

**Response 409 Conflict:**
```json
{
  "exito": false,
  "mensaje": "Conflicto de estado: la operación no es válida para el estado actual de la solicitud.",
  "timestamp": "2025-10-26T12:30:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "exito": false,
  "mensaje": "Error interno del servidor. Intente nuevamente más tarde.",
  "timestamp": "2025-10-26T12:30:00"
}
```


**Última actualización:** 26 de octubre de 2025 - Parte 2/2 (Documentación Completa)
